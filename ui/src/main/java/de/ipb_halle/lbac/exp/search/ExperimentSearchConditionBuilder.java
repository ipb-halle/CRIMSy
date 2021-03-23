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
package de.ipb_halle.lbac.exp.search;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.items.search.ItemSearchConditionBuilder;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class ExperimentSearchConditionBuilder extends SearchConditionBuilder {

    public ExperimentSearchConditionBuilder(EntityGraph graph, String rootName) {
        super(graph, rootName);
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... perm) {
        return addACL(getExperimentCondition(request, true),
                rootGraphName,
                request.getUser(),
                perm);
    }

    public List<Condition> getExperimentCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = new ArrayList<>();
        List<Condition> conditions = createMaterialCondition(request);
        conditions.addAll(createItemCondition(request));
        if (!conditions.isEmpty()) {
            Condition[] conditionArray = new Condition[conditions.size()];
            conditionArray = conditions.toArray(conditionArray);
            conditionList.add(new Condition(Operator.OR, conditionArray));
        }

        for (SearchCategory key : request.getSearchValues().keySet()) {
            switch (key) {
                case LABEL:
                    addLabelCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case TEMPLATE:
                    addTemplateCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case TEXT:
                    addTextCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
                case USER:
                    addOwnerCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;
            }
        }
        return conditionList;
    }

    private void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        conditionList.add(getBinaryLeafConditionWithCast(Operator.IN,
                values,
                "(%s)",
                rootGraphName,
                AttributeType.BARCODE));
    }

    private List<Condition> createItemCondition(SearchRequest request) {
        EntityGraph itemSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.itemSubGraphPath));
        ItemSearchConditionBuilder itemBuilder = new ItemSearchConditionBuilder(
                itemSubGraph, String.join("/", itemSubGraph.getGraphName()));
        List<Condition> subList = itemBuilder.getItemCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(
                    itemSubGraph,
                    ExperimentEntityGraphBuilder.itemSubGraphName,
                    request.getUser());

            for (Condition con : subList) {
                con.addParentPath(String.join("/", rootGraphName, ExperimentEntityGraphBuilder.linkedDataGraphPath));
            }

            return subList;
        }
        return new ArrayList<>();

    }

    private List<Condition> createMaterialCondition(SearchRequest request) {
        EntityGraph materialSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.materialSubGraphPath));
        MaterialSearchConditionBuilder matBuilder = new MaterialSearchConditionBuilder(
                materialSubGraph, String.join("/", rootGraphName, ExperimentEntityGraphBuilder.materialSubGraphPath));
        List<Condition> subList = matBuilder.getMaterialCondition(request, false);
        if (!subList.isEmpty()) {
            addSubGraphACL(
                    materialSubGraph,
                    materialSubGraph.getGraphName(),
                    request.getUser());

            return subList;
        }
        return new ArrayList<>();
    }

    private void addTemplateCondition(List<Condition> conditionList, Set<String> values) {
        if (values.size() == 1) {
            conditionList.add(getBinaryLeafConditionWithCast(
                    Operator.EQUAL,
                    Boolean.valueOf(values.iterator().next()),
                    "(%s)",
                    rootGraphName,
                    AttributeType.TEMPLATE));
        } else {
            throw new IllegalArgumentException("Illegal valueSet for template condition");
        }
    }

    private void addTextCondition(List<Condition> conditionList, Set<String> values) {
        conditionList.add(getTextCondition(values));
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
            subConditionList.add(getBinaryLeafCondition(
                    Operator.ILIKE,
                    "%" + value + "%",
                    rootGraphName + "/exp_records/exp_texts",
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

}
