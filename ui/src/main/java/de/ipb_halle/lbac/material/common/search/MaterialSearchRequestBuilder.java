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
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.Value;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialSearchRequestBuilder extends SearchRequestBuilder {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public MaterialSearchRequestBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.MATERIAL;
    }

    public MaterialSearchRequestBuilder addID(Integer id) {
        addCondition(Operator.EQUAL,
                id,
                AttributeType.MATERIAL,
                AttributeType.LABEL);
        return this;
    }

    public MaterialSearchRequestBuilder addUserName(String userName) {
        addCondition(Operator.ILIKE,
                "%" + userName + "%",
                AttributeType.MEMBER_NAME);
        return this;
    }

    public MaterialSearchRequestBuilder addIndexName(String name) {
        addCondition(Operator.ILIKE,
                "%" + name + "%",
                AttributeType.MATERIAL,
                AttributeType.TEXT);
        return this;
    }

    public MaterialSearchRequestBuilder addProject(String projectName) {
        addCondition(Operator.ILIKE,
                "%" + projectName + "%",
                AttributeType.PROJECT_NAME);
        return this;
    }

    public MaterialSearchRequestBuilder addTypes(MaterialType... types) {
        addCondition(Operator.IN,
                getIdsFromMaterialTypes(types),
                AttributeType.MATERIAL_TYPE);
        return this;
    }

    public MaterialSearchRequestBuilder addSubMolecule(String molecule) {
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
