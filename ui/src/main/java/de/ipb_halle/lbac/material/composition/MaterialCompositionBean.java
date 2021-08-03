/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialCompositionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    private List<Material> foundMaterials = new ArrayList<>();
    private List<Material> materialsInComposition = new ArrayList<>();
    private MaterialType choosenMaterialType;

    @Inject
    private MessagePresenter presenter;

    private CompositionType choosenCompositionType = CompositionType.EXTRACT;

    public List<CompositionType> getCompositionTypes() {
        return Arrays.asList(CompositionType.values());
    }

    public CompositionType getChoosenType() {
        return choosenCompositionType;
    }

    public void setChoosenType(CompositionType choosenType) {
        this.choosenCompositionType = choosenType;
    }

    public void actionStartSearch() {

    }

    public boolean isMaterialAlreadyInComposition(Material materialToLookFor) {
        for (Material materialAlreadyIn : materialsInComposition) {
            if (materialAlreadyIn.getId() == materialToLookFor.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a material to the composition if the material is not already in it
     * and its type is allowed by the compositionType
     *
     * @param materialToAdd
     */
    public void actionAddMaterialToComposition(Material materialToAdd) {
        if (!isMaterialAlreadyInComposition(materialToAdd)
                && choosenCompositionType.getAllowedTypes().contains(materialToAdd.getType())) {
            materialsInComposition.add(materialToAdd);
        }
    }

    /**
     * Switches the current material type. If null , an invalid or not allowed
     * type is passed, no change is made.
     *
     * @param type String value of @see MaterialType
     */
    public void actionSwitchMaterialType(String type) {
        materialsInComposition.clear();
        foundMaterials.clear();
        MaterialType t;
        if (type == null) {
            logger.error("No null value allowed as argument");
            return;
        }
        try {
            t = MaterialType.valueOf(type);
        } catch (IllegalArgumentException e) {
            logger.error(String.format("%s does not match a enum value", type));
            return;
        }
        if (choosenCompositionType.getAllowedTypes().contains(t)) {
            this.choosenMaterialType = t;
        } else {
            logger.error(String.format("CompositionType %s does not allow material type %s", choosenMaterialType, type));
        }
    }

    public MaterialType getChoosenMaterialType() {
        return choosenMaterialType;
    }

    public List<Material> getFoundMaterials() {
        return foundMaterials;
    }

    public boolean isMaterialTypePanelDisabled(String materialTypeString) {
        MaterialType type = MaterialType.valueOf(materialTypeString);
        if (type == null) {
            return false;
        }
        return !choosenCompositionType.getAllowedTypes().contains(type);
    }

    public List<Material> getMaterialsInComposition() {
        return materialsInComposition;
    }

}
