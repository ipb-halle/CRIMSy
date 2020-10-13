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
    private EntityGraph entityGraph;
    private String fieldName;
    private boolean indexField;
    private OrderDirection orderDirection;
    private String orderKey;
    private String tableName;

    public DbField(boolean indexField) {
        this.alias = "";
        this.indexField = indexField;
        this.attributeTypes = new HashSet<> ();
        this.orderDirection = OrderDirection.NONE;
    }

    DbField addAttributeTag(AttributeTag tag) {
        if (tag != null) {
            this.attributeTypes.add(tag.type());
        }
        return this;
    }

    DbField addAttributeTag(AttributeTags tag) {
        if (tag != null) {
            for (AttributeType type : tag.types()) {
                this.attributeTypes.add(type);
            }
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

    public EntityGraph getEntityGraph() {
        return this.entityGraph;
    }

    public OrderDirection getOrderDirection() {
        return this.orderDirection;
    }

    public String getTableName() {
        return this.tableName;
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

    /**
     * compares table name, column name and order key of this instance 
     * against another DbField to select matching columns in ORDER BY 
     * clauses.
     * @return true if both objects have the same columnName, tableName
     * and orderKey.
     */
    public boolean matchesOrder(DbField field) {
        return this.columnName.equals(field.columnName) 
            && this.tableName.equals(field.tableName)
            && (((this.orderKey == null) && (field.orderKey == null))
            || this.orderKey.equals(field.orderKey));
    }

    public DbField setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public DbField setClassName(String className) {
        this.className = className;
        return this;
    }

    public DbField setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public DbField setEntityGraph(EntityGraph entityGraph) {
        this.entityGraph = entityGraph;
        return this;
    }

    public DbField setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public DbField setOrderDirection(OrderDirection dir) {
        this.orderDirection = dir;
        return this;
    }

    /**
     * Key field to identify fields for ORDER BY clauses.
     * Must be assigned during creation of the EntityGraph and 
     * will be evaluated in matchesOrder().
     * @param key the order key
     * @return this 
     */
    public DbField setOrderKey(String key) {
        this.orderKey = key;
        return this;
    }

    public DbField setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }
}

