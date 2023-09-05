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

import de.ipb_halle.crimsy_api.AttributeType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class ConditionValueFetcher {

    public List<Object> getValuesOfType(Condition con, AttributeType... type) {
        List<Object> back = new ArrayList<>();
        if (con == null) {
            return back;
        }
        if (con.getAttribute() != null) {
            if (containsAtributes(con, type)) {
                back.add(con.getValue().getValue());
            }
        } else {
            if (con.getConditions() != null) {
                for (Condition c : con.getConditions()) {
                    back.addAll(getValuesOfType(c, type));
                }
            }
        }
        return back;
    }

    public List<Condition> getConditionsOfType(Condition startCon, AttributeType... type) {
        List<Condition> conditions = new ArrayList<>();
        if (startCon != null) {
            if (startCon.getAttribute() != null) {
                if (containsAtributes(startCon, type)) {
                    conditions.add(startCon);
                }
            } else {
                if (startCon.getConditions() != null) {
                    for (Condition c : startCon.getConditions()) {
                        conditions.addAll(getConditionsOfType(c, type));
                    }
                }
            }
        }
        return conditions;
    }

    private boolean containsAtributes(Condition con, AttributeType... types) {
        boolean containsIt = true;
        for (AttributeType type : types) {
            if (!con.getAttribute().getTypes().contains(type)) {
                containsIt = false;
            }
        }
        return containsIt;
    }
}
