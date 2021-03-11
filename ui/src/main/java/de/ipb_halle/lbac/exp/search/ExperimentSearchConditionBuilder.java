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
        for (Map.Entry<SearchCategory, Set<String>> entry : request.getSearchValues().entrySet()) {
            switch (entry.getKey()) {
                case LABEL:
                    addLabelCondition(conditionList, request.getSearchValues().get(entry.getKey()));
                    break;
            }
            switch (entry.getKey()) {
                case TEXT:
                    addTextCondition(conditionList, request.getSearchValues().get(entry.getKey()));
                    break;
            }
            switch (entry.getKey()) {
                case USER:
                    addOwnerCondition(conditionList, request.getSearchValues().get(entry.getKey()));
                    break;
            }
        }
        addMaterialCondition(conditionList, request);
        addItemCondition(conditionList, request);
        return conditionList;
    }

    private void addLabelCondition(List<Condition> conditionList, Set<String> values) {
        conditionList.add(getBinaryLeafConditionWithCast(
                Operator.IN,
                values,
                "(%s)",
                rootGraphName,
                AttributeType.LABEL));
    }

    private void addItemCondition(List<Condition> conditionList, SearchRequest request) {
        EntityGraph itemSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.itemSubGraphPath));
        ItemSearchConditionBuilder itemBuilder = new ItemSearchConditionBuilder(
                itemSubGraph, ExperimentEntityGraphBuilder.itemSubGraphName);
        List<Condition> subList = itemBuilder.getItemCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(itemSubGraph,
                    ExperimentEntityGraphBuilder.itemSubGraphPath,
                    request.getUser());
            conditionList.addAll(subList);
        }
    }

    private void addMaterialCondition(List<Condition> conditionList, SearchRequest request) {
        EntityGraph materialSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.materialSubGraphPath));
        MaterialSearchConditionBuilder matBuilder = new MaterialSearchConditionBuilder(
                materialSubGraph, ExperimentEntityGraphBuilder.materialSubGraphPath);
        List<Condition> subList = matBuilder.getMaterialCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(materialSubGraph,
                    ExperimentEntityGraphBuilder.materialSubGraphPath,
                    request.getUser());
            conditionList.addAll(subList);
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
