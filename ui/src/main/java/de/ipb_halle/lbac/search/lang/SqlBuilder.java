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
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Builds a SELECT statement for an EntityGraph using given entity annotations
 * and Conditions. NOTE: The methods of this class are not thread safe.
 *
 * @author fbroda
 */
public class SqlBuilder {

    protected EntityGraph entityGraph;
    private List<Value> valueList;
    private Set<DbField> allFields;
    private int argumentCounter;
    private boolean subSelect;

    /**
     * constructor
     */
    public SqlBuilder(EntityGraph graph) {
        this(graph, false);
    }

    /**
     * Constructor
     *
     * @param graph the EntityGraph to construct a SELECT statement for
     * @param subSelect true if the statement is a sub-select of an outer
     * EntityGraph. In this case, the sub-selects will NOT be nested.
     */
    public SqlBuilder(EntityGraph graph, boolean subSelect) {
        this.entityGraph = graph;
        this.subSelect = subSelect;
        this.argumentCounter = 0;
    }

    /**
     * add a condition
     *
     * @param sb the StringBuilder
     * @param condition the condition
     */
    private void addCondition(StringBuilder sb, String context, Condition condition) {
        sb.append("(");
        if (condition.isLeaf()) {
            addLeafCondition(sb, context, condition);
        } else {

            Operator operator = condition.getOperator();
            if (operator.isUnary()) {
                if (operator.isPrefixOperator()) {
                    sb.append(operator.getSql());
                }
                addCondition(sb, context, condition.getLeftCondition());
                if (operator.isPostfixOperator()) {
                    sb.append(operator.getSql());
                }
            } else {
                int i = 0;
                for (Condition c : condition.getConditions()) {
                    if (i > 0) {
                        sb.append(operator.getSql());
                    }
                    addCondition(sb, context, c);
                    i++;
                }
            }
        }
        sb.append(")");
    }

    /**
     * add a leaf condition considering all matching columns. Depending on the
     * type of condition (not single argument) this will update
     * <code>argumentCounter</code> and <code>valueList</code>.
     *
     * @param sb the StringBuilder
     * @param condition the leaf condition
     * @throws NoSuchElementException if the condition attributes can not be
     * fulfilled by the current EntityGraph
     */
    private void addLeafCondition(StringBuilder sb, String context, Condition condition) {
        Operator operator = condition.getOperator();
        Set<DbField> columns = getMatchingColumns(context, condition.getAttribute());
        if (columns.size() == 0) {
            String s = "";
            for (AttributeType t : condition.getAttribute().getTypes()) {
                s += t + ":";
            }

            throw new NoSuchElementException(
                    "No matching field found in addLeafCondition(): "
                    + s + condition.getAttribute().getGraphPath());
        }
        if (operator.isUnary()) {
            addUnaryLeafCondition(sb, columns, operator);
        } else {
            Value value = condition.getValue();
            String argumentKey = "field" + String.valueOf(this.argumentCounter);
            value.setArgumentKey(argumentKey);
            this.argumentCounter++;
            this.valueList.add(value);
            addBinaryLeafCondition(sb, columns, operator, value);
        }
    }

    /**
     * add a leaf condition with a binary operator
     *
     * @param sb the StringBuilder
     * @param columns the matching columns for this condition
     * @param operator the condition operator
     * @param value the condition value
     */
    private void addBinaryLeafCondition(StringBuilder sb, Set<DbField> columns, Operator operator, Value value) {
        String sep = "";
        for (DbField field : columns) {
            sb.append(sep);
            if (operator.isPrefixOperator()) {
                sb.append(operator.getSql());
                sb.append("(");
                sb.append(field.getAliasedColumnName());
                sb.append(",");
                sb.append(value.getCastArgument());
                sb.append(")");
            } else {
                sb.append(field.getAliasedColumnName());
                sb.append(operator.getSql());
                sb.append(value.getCastArgument());
            }
            sep = " OR ";
        }
    }

    /**
     * add a leaf condition with an unary operator
     *
     * @param sb the StringBuilder
     * @param columns the matching columns for this condition
     * @param operator the condition operator
     */
    private void addUnaryLeafCondition(StringBuilder sb, Set<DbField> columns, Operator operator) {
        String sep = "";
        for (DbField field : columns) {
            sb.append(sep);
            if (operator.isPrefixOperator()) {
                sb.append(operator.getSql());
            }
            sb.append(field.getAliasedColumnName());
            if (operator.isPostfixOperator()) {
                sb.append(operator.getSql());
            }
            sep = " OR ";
        }
    }

    /**
     * mark entity subgraphs active based on the conditions
     *
     * @param condition the conditions
     * @param context the activation context
     */
    private void filter(Condition condition, String context) {
        List<Attribute> attributes = new ArrayList<>();
        if (condition != null) {
            condition.getAttributes(attributes);
        }
        for (Attribute attr : attributes) {
            for (DbField field : getMatchingColumns(context, attr)) {
                field.getEntityGraph().activate(context);
            }
        }
    }

    /**
     * mark entity subgraphs active based on the items in the ORDER BY clause.
     *
     * @param orderList a list of fields defining the ORDER BY clause
     * @param context the activation context
     */
    private void filter(List<DbField> orderList, String context) {
        if (orderList == null) {
            return;
        }
        for (DbField field : orderList) {
            getOrderColumn(field).getEntityGraph().activate(context);
        }
    }

    /**
     * @param context the current query context
     * @param attr In case of sub-selects: the AttributeType to which the SQL
     * statement should be restricted, otherwise null
     * @return the FROM clause defined by this EntityGraph for a SELECT
     * statement.
     */
    protected String from(String context, AttributeType attr) {
        StringBuilder sb = new StringBuilder();

        sb.append(" \nFROM ");
        if (this.entityGraph.isEntityClass()) {
            sb.append(this.entityGraph.getTableName());
        } else {
            sb.append("( ");
            sb.append(this.entityGraph.getQuery());
            sb.append(") ");
        }
        sb.append(" AS ");
        sb.append(this.entityGraph.getAlias());

        if (this.entityGraph.hasChildren()) {
            sb.append(joinChildren(this.entityGraph, context, attr));
        }

        return sb.toString();
    }

    private int getArgumentCounter() {
        return this.argumentCounter;
    }

    /**
     * return the appropriate JOIN keyword (i.e. "LEFT JOIN", "JOIN", "RIGHT
     * JOIN") for a join operation
     *
     * @param graph the EntityGraph object
     * @return the JOIN keyword(s)
     * @throws IllegalStateException if joinType doesn't match one of the known
     * join types
     */
    private String getJoinType(EntityGraph graph) {
        switch (graph.getJoinType()) {
            case INNER:
                return " JOIN ";
            case LEFT:
                return " LEFT JOIN ";
            case RIGHT:
                return " RIGHT JOIN ";
        }
        throw new IllegalStateException("Encountered illegal JoinType");
    }

    /**
     * obtain a set of columns, which matches the given attribute
     *
     * @param attribute the attribute to filter the fields (columns) against
     * @return a set of matching columns
     */
    private Set<DbField> getMatchingColumns(String context, Attribute attribute) {
        Set<DbField> fields = new HashSet<>();
        for (DbField field : this.allFields) {
            if (field.matches(context, attribute)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private DbField getOrderColumn(DbField field) {
        for (DbField f : this.allFields) {
            if (f.matchesOrder(field)) {
                return f;
            }
        }
        throw new NoSuchElementException("No matching field found in getOrderColumn()");
    }

    /**
     * return the list of Values with argument keys set
     *
     * @return a list of Value objects
     */
    public List<Value> getValueList() {
        return this.valueList;
    }

    /**
     * Recursively add a child subtree to the SQL statement.
     *
     * @param sb the StringBuilder for the SQL statement
     * @param graph the parent EntityGraph
     * @param child the child EntityGraph (may be a tree structure)
     * @param attr restrict the sub-select to EntityGraph objects with attribute
     * <code>attr</code>
     */
    private void joinChild(StringBuilder statement, EntityGraph graph, EntityGraph child, String context, AttributeType attr) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(getJoinType(child));
        switch (child.getEntityGraphType()) {
            case ENTITYCLASS:
                sb.append(child.getTableName());
                break;
            case QUERY:
                sb.append(" ( ");
                sb.append(child.getQuery());
                sb.append(" ) ");
                break;
            case SUBSELECT:
                if (this.subSelect) {
                    // do not nest sub-selects
                    return;
                }
                sb.append(" ( ");
                String tmpAlias = child.getAlias();
                SqlBuilder builder = new SqlBuilder(child, true);
                builder.setArgumentCounter(this.argumentCounter);
                sb.append(builder.query(
                        "sub_" + tmpAlias,
                        child.getSubSelectCondition(),
                        null, child.getSubSelectAttribute()));
                this.argumentCounter = builder.getArgumentCounter();
                sb.append(" ) ");
                child.setAlias(tmpAlias);
                this.valueList.addAll(builder.getValueList());
                break;
        }
        sb.append(" AS ");
        sb.append(child.getAlias());

        sb.append(" ON ");
        sb.append(joinCondition(graph, child));

        if (child.hasChildren()) {
            sb.append(joinChildren(child, context, attr));
        }
        statement.append(sb.toString());
    }

    /**
     * add joins of dependent (child) entities
     *
     * @param graph the entity graph
     * @param context
     * @param attr
     * @return the SQL JOIN expression
     */
    private String joinChildren(EntityGraph graph, String context, AttributeType attr) {
        StringBuilder sb = new StringBuilder();
        for (EntityGraph child : graph.getChildren()) {
            if (child.getActive(context)) {
                if ((attr == null) || child.hasAttribute(attr)) {
                    joinChild(sb, graph, child, context, attr);
                }
            }
        }
        return sb.toString();
    }

    /**
     * add a join condition between this instance and another EntityGraph.
     *
     * @param parent the parent EntityGraph
     * @param child the child EntityGraph, providing the link between the two
     * tables
     * @return
     * <code>parentAlias.linkParent_0 = childAlias.linkChild_0 [ AND parentAlias.linkParent_1 = childAlias.linkChild_1 ...]</code>
     * @throws NoSuchElementException if either parent or child do not contain a
     * requested link column
     */
    protected String joinCondition(EntityGraph parent, EntityGraph child) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (LinkField link : child.getLinks()) {
            if (!parent.containsColumn(link.getParent())) {
                throw new NoSuchElementException(String.format("Column '%s'does not exist in parent table %s.%s",
                        link.getParent(),
                        parent.getAlias(),
                        parent.getTableName()));
            }
            if (!child.containsColumn(link.getChild())) {
                throw new NoSuchElementException(String.format("Column '%s'does not exist in parent table %s.%s",
                        link.getChild(),
                        child.getAlias(),
                        child.getTableName()));
            }
            sb.append(sep);
            sb.append(parent.getAlias());
            sb.append(".");
            sb.append(link.getParent());
            sb.append(" = ");
            sb.append(child.getAlias());
            sb.append(".");
            sb.append(link.getChild());
            sep = " AND ";
        }
        return sb.toString();
    }

    public String order(List<DbField> orderList) {
        if ((orderList == null) || (orderList.size() == 0)) {
            return "";
        }
        String sep = "";
        StringBuilder sb = new StringBuilder();
        sb.append(" \nORDER BY ");
        for (DbField field : orderList) {
            sb.append(sep);
            sb.append(getOrderColumn(field).getAliasedColumnName());
            switch (field.getOrderDirection()) {
                case ASC:
                    sb.append(" ASC ");
                    break;
                case DESC:
                    sb.append(" DESC ");
                    break;
            }
            sep = ", ";
        }
        return sb.toString();
    }

    public String query(Condition condition) {
        return query("a", condition, null, null);
    }

    public String query(Condition condition, List<DbField> orderList) {
        return query("a", condition, orderList, null);
    }

    public String query(String alias, Condition condition, List<DbField> orderList, AttributeType attr) {
        this.valueList = new ArrayList<>();
        this.entityGraph.reset(alias);
        this.entityGraph.setAlias(alias);
        this.entityGraph.computeGraphPath(alias, "");
        this.allFields = this.entityGraph.getAllFields();
        filter(condition, alias);
        filter(orderList, alias);
        StringBuilder sb = new StringBuilder();
        sb.append(select(alias));
        sb.append(from(alias, attr));
        sb.append(where(alias, condition));
        sb.append(order(orderList));
        return sb.toString();
    }

    /**
     * @param context the current context, i.e. the alias of the current select
     * or sub-select statement.
     * @return 'SELECT ' and the column expressions for a SELECT statement
     */
    protected String select(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT ");

        String sep = "";
        for (DbField field : this.entityGraph.getFieldMap().values()) {
            sb.append(sep);
            sb.append(context);
            sb.append(".");
            sb.append(field.getColumnName()); // slightly more efficient than computing "alias.columName" in DbField
            sep = ", ";
        }
        return sb.toString();
    }

    public void setArgumentCounter(int argumentCounter) {
        this.argumentCounter = argumentCounter;
    }

    /**
     * @param context
     * @param condition
     * @return the complete WHERE clause of a query
     */
    protected String where(String context, Condition condition) {
        if (condition == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" \nWHERE ");
        addCondition(sb, context, condition);
        return sb.toString();
    }
}
