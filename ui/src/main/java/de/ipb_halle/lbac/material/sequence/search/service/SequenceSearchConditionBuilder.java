/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.service;

import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class SequenceSearchConditionBuilder extends MaterialSearchConditionBuilder {

    public SequenceSearchConditionBuilder(EntityGraph entityGraph, String rootName) {
        super(entityGraph, rootName);
    }

    @Override
    public List<Condition> getMaterialCondition(SearchRequest request, boolean toplevel) {
        List<Condition> conditionList = super.getMaterialCondition(request, toplevel);
        for (SearchCategory key : request.getSearchValues().keySet()) {
            switch (key) {
                case SEQUENCE_LIBRARY_TYPE:
                    addSequenceTypeCondition(conditionList, request.getSearchValues().get(key).getValues());
                    break;                
            }
        }
        return conditionList;
    }

    private void addSequenceTypeCondition(List<Condition> conditionList, Set<String> values) {
        if (hasExactOneEntry(values)) {
            String sequenceString = values.iterator().next();
            conditionList.add(getBinaryLeafCondition(Operator.EQUAL,
                    sequenceString,
                    rootGraphName + "/material_compositions/componentMaterials/sequences",
                    AttributeType.SEQUENCE_TYPE));
        }
    }

    private boolean hasExactOneEntry(Set<String> values) {
        return values != null && values.size() == 1;
    }

}
