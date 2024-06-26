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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

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
        this.value = value;
    }
    
    /**
     * @param value the original condition value object
     * @return a modified cast parameter to use the JSON object
     * from the parameter table
     */
    public String getJsonParameter(String aliasedField) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        } else if (value instanceof Integer) {
            return String.format("CAST(%s->>'%s' AS INTEGER)", aliasedField, this.argumentKey);
        } else if (value instanceof Long) {
            return String.format("CAST(%s->>'%s' AS BIGINT)", aliasedField, this.argumentKey);
        } else if (value instanceof String) {
            return String.format("CAST(%s->>'%s' AS VARCHAR)", aliasedField, this.argumentKey);
        } else if (value instanceof Double) {
            return String.format("CAST(%s->>'%s' AS DOUBLE)", aliasedField, this.argumentKey);
        } else if (value instanceof Float) {
            return String.format("CAST(%s->>'%s' AS FLOAT)", aliasedField, this.argumentKey);
        } else if (value instanceof Boolean) {
            return String.format("CAST(%s->>'%s' AS BOOLEAN)", aliasedField, this.argumentKey);
        } else if (value instanceof Enum) {
            if (value.getClass().isAnnotationPresent(JsonEnumerateAsString.class)) {
                return String.format("CAST(%s->>'%s' AS VARCHAR)", aliasedField, this.argumentKey);
            } else {
                return String.format("CAST(%s->>'%s' AS INTEGER)", aliasedField, this.argumentKey);
            }
        } else if (value instanceof UUID) {
            return String.format("CAST(%s->>'%s' AS UUID)", aliasedField, this.argumentKey);    
        } else if (value instanceof Date) {
            return String.format("CAST(%s->>'%s' AS INTEGER)", aliasedField, this.argumentKey);
        } else if (value instanceof Collection) {
            Collection collection = (Collection) value;
            if(! collection.isEmpty()){
                Object obj = collection.iterator().next();
                if (obj instanceof Integer) {
                    return String.format("(SELECT jsonb_array_elements_text(%s->'%s')::INTEGER)", aliasedField, this.argumentKey);
                } else if (obj instanceof String) {
                    return String.format("(SELECT jsonb_array_elements_text(%s->'%s'))", aliasedField, this.argumentKey);
                } else {
                    throw new IllegalArgumentException("Illegal object type in collection");
                }
            }
            throw new IllegalArgumentException("Value class does not support empty collections.");
        } else {
            throw new IllegalArgumentException("datatype not supported: " + value.getClass().getName());
        }
    }

    public JsonElement getValueAsJsonElement() {
        Gson gson = new Gson();
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        } else if (value instanceof Date) {
            return gson.toJsonTree(((Date) value).getTime());
        } else if (value instanceof Enum) {
            if (! value.getClass().isAnnotationPresent(JsonEnumerateAsString.class)) {
                return gson.toJsonTree(((Enum) value).ordinal());
            }
        }
        return gson.toJsonTree(value);
    }
}
