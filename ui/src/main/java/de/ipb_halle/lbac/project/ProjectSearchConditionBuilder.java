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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchConditionBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;

/**
 *
 * @author fmauz
 */
public class ProjectSearchConditionBuilder extends SearchConditionBuilder {

    public ProjectSearchConditionBuilder(User u, int firstResultIndex, int maxResults) {
        super(u, firstResultIndex, maxResults);
        target = SearchTarget.PROJECT;
    }

    public ProjectSearchConditionBuilder addDeactivated(boolean deactivated) {
        addCondition(Operator.EQUAL,
                deactivated,
                AttributeType.DEACTIVATED);
        return this;
    }

    public ProjectSearchConditionBuilder addExactName(String name) {
        addCondition(Operator.EQUAL,
                name,
                AttributeType.PROJECT_NAME);
        return this;
    }

    @Override
    public Condition convertRequestToCondition(SearchRequest request) {
        return null;
    }

}
