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
package de.ipb_halle.lbac.search.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.material.MaterialType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class MaterialTypeFilter {

    private List<MaterialType> selectedMaterialTypes = new ArrayList<>();
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public MaterialTypeFilter() {
        selectedMaterialTypes.add(MaterialType.BIOMATERIAL);
        selectedMaterialTypes.add(MaterialType.STRUCTURE);
        selectedMaterialTypes.add(MaterialType.SEQUENCE);
        selectedMaterialTypes.add(MaterialType.COMPOSITION);
    }

    public List<MaterialType> getSelectedMaterialTypes() {
        return selectedMaterialTypes;
    }

    public void setSelectedMaterialTypes(List<MaterialType> selectedMaterialTypes) {
        this.selectedMaterialTypes = selectedMaterialTypes;
    }

    public String getLocalizedType(MaterialType unlocalizedType) {
        return Messages.getString(
                MESSAGE_BUNDLE,
                "advanced_search_type_" + unlocalizedType.toString(),
                null);
    }

    public List<MaterialType> getTypes() {
        List<MaterialType> types = new ArrayList<>();
        types.add(MaterialType.STRUCTURE);
        types.add(MaterialType.SEQUENCE);
        types.add(MaterialType.BIOMATERIAL);
        types.add(MaterialType.COMPOSITION);

        return types;
    }

    public boolean shouldSearchForSequence() {
        return selectedMaterialTypes.contains(MaterialType.SEQUENCE);
    }

    public boolean shouldSearchForBiomaterial() {
        return selectedMaterialTypes.contains(MaterialType.BIOMATERIAL);
    }

    public boolean shouldSearchForStructure() {
        return selectedMaterialTypes.contains(MaterialType.STRUCTURE);
    }

}
