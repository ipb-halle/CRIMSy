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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
// import de.ipb_halle.lbac.search.lang.ConditionBuilder;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class SearchRequestBuilder {

    protected List<Condition> leafConditions = new ArrayList<>();
    protected int firstResultIndex;
    protected int maxResults;
    protected User user;
//    protected ConditionBuilder conditionBuilder;

    public SearchRequestBuilder(User u, int firstResultIndex, int maxResults) {
        this.user = u;
        this.firstResultIndex = firstResultIndex;
        this.maxResults = maxResults;
//        conditionBuilder = new ConditionBuilder(leafConditions);
    }

    protected void addCondition(Operator op, Object value, AttributeType... types) {
        leafConditions.add(new Condition(
                new Attribute(types),
                op,
                new de.ipb_halle.lbac.search.lang.Value(value)));
    }

    protected void addConditionWithCast(Operator op, Object value, String castExpression, AttributeType... types) {
        de.ipb_halle.lbac.search.lang.Value valueWithCast = new de.ipb_halle.lbac.search.lang.Value(value);
        valueWithCast.setCastExpression(castExpression);
        leafConditions.add(new Condition(
                new Attribute(types),
                op,
                valueWithCast));
    }

    private Condition buildConditions() {
        switch (this.leafConditions.size()) {
            case 0:
                return null;
            case 1:
                return this.leafConditions.get(0);
        }
        return new Condition(Operator.AND, leafConditions.toArray(new Condition[]{}));
    }

    public SearchRequest buildSearchRequest() {
        SearchRequestImpl searchRequest = new SearchRequestImpl(
                user,
                buildConditions(),
                firstResultIndex,
                maxResults
        );

        return searchRequest;
    }
}
