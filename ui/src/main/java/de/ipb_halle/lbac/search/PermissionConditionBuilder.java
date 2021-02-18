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
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class PermissionConditionBuilder {

    private SearchRequestBuilder searchRequestBuilder;
    private List<AttributeType[]> fieldsToCheckAgainstAcl = new ArrayList<>();
    private User user;
    private ACPermission permission;

    public PermissionConditionBuilder(
            SearchRequestBuilder searchRequestBuilder,
            User user,
            ACPermission permission) {
        this.searchRequestBuilder = searchRequestBuilder;
        this.user = user;
        this.permission = permission;
    }

    public PermissionConditionBuilder addFields(AttributeType... fields) {
        fieldsToCheckAgainstAcl.add(fields);
        return this;
    }

    public Condition addPermissionCondition(Condition originalCondition) {
        Condition back = originalCondition;
        for (AttributeType[] fields : fieldsToCheckAgainstAcl) {
            back = addAclCondition(back, fields);
        }
        return back;
    }

    private Condition addAclCondition(Condition originalCondition, AttributeType[] fields) {
        Condition condition = searchRequestBuilder.getCondition(
                user,
                permission,
                fields);

        if (originalCondition != null) {
            return new Condition(
                    Operator.AND,
                    originalCondition,
                    condition);
        } else {
            return condition;
        }
    }
}
