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

/**
 *
 * @author fmauz
 */
public class Condition {

    private Attribute attribute;
    private Condition leftCondition;
    private Operator operator;
    private Condition rightCondition;
    private Value value;


    public Condition(Attribute attribute, Operator operator, Value value) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public Condition(Attribute attribute, Operator operator) {
        if (! operator.isUnary()) {
            throw new IllegalArgumentException("Used binary operator with single argument");
        }
        this.attribute = attribute;
        this.operator = operator;
    }

    public Condition(Condition left, Operator operator, Condition right) {
        this.leftCondition = left;
        this.operator = operator;
        this.rightCondition = right;
    }

    public Condition(Condition condition, Operator operator) {
        if (! operator.isUnary()) {
            throw new IllegalArgumentException("Used binary operator with single argument");
        }
        this.leftCondition = condition;
        this.operator = operator;
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public Condition getLeftCondition() {
        return this.leftCondition;
    }

    public Operator getOperator() {
        return this.operator;
    }

    public Condition getRightCondition() {
        return this.rightCondition;
    }

    public Value getValue() {
        return this.value;
    }

    public boolean isLeaf() {
        return (leftCondition == null);
    }
}
