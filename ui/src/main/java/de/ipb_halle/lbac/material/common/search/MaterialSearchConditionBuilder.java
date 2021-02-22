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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.Value;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Deprecated
    public MaterialSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.MATERIAL;
    }

    /**
     * Create an access control condition and combine it with other conditions.
     *
     * @param conditionList
     * @param request
     * @param acPermission
     * @return the combined condition
     */
    private Condition addACL(List<Condition> conditionList, SearchRequest request, ACPermission... acPermission) {
        List<Condition> subCondition = new ArrayList<>();
        for (ACPermission perm : acPermission) {
            subCondition.add(getACLCondition(
                    request.getUser(),
                    perm,
                    AttributeType.MATERIAL));
        }

        Condition aclCondition = (subCondition.size() > 1)
                ? new Condition(
                        Operator.OR,
                        subCondition.toArray(new Condition[0]))
                : subCondition.get(0);

        if (conditionList.isEmpty()) {
            return aclCondition;
        }
        conditionList.add(aclCondition);
        return new Condition(
                Operator.AND,
                conditionList.toArray(new Condition[0]));
    }

    private void addDeactivatedCondition(List<Condition> conditionList, Set<String> values) {
        Boolean deactivated = Boolean.FALSE;
        for (String val : values) {
            deactivated |= (val.compareToIgnoreCase("deactivated") == 0);
        }
        Condition con = getBinaryLeafCondition(
                Operator.EQUAL,
                deactivated,
                AttributeType.MATERIAL,
                AttributeType.DIRECT,
                AttributeType.DEACTIVATED);
        conditionList.add(con);
    }

    /**
     * Create an ORed condition for index values
     *
     * @param conditionList
     * @param values
     * @param isName limit search to names (true), indices (false) or not at all
     * (null)
     */
    private void addIndexCondition(List<Condition> conditionList, Set<String> values, Boolean isName) {
        if (isName != null) {
            Condition typeCondition = getBinaryLeafCondition(
                    isName ? Operator.EQUAL : Operator.NOT_EQUAL,
                    IndexTypeEntity.INDEX_TYPE_NAME,
                    AttributeType.MATERIAL,
                    AttributeType.INDEX_TYPE);
            conditionList.add(new Condition(
                    Operator.AND,
                    getIndexCondition(values),
                    typeCondition));
            return;
        }
        conditionList.add(getIndexCondition(values));
    }

    private void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        Set<Integer> idSet = new HashSet<>();
        for (String value : values) {
            try {
                int id = Integer.parseInt(value);
                if (id > 0) {
                    idSet.add(id);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (idSet.size() > 0) {
            conditionList.add(getBinaryLeafCondition(
                    Operator.IN,
                    idSet,
                    AttributeType.MATERIAL,
                    AttributeType.LABEL));
        }
    }

    private void addMaterialTypeCondition(List<Condition> conditionList, Set<String> values) {
        Set<MaterialType> types = new HashSet<>();
        for (String val : values) {
            MaterialType t = MaterialType.fromString(val);
            if (t != null) {
                types.add(t);
            }
        }
        if (types.size() > 0) {
            conditionList.add(getBinaryLeafCondition(
                    Operator.IN,
                    getIdsFromMaterialTypes(types.toArray(new MaterialType[0])),
                    AttributeType.MATERIAL,
                    AttributeType.MATERIAL_TYPE
            ));
        }
    }

    private void addOwnerCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple owners currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                AttributeType.MATERIAL,
                AttributeType.DIRECT,
                AttributeType.OWNER,
                AttributeType.MEMBER_NAME);
        conditionList.add(con);
    }

    private void addProjectCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple projects currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                AttributeType.MATERIAL,
                AttributeType.PROJECT,
                AttributeType.PROJECT_NAME);
        conditionList.add(con);
    }

    private void addStructureCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple structures currently not supported");
        }
        conditionList.add(getBinaryLeafConditionWithCast(
                Operator.SUBSTRUCTURE,
                values.iterator().next(),
                " CAST(%s AS MOLECULE) ",
                AttributeType.MOLECULE));
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... perm) {
        List<Condition> conditionList = new ArrayList<>();
        for (Map.Entry<SearchCategory, Set<String>> entry : request.getSearchValues().entrySet()) {
            switch (entry.getKey()) {
                case DEACTIVATED:
                    addDeactivatedCondition(conditionList, entry.getValue());
                    break;
                case INDEX:
                    addIndexCondition(conditionList, entry.getValue(), Boolean.FALSE);
                    break;
                case LABEL:
                    addLabelCondition(conditionList, entry.getValue());
                    break;
                case NAME:
                    addIndexCondition(conditionList, entry.getValue(), Boolean.TRUE);
                    break;
                case PROJECT:
                    addProjectCondition(conditionList, entry.getValue());
                    break;
                case STRUCTURE:
                    addStructureCondition(conditionList, entry.getValue());
                    break;
                case TEXT:
                    addIndexCondition(conditionList, entry.getValue(), null);
                    break;
                case TYPE:
                    addMaterialTypeCondition(conditionList, entry.getValue());
                    break;
                case USER:
                    addOwnerCondition(conditionList, entry.getValue());
                    break;
            }
        }
        return addACL(conditionList, request, perm);
    }

    @Deprecated
    public void setConditionsBySearchValues(MaterialSearchMaskValues values) {
        /*
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
         */
    }

    private Condition getIndexCondition(Set<String> values) {
        ArrayList<Condition> subConditionList = new ArrayList<>();
        for (String value : values) {
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    AttributeType.MATERIAL,
                    AttributeType.TEXT));
        }
        if (subConditionList.size() > 1) {
            return new Condition(
                    Operator.OR,
                    subConditionList.toArray(new Condition[0])
            );
        }
        if (subConditionList.size() > 0) {
            return subConditionList.get(0);
        }
        throw new IllegalArgumentException("Could not create Condition");
    }

    private Set<Integer> getIdsFromMaterialTypes(MaterialType... types) {
        Set<Integer> ids = new HashSet<>();
        for (MaterialType t : types) {
            ids.add(t.getId());
        }
        return ids;
    }
}
