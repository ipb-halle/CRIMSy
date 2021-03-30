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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.lbac.webclient.XmlSetWrapper;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.collections.CollectionSearchConditionBuilder;
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
public class DocumentSearchConditionBuilder extends SearchConditionBuilder {

    public DocumentSearchConditionBuilder(EntityGraph entityGraph, String rootGraphName) {
        super(entityGraph, rootGraphName);
    }

    private void addWordRootCondition(List<Condition> conditionList, Set<String> values) {
        Condition con = getBinaryLeafCondition(
                Operator.IN,
                values,
                String.join("/", rootGraphName, "termvectors"),
                AttributeType.WORDROOT);
        conditionList.add(con);
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request, ACPermission... acPermission) {
        EntityGraph collection = entityGraph.selectSubGraph(
                String.join("/", rootGraphName, "collections"));

        List<Condition> conditionList = new ArrayList<>();

        XmlSetWrapper wrapper = request.getSearchValues().get(SearchCategory.WORDROOT);
        if (wrapper != null) {
            addWordRootCondition(conditionList, wrapper.getValues());
        }

        CollectionSearchConditionBuilder collectionBuilder = new CollectionSearchConditionBuilder(
                collection,
                String.join("/", rootGraphName, "collections"));
        conditionList.add(collectionBuilder.convertRequestToCondition(request, acPermission));
        return new Condition(Operator.AND, conditionList.stream().toArray(Condition[]::new));

    }
}
