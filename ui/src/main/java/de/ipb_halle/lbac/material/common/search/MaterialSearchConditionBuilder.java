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
import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
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
     * @param isName limit search to names (true; SearchCategory=NAME), indices
     * (false; SearchCategory=INDEX) or not at all (SearchCategory=TEXT) (null)
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
                    getIndexCondition(values, false),
                    typeCondition));
            return;
        }
        conditionList.add(getIndexCondition(values, true));
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
                //                AttributeType.DIRECT,
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
        List<Condition> conditionList = getMaterialCondition(request, true);
        return addACL(conditionList, request, AttributeType.MATERIAL, perm);
    }

    private AttributeType[] getIndexAttributes(boolean requireTopLevel) {
        if (requireTopLevel) {
            return new AttributeType[]{
                AttributeType.MATERIAL,
                AttributeType.TOPLEVEL,
                AttributeType.TEXT
            };
        }
        return new AttributeType[]{
            AttributeType.MATERIAL,
            AttributeType.TEXT};
    }

    private Condition getIndexCondition(Set<String> values, boolean requireTopLevel) {
        ArrayList<Condition> subConditionList = new ArrayList<>();
        for (String value : values) {
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    getIndexAttributes(requireTopLevel)));
        }
        return getDisjunction(subConditionList);
    }

    public List<Condition> getMaterialCondition(SearchRequest request, boolean toplevel) {
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
                    if (toplevel) {
                        addLabelCondition(conditionList, entry.getValue());
                    }
                    break;
                case NAME:
                    addIndexCondition(conditionList, entry.getValue(), Boolean.TRUE);
                    break;
                case PROJECT:
                    if (toplevel) {
                        addProjectCondition(conditionList, entry.getValue());
                    }
                    break;
                case STRUCTURE:
                    addStructureCondition(conditionList, entry.getValue());
                    break;
                case TEXT:
                    if (toplevel) {
                        addIndexCondition(conditionList, entry.getValue(), null);
                    }
                    break;
                case TYPE:
                    addMaterialTypeCondition(conditionList, entry.getValue());
                    break;
                case USER:
                    addOwnerCondition(conditionList, entry.getValue());
                    break;
            }
        }
        return conditionList;
    }

    private Set<Integer> getIdsFromMaterialTypes(MaterialType... types) {
        Set<Integer> ids = new HashSet<>();
        for (MaterialType t : types) {
            ids.add(t.getId());
        }
        return ids;
    }
}
