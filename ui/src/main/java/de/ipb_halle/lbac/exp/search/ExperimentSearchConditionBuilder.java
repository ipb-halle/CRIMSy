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
import de.ipb_halle.lbac.items.service.ItemEntityGraphBuilder;
import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;
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

    public ExperimentSearchConditionBuilder() {
        super(null, 0, 0);
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... perm) {
        return addACL(getExperimentCondition(request, true),
                request.getUser(), 
                AttributeType.EXPERIMENT, 
                perm);
    }

    public List<Condition> getExperimentCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = new ArrayList<>();
        for (Map.Entry<SearchCategory, Set<String>> entry : request.getSearchValues().entrySet()) {
            switch (entry.getKey()) {
                case NAME:
                    if (toplevel) {
                        addIndexCondition(conditionList, entry.getValue(), true);
                    }
                    break;
            }
        }

        addMaterialCondition(conditionList, request);
        addItemCondition(conditionList, request);
        return conditionList;
    }

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
    
    private void addItemCondition(List<Condition> conditionList, SearchRequest request) {
        EntityGraph itemSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.itemSubGraphName));
        ItemSearchConditionBuilder itemBuilder = new ItemSearchConditionBuilder(
                itemSubGraph, ExperimentEntityGraphBuilder.itemSubGraphName);
        List<Condition> subList = itemBuilder.getItemCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(itemSubGraph, 
                    request.getUser(), 
                    AttributeType.MATERIAL);
            conditionList.addAll(subList);
        }
    }

    private void addMaterialCondition(List<Condition> conditionList, SearchRequest request) {
        EntityGraph materialSubGraph = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, ExperimentEntityGraphBuilder.materialSubGraphName));
        MaterialSearchConditionBuilder matBuilder = new MaterialSearchConditionBuilder(
                materialSubGraph, ExperimentEntityGraphBuilder.materialSubGraphName);
        List<Condition> subList = matBuilder.getMaterialCondition(request, false);

        if (!subList.isEmpty()) {
            addSubGraphACL(materialSubGraph, 
                    request.getUser(), 
                    AttributeType.MATERIAL);
            conditionList.addAll(subList);
        }
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

}
