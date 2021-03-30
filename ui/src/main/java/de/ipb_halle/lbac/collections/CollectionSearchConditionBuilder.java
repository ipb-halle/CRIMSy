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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.webclient.XmlSetWrapper;
import de.ipb_halle.lbac.admission.ACPermission;
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
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class CollectionSearchConditionBuilder extends SearchConditionBuilder {

    public CollectionSearchConditionBuilder(EntityGraph entityGraph, String rootName) {
        super(entityGraph, rootName);
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... perm) {
        List<Condition> conditionList = getCollectionCondition(request, true);
        return addACL(conditionList, rootGraphName, request.getUser(), perm);
    }

    public List<Condition> getCollectionCondition(SearchRequest request, boolean toplevel) {
        XmlSetWrapper wrapper = request.getSearchValues().get(SearchCategory.COLLECTION);
        List<Condition> conditionList = new ArrayList<>();
        addCollectionCondition(
                conditionList,
                wrapper == null ? new HashSet() : wrapper.getValues());
        return conditionList;
    }

    /**
     * Create a condition to match selected Collections. The method ensures that
     * the condition list is not empty to prevent pruning of the ACL conditions.
     *
     * @param conditions
     * @param values
     */
    private void addCollectionCondition(List<Condition> conditions, Set<String> values) {
        Condition con;
        if (values != null && !values.isEmpty()) {
            con = getBinaryLeafCondition(
                    Operator.IN,
                    values,
                    rootGraphName,
                    AttributeType.COLLECTION);
        } else {
            con = getBinaryLeafCondition(
                    Operator.GREATER,
                    -1,
                    rootGraphName,
                    AttributeType.COLLECTION);
        }
        conditions.add(con);
    }

}
