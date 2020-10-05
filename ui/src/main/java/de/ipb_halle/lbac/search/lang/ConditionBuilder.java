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
package de.ipb_halle.lbac.search.lang;

import java.util.List;

/**
 *
 * @author fmauz
 */
public class ConditionBuilder {

    private List<Condition> conditions;

    public ConditionBuilder(List<Condition> leafConditions) {
        this.conditions = leafConditions;
    }

    public Condition buildConditionTree() {
        if (conditions.isEmpty()) {
            return null;
        }
        while (conditions.size() > 1) {
            Condition lastCondition = conditions.get(conditions.size() - 1);
            Condition secondlastCondition = conditions.get(conditions.size() - 2);
            createNewCondition(lastCondition, Operator.AND, secondlastCondition);
            conditions.remove(lastCondition);
            conditions.remove(secondlastCondition);
        }
        return conditions.get(0);
    }

    private void createNewCondition(Condition left, Operator op, Condition right) {
        conditions.add(0, new Condition(left, op, right));
    }

}
