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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fbroda
 */
public class EntityGraph {
    
    private Class<?> entityClass;
    private String query;

    private Set<AttributeType> attributeTypes;
    private List<EntityGraph> children;
    private List<LinkField> linkFields;
    private EntityGraph parent;
    private JoinType joinType;

    private Map<String, DbField> fieldMap;
    private String alias;
    private boolean active;
    private int indexCount;
    private String tableName;


    /**
     * constructor
     */
    public EntityGraph(Class entityClass) {
        this();
        this.entityClass = entityClass;
        processClass();
    }


    public EntityGraph(String query) {
        this();
        this.query = query;
        this.tableName = "__QUERY__";
    }
        
    private EntityGraph() {
        this.active = false;
        this.attributeTypes = new HashSet<> ();
        this.children = new ArrayList<> ();
        this.joinType = JoinType.INNER;
        this.linkFields = new ArrayList<> ();
        this.indexCount = 0;
        this.fieldMap = new HashMap<> ();
    }

    protected void activate() {
        this.active = true;
        if (this.parent != null) {
            this.parent.activate();
        }
    }

    protected EntityGraph addAttributeType(AttributeType type) {
        this.attributeTypes.add(type);
        for (EntityGraph eg : this.children) {
            eg.addAttributeType(type);
        }
        return this;
    }

    protected EntityGraph addAttributeTypes(Set<AttributeType> types) {
        this.attributeTypes.addAll(types);
        for (EntityGraph eg : this.children) {
            eg.addAttributeTypes(types);
        }
        return this;
    }

    public EntityGraph addChild(EntityGraph child) {
        if (child.getLinks().size() == 0) {
            throw new IllegalArgumentException("Child entity without link");
        }
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    public EntityGraph addField(DbField dbField) {
        this.fieldMap.put(dbField.getColumnName(), dbField); 
        return this;
    }

    public EntityGraph addLinkField(String parent, String child) {
        this.linkFields.add(new LinkField(parent, child));
        return this;
    }

    /**
     * convert the fieldName:DbField mapping into a columnName:DbField mapping
     */
    protected void buildFieldMap() {
        Map<String, DbField> tempMap = new HashMap<> ();
        for (DbField field : this.fieldMap.values()) {
            tempMap.put(field.getColumnName(), field);
        }
        this.fieldMap = tempMap;
    }

    /**
     * @param column a column name
     * @return whether this EntityGraph contains the given column
     */
    protected boolean containsColumn(String column) {
        return this.fieldMap.containsKey(column);
    }

    protected boolean getActive() {
        return this.active;
    }

    protected String getAlias() {
        return this.alias;
    }

    /** 
     * @return recursively return all fields of this EntityGraph and 
     * all of its children
     */
    protected Set<DbField> getAllFields() {
        Set<DbField> fields = new HashSet<> ();
        fields.addAll(this.fieldMap.values());
        for (EntityGraph eg : this.children) {
            fields.addAll(eg.getAllFields());
        }
        return fields;
    }

    /**
     * @return the children of this EntityGraph
     */
    protected List<EntityGraph> getChildren() {
        return this.children;
    }
 
    /**
     * @return returns the fields of this EntityGraph only (excluding children)
     */
    protected Map<String, DbField> getFieldMap() {
        return this.fieldMap;
    }

    /**
     * @return the JoinType for this EntityGraph (in relation to its parent).
     */
    protected JoinType getJoinType() {
        return this.joinType;
    }

    /**
     * @return the list of LinkFields
     */
    protected List<LinkField> getLinks() {
        return this.linkFields;
    }

    protected EntityGraph getParent() {
        return this.parent;
    }

    /**
     * @return the sql query string (for non entity class EntityGraph objects)
     */
    protected String getQuery() {
        return this.query;
    }

    /**
     * @return the table name (or a user specified alias for queries)
     */
    protected String getTableName() {
        return this.tableName;
    }

    protected boolean hasChildren() {
        return this.children.size() > 0; 
    }


    protected boolean isEntityClass() {
        return this.entityClass != null;
    }

    private void processAttributeOverride(String parentFieldName, AttributeOverride override) {
        String name = parentFieldName.isEmpty() ?
                override.name() :
                parentFieldName + "." + override.name();

        DbField dbField = this.fieldMap.get(name);
        dbField.setColumnName(override.column().name());
    }

    private void processAttributeOverrides(String parentFieldName, AnnotatedElement element) {
        AttributeOverride ao = element.getAnnotation(AttributeOverride.class);
        if (ao != null) {
            processAttributeOverride(parentFieldName, ao);
        }

        AttributeOverrides attributeOverrides = element.getAnnotation(AttributeOverrides.class);
        if (attributeOverrides != null) {
            for (AttributeOverride override : attributeOverrides.value()) {
                processAttributeOverride(parentFieldName, override);
            }
        }
    }

    /** 
     * Process the annotations of a class. Many of the 
     * 'more special' Hibernate annotations will be ignored.
     */
    private void processClass() {
        processTable();
        AttributeTag tag = this.entityClass.getAnnotation(AttributeTag.class);
        if (tag != null) {
            addAttributeType(tag.type());
        }
        processFields(this.entityClass, "", false);
        buildFieldMap();
    }

    /**
     * Process a single field of a class. The field will not be added 
     * to the fieldMap, if it is not annotated with <code>@Id</code> or <code>@Column</code>.
     * Fields annotated with <code>@EmbeddedId</code> will be resolved.
     *
     * Note: Annotations <code>@EntityCollection, @OneTo*, @ManyTo*</code>  
     * are currently ignored.
     * 
     * @param parentFieldName name of the field embedding the class of this field
     * @param field the field
     */
    private void processColumn(String parentFieldName, Field field) {
        String fieldName = parentFieldName.isEmpty() 
                ? field.getName() 
                : parentFieldName + "." + field.getName();

        if (field.getAnnotation(EmbeddedId.class) != null) {
            Class<?> clazz = field.getType();
            processFields(clazz, fieldName, true);
            return;
        }

        if (field.getAnnotation(Id.class) != null) {
            processId(parentFieldName, field);
            return;
        }

        Embedded embedded = field.getAnnotation(Embedded.class);
        if (embedded != null) {
            Class clazz = field.getType();
            processFields(clazz, fieldName, false);
            return;
        }


        AttributeTag attributeTag = field.getAnnotation(AttributeTag.class);
        AttributeTags attributeTags = field.getAnnotation(AttributeTags.class);
        String columnName = field.getName();
        Column column = field.getAnnotation(Column.class);
        Basic basic = field.getAnnotation(Basic.class);
        if ((basic != null) || (column != null)) {
            if ((column != null) && (! column.name().isEmpty())) {
                columnName = column.name();
            }
            DbField dbField = new DbField(false)
                .setEntityGraph(this)
                .setFieldName(fieldName)
                .setColumnName(columnName)
                .setTableName(this.tableName)
                .addAttributeTag(attributeTag)
                .addAttributeTag(attributeTags)
                .addAttributeTypes(this.attributeTypes);
            this.fieldMap.put(fieldName, dbField);
        }
    }

    /**
     * process the fields of a class to obtain a map of database fields
     * @param clazz the class to resolve the fields for
     * @param isIndex if the class is an EmbeddedId
     */
    private void processFields(Class<?> clazz, String parentFieldName, boolean isIndex) {
        for(Field field : clazz.getDeclaredFields()) {
            if (isIndex) {
                processId(parentFieldName, field);
            } else {
                processColumn(parentFieldName, field);
            }
            String name = parentFieldName.isEmpty() ?
                    field.getName() :
                    parentFieldName + "." + field.getName();
            processAttributeOverrides(name, field);
        }

        Class<?> superClass = clazz.getSuperclass();
        if ((superClass != null) 
                && (superClass.getAnnotation(MappedSuperclass.class) != null)) {
            processFields(superClass, parentFieldName, isIndex);
        }
        processAttributeOverrides(parentFieldName, clazz);
    }

    /**
     * process an index field; could be either a 
     * single field with @Id annotation or a field from 
     * an @EmbeddedId object.
     * @param parentFieldName name of the field embedding the class of this field 
     * @param field the index field
     */
    private void processId(String parentFieldName, Field field) {
        String fieldName = parentFieldName.isEmpty() 
                ? field.getName() 
                : parentFieldName + "." + field.getName();

        String columnName = field.getName();
        Column column = field.getAnnotation(Column.class);
        if ((column != null) && (! column.name().isEmpty())) {
            columnName = column.name();
        }
        
        AttributeTag attributeTag = field.getAnnotation(AttributeTag.class);
        AttributeTags attributeTags = field.getAnnotation(AttributeTags.class);
        DbField dbField = new DbField(true)
                .setEntityGraph(this)
                .setFieldName(fieldName) 
                .setColumnName(columnName) 
                .setTableName(this.tableName)
                .addAttributeTag(attributeTag) 
                .addAttributeTag(attributeTags) 
                .addAttributeTypes(this.attributeTypes);
        this.fieldMap.put(fieldName, dbField);
        this.indexCount++;
    }

    /**
     * process <code>@Table</code> annotation for an entity class.
     * If the annotation is missing, the simple name of the class
     * is used.
     */
    private void processTable(){
        Table table = this.entityClass.getAnnotation(Table.class);
        if (table != null) {
            StringBuilder sb = new StringBuilder();
            if ((table.schema() != null) && (! table.schema().isEmpty())) {
                sb.append(table.schema());
                sb.append(".");
            }
            sb.append(table.name());
            this.tableName = sb.toString();
            return;
        } 
        this.tableName = this.entityClass.getSimpleName();
    }

    protected void reset() {
        this.active = false;
        for (EntityGraph graph : this.children) {
            graph.reset();
        }
    }

    protected void setAlias(String alias) {
        this.alias = alias;
        for (DbField field : this.fieldMap.values()) {
            field.setAlias(alias);
        }
        int i = 0;
        for (EntityGraph graph : this.children) {
            graph.setAlias(alias + "_" + String.valueOf(i));
            i++;
        }
    }

    public EntityGraph setJoinType(JoinType joinType) {
        this.joinType = joinType;
        return this;
    }

    public EntityGraph setOrderKey(String key) {
        for (DbField field : this.fieldMap.values()) {
            field.setOrderKey(key);
        }
        return this;
    }

    private void setParent(EntityGraph parent) {
        this.parent = parent;
    }

    /**
     * set the table name for query type EntityGraphs to assist in debugging
     * @param tableName 
     * @return this EntityGraph
     * @throws UnsupportedOperationException if this is an entity class type EntityGraph object
     */
    public EntityGraph setTableName(String tableName) {
        if (isEntityClass()) {
            throw new UnsupportedOperationException("Illegal attempt to set table name for entity class");
        }
        this.tableName = tableName;
        return this;
    }

}
