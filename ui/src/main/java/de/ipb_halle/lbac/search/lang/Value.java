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
import java.util.Collection;
import java.util.Date;
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
    private String jsonCast;

    public Value() {
    }

    public Value(Object value) {
        setValue(value);        
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
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        } else if (value instanceof Integer) {
            jsonCast = "CAST(%s AS INTEGER)";
        } else if (value instanceof Long) {
            jsonCast = "CAST(%s AS BIGINT)";
        } else if (value instanceof String) {
            jsonCast = "CAST(%s AS VARCHAR)";
        } else if (value instanceof Double) {
            jsonCast = "CAST(%s AS DOUBLE)";
        } else if (value instanceof Float) {
            jsonCast = "CAST(%s AS FLOAT)";
        } else if (value instanceof Boolean) {
            jsonCast = "CAST(%s AS BOOLEAN)";
        }else if (value instanceof Date) {
            jsonCast = "to_timestamp(CAST(%s AS BIGINT))";
        } else if (value.getClass().isInstance(Collection.class)) {
            Collection collection = (Collection) value;
            if(! collection.isEmpty()){
                Object obj = collection.iterator().next();
                if (obj instanceof Integer) {
                    jsonCast = "convert_jsonb_to_int_array(%s)";
                } else if (obj instanceof String) {
                    jsonCast = "convert_jsonb_to_varchar_array(%s)";
                } else {
                    throw new IllegalArgumentException("Illegal object type in collection");
                }
            }
            throw new IllegalArgumentException("Value class does not support empty collections.");
        } else {
            throw new IllegalArgumentException("datatype not supported: " + value.getClass().getName());
        }
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

    public String getJsonCast() {
        return jsonCast;
    }

    public String getCastJsonField(String field) {
        return String.format(this.jsonCast, field);
    }
}
