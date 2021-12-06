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
import javax.persistence.GeneratedValue;
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
    private AttributeType subSelectAttribute;
    private Condition subSelectCondition;

    private Set<AttributeType> attributeTypes;
    private List<EntityGraph> children;
    private List<LinkField> linkFields;
    private EntityGraph parent;
    private JoinType joinType;

    private Map<String, DbField> fieldMap;
    private String alias;
    private Set<String> active;
    private int indexCount;
    private String tableName;
    private String graphName;
    private Map<String, String> graphPathMap;

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
        this.graphName = this.tableName;
    }

    private EntityGraph() {
        this.active = new HashSet<>();
        this.attributeTypes = new HashSet<>();
        this.children = new ArrayList<>();
        this.joinType = JoinType.INNER;
        this.linkFields = new ArrayList<>();
        this.indexCount = 0;
        this.fieldMap = new HashMap<>();
        this.graphName = "";
        this.graphPathMap = new HashMap<>();
    }

    protected void activate(String context) {
        this.active.add(context);
        if (this.parent != null) {
            this.parent.activate(context);
        }
    }

    /**
     * Add an AttributeType to this EntityGraph and all its fields only.
     *
     * @param type
     * @return
     */
    public EntityGraph addAttributeType(AttributeType type) {
        this.attributeTypes.add(type);
        for (DbField field : this.fieldMap.values()) {
            field.addAttributeType(type);
        }
        return this;
    }

    /**
     * Add an AttributeType to this EntityGraph, all its fields and all its
     * children.
     *
     * @param type
     * @return
     */
    public EntityGraph addAttributeTypeInherit(AttributeType type) {
        addAttributeType(type);
        for (EntityGraph eg : this.children) {
            eg.addAttributeTypeInherit(type);
        }
        return this;
    }

    public EntityGraph addAttributeTypes(Set<AttributeType> types) {
        this.attributeTypes.addAll(types);
        for (DbField field : this.fieldMap.values()) {
            field.addAttributeTypes(types);
        }
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

    public EntityGraph addChildInherit(EntityGraph child) {
        addChild(child);
        child.addAttributeTypes(this.attributeTypes);
        return this;
    }

    /**
     * add a field definition for query type EntityGraph objects
     *
     * @param dbField a field returned from the query string of the EntityGraph
     * object
     * @return the EntityGraph object
     * @throws UnsupportedOperationException if object is not a query type
     * EntityGraph
     */
    public EntityGraph addField(DbField dbField) {
        if (isEntityClass()) {
            throw new UnsupportedOperationException("Illegal attempt to add field to entity class");
        }
        this.fieldMap.put(dbField.getColumnName(), dbField);
        return this;
    }

    public EntityGraph addLinkField(String parent, String child) {
        this.linkFields.add(new LinkField(parent, child));
        return this;
    }

    public EntityGraph addLinkField(LinkField field) {
        this.linkFields.add(field);
        return this;
    }

    /**
     * convert the fieldName:DbField mapping into a columnName:DbField mapping
     */
    protected void buildFieldMap() {
        Map<String, DbField> tempMap = new HashMap<>();
        for (DbField field : this.fieldMap.values()) {
            tempMap.put(field.getColumnName(), field);
        }
        this.fieldMap = tempMap;
    }

    /**
     * Compute the context sensitive graph path for this EntityGraph object.
     * Computation can start either from the root element of an EntityGraph or
     * from individual sub-graphs (e.g. for sub-selects).
     *
     * @param context
     * @param path
     */
    public void computeGraphPath(String context, String path) {
        String gp;
        if ((path != null) && (path.length() > 0)) {
            gp = String.join("/", path, this.graphName);
        } else {
            gp = this.graphName;
        }
        this.graphPathMap.put(context, gp);
        for (EntityGraph graph : this.children) {
            graph.computeGraphPath(context, gp);
        }
    }

    /**
     * @param column a column name
     * @return whether this EntityGraph contains the given column
     */
    protected boolean containsColumn(String column) {
        return this.fieldMap.containsKey(column);
    }

    protected boolean getActive(String context) {
        return this.active.contains(context);
    }

    protected String getAlias() {
        return this.alias;
    }

    /**
     * @return recursively return all fields of this EntityGraph and all of its
     * children
     */
    protected Set<DbField> getAllFields() {
        Set<DbField> fields = new HashSet<>();
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

    protected EntityGraphType getEntityGraphType() {
        if (this.entityClass != null) {
            if (this.subSelectCondition == null) {
                return EntityGraphType.ENTITYCLASS;
            } else {
                return EntityGraphType.SUBSELECT;
            }
        }
        return EntityGraphType.QUERY;
    }

    /**
     * @return returns the fields of this EntityGraph only (excluding children)
     */
    protected Map<String, DbField> getFieldMap() {
        return this.fieldMap;
    }

    /**
     * Graph names get assigned by EntityGraphBuilders and can be used to
     * identify sub-graphs in an EntityGraph by walking the names of child
     * entities.
     *
     * @return the name of this EntityGraph
     */
    public String getGraphName() {
        return this.graphName;
    }

    /**
     * @param context the current context of the method invocation
     * @return returns the precomputed graph path for an EntityGraph object in a
     * certain context.
     */
    public String getGraphPath(String context) {
        return this.graphPathMap.get(context);
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

    protected AttributeType getSubSelectAttribute() {
        return this.subSelectAttribute;
    }

    /**
     * @return the subselect condition
     */
    protected Condition getSubSelectCondition() {
        return this.subSelectCondition;
    }

    /**
     * @return the table name (or a user specified alias for queries)
     */
    protected String getTableName() {
        return this.tableName;
    }

    protected boolean hasAttribute(AttributeType attr) {
        return this.attributeTypes.contains(attr);
    }

    protected boolean hasChildren() {
        return this.children.size() > 0;
    }

    /**
     * check whether an EntityGraph
     */
    protected boolean hasSubGraph(String graphPath) {
        return false;
    }

    protected boolean isEntityClass() {
        return this.entityClass != null;
    }

    private void processAttributeOverride(String parentFieldName, AttributeOverride override) {
        String name = parentFieldName.isEmpty()
                ? override.name()
                : parentFieldName + "." + override.name();

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
     * Process the annotations of a class. Many of the 'more special' Hibernate
     * annotations will be ignored.
     */
    private void processClass() {
        processTable();
        AttributeTag tag = this.entityClass.getAnnotation(AttributeTag.class);
        if (tag != null) {
            addAttributeType(tag.type());
        }
        processFields(this.entityClass, new ArrayList<Field>(3), "", false);
        buildFieldMap();
    }

    /**
     * Process a single field of a class. The field will not be added to the
     * fieldMap, if it is not annotated with <code>@Id</code> or
     * <code>@Column</code>. Fields annotated with <code>@EmbeddedId</code> will
     * be resolved.
     *
     * Note: Annotations <code>@EntityCollection, @OneTo*, @ManyTo*</code> are
     * currently ignored.
     *
     * @param accessors chain of (embedded) fields for accessing all field
     * values (direct and embedded) of an object
     * @param parentFieldName name of the field embedding the class of this
     * field
     * @param field the field
     */
    @SuppressWarnings({"unchecked"})
    private void processColumn(List<Field> accessors, String parentFieldName, Field field) {
        String fieldName = parentFieldName.isEmpty()
                ? field.getName()
                : parentFieldName + "." + field.getName();

        if (field.getAnnotation(EmbeddedId.class) != null) {
            Class<?> clazz = field.getType();

            processFields(clazz, new ArrayList(accessors), fieldName, true);
            return;
        }

        if (field.getAnnotation(Id.class) != null) {
            processId(new ArrayList(accessors), parentFieldName, field);
            return;
        }

        Embedded embedded = field.getAnnotation(Embedded.class);
        if (embedded != null) {
            Class clazz = field.getType();
            processFields(clazz, new ArrayList(accessors), fieldName, false);
            return;
        }

        AttributeTag attributeTag = field.getAnnotation(AttributeTag.class);
        AttributeTags attributeTags = field.getAnnotation(AttributeTags.class);
        String columnName = field.getName();
        Column column = field.getAnnotation(Column.class);
        Basic basic = field.getAnnotation(Basic.class);
        if ((basic != null) || (column != null)) {
            if ((column != null) && (!column.name().isEmpty())) {
                columnName = column.name();
            }
            DbField dbField = new DbField(false,
                    (field.getAnnotation(GeneratedValue.class) != null) ? true : false)
                    .setEntityGraph(this)
                    .setFieldName(fieldName)
                    .setColumnName(columnName)
                    .setTableName(this.tableName)
                    .addAccessors(accessors)
                    .addAttributeTag(attributeTag)
                    .addAttributeTag(attributeTags)
                    .addAttributeTypes(this.attributeTypes);
            this.fieldMap.put(fieldName, dbField);
        }
    }

    /**
     * process the fields of a class to obtain a map of database fields
     *
     * @param clazz the class to resolve the fields for
     * @param parentAccessors a chain of (embedded) fields for accessing all
     * field values (direct and embedded) of an object
     * @param isIndex if the class is an EmbeddedId
     */
    private void processFields(Class<?> clazz, List<Field> parentAccessors, String parentFieldName, boolean isIndex) {
        for (Field field : clazz.getDeclaredFields()) {
            List<Field> accessors = new ArrayList<>(parentAccessors);
            accessors.add(field);
            if (isIndex) {
                processId(accessors, parentFieldName, field);
            } else {
                processColumn(accessors, parentFieldName, field);
            }
            String name = parentFieldName.isEmpty()
                    ? field.getName()
                    : parentFieldName + "." + field.getName();
            processAttributeOverrides(name, field);
        }

        Class<?> superClass = clazz.getSuperclass();
        if ((superClass != null)
                && (superClass.getAnnotation(MappedSuperclass.class) != null)) {
            processFields(superClass, parentAccessors, parentFieldName, isIndex);
        }
        processAttributeOverrides(parentFieldName, clazz);
    }

    /**
     * process an index field; could be either a single field with @Id
     * annotation or a field from an @EmbeddedId object.
     *
     * @param accessors a chain of (embedded) fields for accessing all field
     * values (direct and embedded) of an object
     * @param parentFieldName name of the field embedding the class of this
     * field
     * @param field the index field
     */
    private void processId(List<Field> accessors, String parentFieldName, Field field) {
        String fieldName = parentFieldName.isEmpty()
                ? field.getName()
                : parentFieldName + "." + field.getName();

        String columnName = field.getName();
        Column column = field.getAnnotation(Column.class);
        if ((column != null) && (!column.name().isEmpty())) {
            columnName = column.name();
        }

        AttributeTag attributeTag = field.getAnnotation(AttributeTag.class);
        AttributeTags attributeTags = field.getAnnotation(AttributeTags.class);
        DbField dbField = new DbField(true,
                (field.getAnnotation(GeneratedValue.class) != null) ? true : false)
                .setEntityGraph(this)
                .setFieldName(fieldName)
                .setColumnName(columnName)
                .setTableName(this.tableName)
                .addAccessors(accessors)
                .addAttributeTag(attributeTag)
                .addAttributeTag(attributeTags)
                .addAttributeTypes(this.attributeTypes);
        this.fieldMap.put(fieldName, dbField);
        this.indexCount++;
    }

    /**
     * process <code>@Table</code> annotation for an entity class. If the
     * annotation is missing, the simple name of the class is used.
     */
    private void processTable() {
        Table table = this.entityClass.getAnnotation(Table.class);
        if (table != null) {
            StringBuilder sb = new StringBuilder();
            if ((table.schema() != null) && (!table.schema().isEmpty())) {
                sb.append(table.schema());
                sb.append(".");
            }
            sb.append(table.name());
            this.tableName = sb.toString();
            this.graphName = this.tableName;
            return;
        }
        this.tableName = this.entityClass.getSimpleName();
    }

    protected void reset(String context) {
        this.active.remove(context);
        for (EntityGraph graph : this.children) {
            graph.reset(context);
        }
    }

    /**
     * Select a sub-graph from this EntityGraph matching the given graphPath.
     *
     * @param graphPath the path of graphNames leading to the desired
     * EntityGraph. GraphPaths are generated by joining the individual
     * graphNames of the EntityGraphs with slashes (example: item/material)
     * @return a matching sub-graph or null
     */
    public EntityGraph selectSubGraph(String graphPath) {
        int pos = graphPath.indexOf('/');
        if (pos == -1) {
            if (graphPath.equals(this.graphName)) {
                return this;
            }
            return null;
        }

        if (graphName.equals(graphPath.substring(0, pos)) && hasChildren()) {
            String subGraphPath = graphPath.substring(pos + 1);
            for (EntityGraph graph : children) {
                EntityGraph subGraph = graph.selectSubGraph(subGraphPath);
                if (subGraph != null) {
                    return subGraph;
                }
            }
        }
        return null;
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

    /**
     * Graph names get assigned by EntityGraphBuilders and can be used to
     * identify sub-graphs in an EntityGraph by walking the names of child
     * entities.
     *
     * @return the name of this EntityGraph. Must not contain forward slashes
     * (/).
     */
    public EntityGraph setGraphName(String graphName) {
        this.graphName = graphName;
        return this;
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
     * Restrict sub-selects
     *
     * @param attr Only EntityGraph objects with AttributeType attr will be
     * included in the sub-select
     * @return
     */
    public EntityGraph setSubSelectAttribute(AttributeType attr) {
        this.subSelectAttribute = attr;
        return this;
    }

    /**
     * Enables the transformation of this EntityGraph subtree into a subselect
     * instead of a classical JOIN. This enables the handling of access control
     * conditions within the subselect, making the rest of the conditions much
     * easier.
     *
     * @param con the condition (WHERE clause) for this subtree, usually access
     * control conditions
     * @return this EntityGraph
     */
    public EntityGraph setSubSelectCondition(Condition con) {
        this.subSelectCondition = con;
        return this;
    }

    /**
     * set the table name for query type EntityGraphs to assist in debugging
     *
     * @param tableName
     * @return this EntityGraph
     * @throws UnsupportedOperationException if this is an entity class type
     * EntityGraph object
     */
    public EntityGraph setTableName(String tableName) {
        if (isEntityClass()) {
            throw new UnsupportedOperationException("Illegal attempt to set table name for entity class");
        }
        this.tableName = tableName;
        return this;
    }

    public void printGraphStructure(int level) {

        String levelString = "";
        for (int i = 0; i < level; i++) {
            levelString += "-";
        }
        levelString += " ";
        System.out.println(levelString + graphName+":"+String.join(",", active));
        level++;
        for (EntityGraph subGraph : children) {
            subGraph.printGraphStructure(level);
        }
    }

}
