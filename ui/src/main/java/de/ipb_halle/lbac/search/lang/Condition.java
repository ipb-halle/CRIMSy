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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class Condition {

    private Attribute attribute;
    private Condition[] conditions;
    private Operator operator;
    private Value value;

    public Condition(Attribute attribute, Operator operator, Value value) {
        if (value == null) {
            throw new IllegalArgumentException("Null value not allowed");
        }
        if (operator.isUnary()) {
            throw new IllegalArgumentException("Used binary operator with multiple arguments");
        }
        if (!operator.isLeafOperator()) {
            throw new IllegalArgumentException("Used non-leaf operator with leaf condition");
        }
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    public Condition(Attribute attribute, Operator operator) {
        if (!operator.isUnary()) {
            throw new IllegalArgumentException("Used binary operator with single argument");
        }
        if (!operator.isLeafOperator()) {
            throw new IllegalArgumentException("Used non-leaf operator with leaf condition");
        }
        this.attribute = attribute;
        this.operator = operator;
    }

    public Condition(Operator operator, Condition... condition) {
        if (operator.isLeafOperator()) {
            throw new IllegalArgumentException("Used leaf operator with non-leaf condition");
        }
        if ((condition == null)
                || (condition.length == 0)
                || ((condition.length == 1) && (!operator.isUnary()))
                || ((condition.length > 1) && operator.isUnary())) {
            throw new IllegalArgumentException("Illegal operator arity");
        }
        this.conditions = condition;
        this.operator = operator;
    }

    /**
     * prepend a parent path to all condition attributes of
     * this Condition tree.
     * @param path
     */
    public void addParentPath(String path) {
        if (isLeaf()) {
            this.attribute.addParentPath(path);
        } else {
            for (Condition cond : this.conditions) {
                cond.addParentPath(path);
            }
        }
    }

    /**
     * Collect the attributes of all leaf conditions in this
     * Condition tree.
     * @param attributeList
     */
    public void getAttributes(List<Attribute> attributeList) {
        if (isLeaf()) {
            attributeList.add(this.attribute);
        } else {
            for (Condition cond : this.conditions) {
                cond.getAttributes(attributeList);
            }
        }
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public Condition[] getConditions() {
        return this.conditions;
    }

    public Condition getLeftCondition() {
        return this.conditions[0];
    }

    public Operator getOperator() {
        return this.operator;
    }

    public Value getValue() {
        return this.value;
    }

    public boolean isLeaf() {
        return (this.conditions == null);
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public void setConditions(Condition[] conditions) {
        this.conditions = conditions;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void debug(String level){
        if(operator!=null){
            System.out.println(level+operator);
        }
        if(attribute!=null){
            String s=" ";
            for(AttributeType at:attribute.getTypes()){
            s+=at+":";
            }
            if(value!=null){
                s+="::"+value.getValue();
            }
            System.out.println(level+s+"( "+ attribute.getGraphPath()+")");
        }
        if(conditions!=null){
            for(Condition c:conditions){
                c.debug(level+"-");
            }
        }
    }

}
