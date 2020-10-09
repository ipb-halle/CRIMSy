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

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;

/**
 *
 * @author fmauz
 */
public class PermissionConditionBuilder {

    private ACListService aclistService;
    private AttributeType[] memberFieldAttributes;

    public PermissionConditionBuilder(ACListService aclistService, AttributeType[] memberFieldAttributes) {
        this.aclistService = aclistService;
        this.memberFieldAttributes = memberFieldAttributes;
    }

    public Condition addPermissionCondition(SearchRequest request, ACPermission permission) {
        Condition condition = aclistService.getCondition(
                request.getUser(),
                permission,
                memberFieldAttributes);

        if (request.getCondition() != null) {
            condition = new Condition(
                    Operator.AND,
                    request.getCondition(),
                    condition);
        }
        return condition;
    }
}
