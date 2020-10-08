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
 * ToDo: - ORDER clause
 *
 * @author fbroda
 */
public class SqlBuilder {

    protected EntityGraph entityGraph;
    private List<Value> valueList;
    private int argumentCounter;

    /**
     * constructor
     */
    public SqlBuilder(EntityGraph graph) {
        this.entityGraph = graph;
    }

    /**
     * add a condition
     *
     * @param sb the StringBuilder
     * @param condition the condition
     */
    private void addCondition(StringBuilder sb, Condition condition) {
        sb.append("(");
        if (condition.isLeaf()) {
            addLeafCondition(sb, condition);
        } else {

            Operator operator = condition.getOperator();
            if (operator.isUnary()) {
                if (operator.isPrefixOperator()) {
                    sb.append(operator.getSql());
                }
                addCondition(sb, condition.getLeftCondition());
                if (operator.isPostfixOperator()) {
                    sb.append(operator.getSql());
                }
            } else {
                addCondition(sb, condition.getLeftCondition());
                sb.append(operator.getSql());
                addCondition(sb, condition.getRightCondition());
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
    private void addLeafCondition(StringBuilder sb, Condition condition) {
        Operator operator = condition.getOperator();
        Set<DbField> columns = getMatchingColumns(condition.getAttribute());
        if (columns.size() == 0) {
            throw new NoSuchElementException("No matching field found in addLeafCondition()");
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
            sb.append(field.getAliasedColumnName());
            sb.append(operator.getSql());
            sb.append(value.getCastArgument());
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
     * @return the FROM clause defined by this EntityGraph for a SELECT
     * statement.
     */
    protected String from(String alias) {
        StringBuilder sb = new StringBuilder();

        this.entityGraph.setAlias(alias);

        sb.append("FROM ");
        if (this.entityGraph.isEntityClass()) {
            sb.append(this.entityGraph.getTableName());
        } else {
            sb.append("( ");
            sb.append(this.entityGraph.getQuery());
            sb.append(") ");
        }
        sb.append(" AS ");
        sb.append(alias);

        if (this.entityGraph.hasChildren()) {
            sb.append(joinChildren(this.entityGraph, alias));
        }

        return sb.toString();
    }

    /**
     * obtain a set of columns, which matches the given attribute
     *
     * @param attribute the attribute to filter the fields (columns) against
     * @return a set of matching columns
     */
    private Set<DbField> getMatchingColumns(Attribute attribute) {
        Set<DbField> fields = new HashSet<>();
        for (DbField field : this.entityGraph.getAllFields()) {
            if (field.matches(attribute)) {
                fields.add(field);
            }
        }
        return fields;
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
     * return the list of Values with argument keys set
     *
     * @return a list of Value objects
     */
    public List<Value> getValueList() {
        return this.valueList;
    }

    /**
     * add joins of dependent (child) entities
     *
     * @param graph the entity graph
     * @param alias the SQL alias for this entity
     * @return the SQL JOIN expression
     */
    private String joinChildren(EntityGraph graph, String alias) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (EntityGraph eg : graph.getChildren()) {
            String newAlias = alias + "_" + String.valueOf(i);
            eg.setAlias(newAlias);
            sb.append(getJoinType(eg));
            if (eg.isEntityClass()) {
                sb.append(eg.getTableName());
            } else {
                sb.append(" ( ");
                sb.append(eg.getQuery());
                sb.append(" ) ");
            }
            sb.append(" AS ");
            sb.append(newAlias);

            sb.append(" ON ");
            sb.append(joinCondition(graph, eg, alias, newAlias));

            if (eg.hasChildren()) {
                sb.append(joinChildren(eg, newAlias));
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * add a join condition between this instance and another EntityGraph.
     *
     * @param parent the parent EntityGraph
     * @param child the child EntityGraph, providing the link between the two
     * tables
     * @param alias table alias for this instance
     * @param newAlias table alias for the other instance
     * @return
     * <code>alias.linkParent_0 = newAlias.linkChild_0 [ AND alias.linkParent_1 = newAlias.linkChild_1 ...]</code>
     * @throws NoSuchElementException if either parent or child do not contain a
     * requested link column
     */
    protected String joinCondition(EntityGraph parent, EntityGraph child, String alias, String newAlias) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (LinkField link : child.getLinks()) {
            if (!parent.containsColumn(link.getParent())) {
                throw new NoSuchElementException(String.format("Column '%s'does not exist in parent table %s.%s",
                        link.getParent(),
                        alias,
                        parent.getTableName()));
            }
            if (!child.containsColumn(link.getChild())) {
                throw new NoSuchElementException(String.format("Column '%s'does not exist in parent table %s.%s",
                        link.getChild(),
                        newAlias,
                        child.getTableName()));
            }
            sb.append(sep);
            sb.append(alias);
            sb.append(".");
            sb.append(link.getParent());
            sb.append(" = ");
            sb.append(newAlias);
            sb.append(".");
            sb.append(link.getChild());
            sep = " AND ";
        }
        sb.append(" ");
        return sb.toString();
    }

    public String query(Condition condition) {
        return query("a", condition);
    }

    public String query(String alias, Condition condition) {
        this.argumentCounter = 0;
        this.valueList = new ArrayList<>();
        return select(alias)
                + from(alias)
                + where(condition);
    }

    /**
     * @return 'SELECT ' and the column expressions for a SELECT statement
     */
    protected String select(String alias) {
        StringBuilder sb = new StringBuilder();
        this.entityGraph.setAlias(alias);
        sb.append("SELECT DISTINCT ");

        String sep = "";
        for (DbField field : this.entityGraph.getFieldMap().values()) {
            sb.append(sep);
            sb.append(alias);
            sb.append(".");
            sb.append(field.getColumnName()); // slightly more efficient than computing "alias.columName" in DbField
            sep = ", ";
        }
        sb.append(" ");
        return sb.toString();
    }

    /**
     * @return the complete WHERE clause of a query
     */
    protected String where(Condition condition) {
        if (condition == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ");
        addCondition(sb, condition);
        return sb.toString();
    }
}
