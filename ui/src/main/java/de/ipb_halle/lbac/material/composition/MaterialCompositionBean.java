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

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.search.SearchResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.TabChangeEvent;

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
    private int MAX_RESULTS = 25;
    private String materialName;
    private CompositionType choosenCompositionType = CompositionType.EXTRACT;

    @Inject
    private MaterialService materialService;

    @Inject
    private transient MessagePresenter presenter;

    @Inject
    private UserBean userBean;

    public MaterialCompositionBean() {
    }

    public MaterialCompositionBean(
            UserBean userBean,
            MessagePresenter presenter,
            MaterialService materialService) {
        this.userBean = userBean;
        this.presenter = presenter;
        this.materialService = materialService;
    }

    public List<CompositionType> getCompositionTypes() {
        return Arrays.asList(CompositionType.values());
    }

    public CompositionType getChoosenType() {
        return choosenCompositionType;
    }

    public void setChoosenType(CompositionType choosenType) {
        if (choosenType != choosenCompositionType) {
            materialsInComposition.clear();
            foundMaterials.clear();
        }
        this.choosenCompositionType = choosenType;
        if (!choosenType.getAllowedTypes().contains(choosenMaterialType)) {
            choosenMaterialType = choosenType.getAllowedTypes().get(0);
        }
        logger.info(choosenMaterialType);
    }

    public void onTabChange(TabChangeEvent event) {
        logger.info(event.getTab().getTitle());
        if (event.getTab().getTitle().equals("Struktur")) {
            choosenMaterialType = MaterialType.STRUCTURE;
        } else if ((event.getTab().getTitle().equals("Sequenz"))) {
            choosenMaterialType = MaterialType.SEQUENCE;
        } else {
            choosenMaterialType = MaterialType.BIOMATERIAL;
        }
        logger.info(choosenMaterialType);
    }

    public void actionStartSearch() {
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(userBean.getCurrentAccount(), 0, MAX_RESULTS);
        requestBuilder.addMaterialType(choosenMaterialType);
        if (materialName != null && !materialName.trim().isEmpty()) {
            requestBuilder.setMaterialName(materialName);
        }
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());
        foundMaterials = result.getAllFoundObjects(choosenMaterialType.getClassOfDto(), result.getNode());
        logger.info("Materials in " + materialsInComposition.size());
    }

    public List<Material> getMaterialsThatCanBeAdded() {
        return foundMaterials.stream()
                .filter(m -> choosenCompositionType.getAllowedTypes().contains(m.getType()))
                .filter((m -> !isMaterialAlreadyInComposition(m)))
                .collect(Collectors.toCollection(ArrayList::new));
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
            logger.info("Put " + materialToAdd.getFirstName() + " in( " + materialsInComposition.size() + ")");

        }
    }

    public void actionRemoveMaterialFromComposition(Material materialToRemove) {
        materialsInComposition.remove(materialToRemove);
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

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

}
