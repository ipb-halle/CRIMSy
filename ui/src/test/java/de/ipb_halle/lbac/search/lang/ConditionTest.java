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
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class ConditionTest {

    private String checkCondition(Attribute attr, Operator oper, Value value) {
        try {
            Condition c = new Condition(attr, oper, value);
        } catch(IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return null;
    }

    private String checkCondition(Operator oper, Condition... cond) {
        try {
            Condition c = new Condition(oper, cond);
        } catch(IllegalArgumentException ex) {
            return ex.getMessage();
        }
        return null;
    }

    @Test
    public void test001_Condition() {
        Attribute attr1 = new Attribute(AttributeType.TEXT);
        Attribute attr2 = new Attribute(AttributeType.PERM_READ);
        Value value = new Value("foo");

        Assert.assertNull("LEAF: normal condition", 
            checkCondition(attr1, Operator.EQUAL, value));
        Assert.assertEquals("LEAF: null value", 
            "Null value not allowed",
            checkCondition(attr1, Operator.EQUAL, null));
        Assert.assertEquals("LEAF: non leaf operator", 
            "Used non-leaf operator with leaf condition",
            checkCondition(attr1, Operator.AND, value));
        Assert.assertEquals("LEAF: unary operator in binary leaf condition",
            "Used binary operator with multiple arguments",
            checkCondition(attr1, Operator.IS_TRUE, value));

        Condition c1 = new Condition(attr1, Operator.EQUAL, value);
        Condition c2 = new Condition(attr2, Operator.IS_TRUE);
        Assert.assertNull("NODE: normal condition",
            checkCondition(Operator.AND, c1, c2));
        Assert.assertEquals("NODE: illegal leaf operator",
            "Used leaf operator with non-leaf condition",
            checkCondition(Operator.EQUAL, c1, c2));
        Assert.assertEquals("NODE: illegal operator arity: unary",
            "Illegal operator arity",
            checkCondition(Operator.NOT, c1, c2));
        Assert.assertEquals("NODE: illegal operator arity: binary",
            "Illegal operator arity",
            checkCondition(Operator.AND, c1));

    }
}
