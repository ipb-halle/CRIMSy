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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author fbroda
 */
@XmlRootElement
public class Value {

    @XmlTransient
    private Object value;

    private String argumentKey;
    @XmlTransient
    private String castExpression;

    private HashSet valueSet;
    private ArrayList valueList;
    private Object singleValue;
    private String transferCastExpression;

    public Value() {
    }

    public Value(Object value) {
        this.value = value;
    }

    public String getArgumentKey() {
        return this.argumentKey;
    }

    public String getCastArgument() {
        String arg = ":" + this.argumentKey;
        if (castExpression != null) {
            return String.format(this.castExpression, arg);
        }
        return arg;
    }

    public String getCastExpression() {
        return castExpression;
    }

    public Object getValue() {
        return this.value;
    }

    public Value setArgumentKey(String argumentKey) {
        this.argumentKey = argumentKey;
        return this;
    }

    public Value setCastExpression(String castExpression) {
        this.castExpression = castExpression;
        return this;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public HashSet getValueSet() {
        return valueSet;
    }

    public void setValueSet(HashSet valueSet) {
        this.valueSet = valueSet;
    }

    public ArrayList getValueList() {
        return valueList;
    }

    public void setValueList(ArrayList valueList) {
        this.valueList = valueList;
    }

    public Object getSingleValue() {
        return singleValue;
    }

    public void setSingleValue(Object singleValue) {
        this.singleValue = singleValue;
    }

}
