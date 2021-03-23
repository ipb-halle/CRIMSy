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
package de.ipb_halle.lbac.items.search;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.items.service.ItemEntityGraphBuilder;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class ItemSearchConditionBuilder extends SearchConditionBuilder {

    private final static String itemLabelPattern = "[0-9]{8,12}";

    public ItemSearchConditionBuilder(EntityGraph entityGraph, String rootName) {
        super(entityGraph, rootName);
    }

    private void addDeactivatedCondition(List<Condition> conditionList, Set<String> values) {
        Boolean deactivated = Boolean.FALSE;
        for (String val : values) {
            deactivated |= (val.compareToIgnoreCase("deactivated") == 0);
        }
        Condition con = getBinaryLeafCondition(
                Operator.EQUAL,
                deactivated,
                rootGraphName,
                AttributeType.DEACTIVATED);
        conditionList.add(con);
    }

    private void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        Set<String> idSet = new HashSet<>();
        for (String value : values) {
            if (value.matches(itemLabelPattern)) {
                idSet.add(value);
            }
        }
        if (idSet.size() > 0) {
            conditionList.add(getBinaryLeafConditionWithCast(Operator.IN,
                    idSet,
                    "(%s)",
                    rootGraphName,
                    AttributeType.BARCODE));
        }
    }

    /**
     * ToDo: possibly replace by call to ContainerSearchConditionBuilder?
     *
     * @param conditionList
     * @param values
     */
    private void addLocationCondition(List<Condition> conditionList, Set<String> values) {
        List<Condition> subList = new ArrayList<>();
        for (String value : values) {
            subList.add(getBinaryLeafCondition(Operator.ILIKE,
                    "%" + value + "%",
                    rootGraphName + "/containers",
                    AttributeType.BARCODE));

            subList.add(getBinaryLeafCondition(Operator.ILIKE,
                    "%" + value + "%",
                    rootGraphName + "/nested_containers/containers",
                    AttributeType.BARCODE));
        }
        conditionList.add(getDisjunction(subList));
    }

    private void addMaterialCondition(List<Condition> conditionList, SearchRequest request) {
        EntityGraph materialSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ItemEntityGraphBuilder.materialSubGraphPath));
        MaterialSearchConditionBuilder matBuilder = new MaterialSearchConditionBuilder(
                materialSubGraph, ItemEntityGraphBuilder.materialSubGraphPath);
        List<Condition> subList = matBuilder.getMaterialCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(materialSubGraph,
                    ItemEntityGraphBuilder.materialSubGraphPath,
                    request.getUser());

            for (Condition con : subList) {
                con.addParentPath(rootGraphName);
                conditionList.add(con);
            }
        }
    }

    private void addOwnerCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() != 1) {
            throw new IllegalArgumentException("Addition of multiple owners currently not supported");
        }
        Condition con = getBinaryLeafCondition(
                Operator.ILIKE,
                "%" + values.iterator().next() + "%",
                rootGraphName + "/USERSGROUPS",
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
                rootGraphName + "/projects",
                AttributeType.PROJECT_NAME);
        conditionList.add(con);
    }

    /**
     * Create an ORed condition for index values
     *
     * @param conditionList
     * @param values
     */
    private void addTextCondition(List<Condition> conditionList, Set<String> values) {
        conditionList.add(getTextCondition(values));
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... acPermission) {

        return addACL(getItemCondition(request, true),
                rootGraphName,
                request.getUser(),
                acPermission);
    }

    public List<Condition> getItemCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = new ArrayList<>();
        for (SearchCategory key : request.getSearchValues().keySet()) {
            switch (key) {
                case DEACTIVATED:
                    addDeactivatedCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case LABEL:
                    if (toplevel) {
                        addLabelCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
                case LOCATION:
                    addLocationCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case PROJECT:
                    if (toplevel) {
                        addProjectCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
                case TEXT:
                    if (toplevel) {
                        addTextCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
                case USER:
                    if (toplevel) {
                        addOwnerCondition(conditionList, request.getSearchValues().get(key).getValues());
                    }
                    break;
            }
        }
        addMaterialCondition(conditionList, request);
        return conditionList;
    }

    private Condition getTextCondition(Set<String> values) {
        ArrayList<Condition> subConditionList = new ArrayList<>();
        for (String value : values) {
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    rootGraphName,
                    AttributeType.TOPLEVEL,
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
}
