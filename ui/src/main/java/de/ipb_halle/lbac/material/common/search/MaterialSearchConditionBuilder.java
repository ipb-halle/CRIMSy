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
package de.ipb_halle.lbac.material.common.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialSearchConditionBuilder extends SearchConditionBuilder {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialSearchConditionBuilder() {
        super(null, 0, 0);
    }
    
    @Override
    public  Condition convertRequestToCondition(SearchRequest request){
//        for(SearchCategory sc : request.getSearchValues().keySet()){
//            if(sc==SearchCategory.STRUCTURE){
//                addSubMolecule(request.getSearchValues().get(sc).iterator().next());
//            }
//        }
//        
       return null;
    }

    public MaterialSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.MATERIAL;
    }

    public MaterialSearchConditionBuilder addID(Integer id) {
        addCondition(Operator.EQUAL,
                id,
                AttributeType.MATERIAL,
                AttributeType.LABEL);
        return this;
    }

    public MaterialSearchConditionBuilder addDeactivated(boolean deactivated) {
        addCondition(Operator.EQUAL,
                deactivated,
                AttributeType.MATERIAL,
                AttributeType.DEACTIVATED);
        return this;
    }

    public MaterialSearchConditionBuilder addUserName(String userName) {
        addCondition(Operator.ILIKE,
                "%" + userName + "%",
                AttributeType.MEMBER_NAME);
        return this;
    }

    public MaterialSearchConditionBuilder addIndexName(String name) {
        addCondition(Operator.ILIKE,
                "%" + name + "%",
                AttributeType.MATERIAL,
                AttributeType.TEXT);
        return this;
    }

    public MaterialSearchConditionBuilder addProject(String projectName) {
        addCondition(Operator.ILIKE,
                "%" + projectName + "%",
                AttributeType.PROJECT_NAME);
        return this;
    }

    public MaterialSearchConditionBuilder addTypes(MaterialType... types) {
        addCondition(Operator.IN,
                getIdsFromMaterialTypes(types),
                AttributeType.MATERIAL_TYPE);
        return this;
    }

    public MaterialSearchConditionBuilder addSubMolecule(String molecule) {
        addConditionWithCast(Operator.SUBSTRUCTURE,
                molecule,
                " CAST(%s AS MOLECULE) ",
                AttributeType.MOLECULE);
        return this;
    }

    public void setConditionsBySearchValues(MaterialSearchMaskValues values) {
        if (values != null) {
            if (values.id != null) {
                addID(values.id);
            }
            if (values.index != null && !values.index.trim().isEmpty()) {
                addIndexName(values.index);
            }
            if (values.materialName != null && !values.materialName.trim().isEmpty()) {
                addIndexName(values.materialName);
            }
            if (values.molecule != null && !values.molecule.trim().isEmpty()) {
                addSubMolecule(values.molecule);
            }
            if (values.projectName != null && !values.projectName.trim().isEmpty()) {
                addProject(values.projectName);
            }

            if (values.type != null && !values.type.isEmpty()) {
                int size = values.type.size();
                MaterialType[] types = new MaterialType[size];
                addTypes(values.type.toArray(types));
            } else {
                addTypes(MaterialType.BIOMATERIAL,
                        MaterialType.COMPOSITION,
                        MaterialType.CONSUMABLE,
                        MaterialType.SEQUENCE,
                        MaterialType.STRUCTURE,
                        MaterialType.TISSUE);
            }

        }
    }

    private Set<Integer> getIdsFromMaterialTypes(MaterialType... types) {
        Set<Integer> ids = new HashSet<>();
        for (MaterialType t : types) {
            ids.add(t.getId());
        }
        return ids;
    }

}
