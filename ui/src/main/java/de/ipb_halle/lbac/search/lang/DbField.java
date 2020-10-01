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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fbroda
 */
public class DbField {
    private String alias;
    private Set<AttributeType> attributeTypes;
    private String className;
    private String columnName;
    private String fieldName;
    private boolean indexField;

    DbField(boolean indexField) {
        this.alias = "";
        this.indexField = indexField;
        this.attributeTypes = new HashSet<> ();
    }

    DbField addAttributeTag(AttributeTag tag) {
        if (tag != null) {
            this.attributeTypes.add(tag.type());
        }
        return this;
    }

    DbField addAttributeTypes(Set<AttributeType> types) {
        this.attributeTypes.addAll(types);
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DbField that = (DbField) o;
        if (alias.equals(that.alias) && columnName.equals(that.columnName)) {
            return true;
        }
        return false;
    }

    public String getAliasedColumnName() {
        return this.alias + "." + this.columnName;
    }

    public Set<AttributeType> getAttributeTypes() {
        return this.attributeTypes;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public int hashCode() {
        return this.alias.hashCode() + this.columnName.hashCode();
    }

    /**
     * @param attribute the Attribute to match 
     * @return true if this field matches (contains all) the requested
     * attribute types
     */
    public boolean matches(Attribute attribute) {
        return this.attributeTypes.containsAll(attribute.getTypes());
    }

    DbField setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    DbField setClassName(String className) {
        this.className = className;
        return this;
    }

    DbField setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    DbField setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }
}

