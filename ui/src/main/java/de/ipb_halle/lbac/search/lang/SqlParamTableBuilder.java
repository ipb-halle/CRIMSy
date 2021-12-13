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
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

/**
 * Builds a SELECT statement for an EntityGraph using given entity annotations
 * and Conditions. Query parameters are stored in a parameter table.
 * NOTE: The methods of this class are not thread safe.
 *
 * @author fbroda
 */
public class SqlParamTableBuilder extends SqlBuilder {

    protected final static String paramTableNameQuery = "SELECT process_id, parameter FROM tmp_search_parameter";
    private final static String paramTableId = "process_id";
    private final static String paramTableName = "param_table";
    private String processId;
    private EntityGraph paramGraph;

    /**
     * constructor
     */
    public SqlParamTableBuilder(EntityGraph graph) {
        this(graph, false);
    }

    /**
     * Constructor
     *
     * @param graph the EntityGraph to construct a SELECT statement for
     * @param subSelect true if the statement is a sub-select of an outer
     * EntityGraph. In this case, the sub-selects will NOT be nested.
     */
    public SqlParamTableBuilder(EntityGraph graph, boolean subSelect) {
        this(graph, subSelect, UUID.randomUUID().toString());
    }

    private SqlParamTableBuilder(EntityGraph graph, boolean  subSelect, String processId) {
        super(graph, subSelect);
        this.paramGraph = new EntityGraph(paramTableNameQuery)
                .setGraphName(paramTableName)
                .addLinkField(new LinkField("'" + processId + "'", paramTableId, true));
        graph.addChild(paramGraph);
        this.processId = processId;
    }
    /**
     * add a leaf condition with a binary operator
     *
     * @param sb the StringBuilder
     * @param columns the matching columns for this condition
     * @param operator the condition operator
     * @param value the condition value
     */
    @Override
    protected void addBinaryLeafCondition(StringBuilder sb, Set<DbField> columns, Operator operator, Value value) {
        String sep = "";
        for (DbField field : columns) {
            sb.append(sep);
            if (operator.isPrefixOperator()) {
                sb.append(operator.getSql());
                sb.append("(");
                sb.append(field.getAliasedColumnName());
                sb.append(",");
                sb.append(getCastParameter(value));
                sb.append(")");
            } else {
                sb.append(field.getAliasedColumnName());
                sb.append(operator.getSql());
                sb.append(getCastParameter(value));
            }
            sep = " OR ";
        }
    }

    /**
     * Always activate the parameter table
     * @param cond
     * @param context
     */
    @Override
    protected void filter(Condition cond, String context) {
        super.filter(cond, context);
        this.paramGraph.activate(context);
    }

    /**
     * @param value the original condition value object
     * @return a modified cast parameter to use the JSON object
     * from the parameter table
     */
    private String getCastParameter(Value value) {
        
        String  expr = value.getCastExpression();
//        String param = "CAST(parameter->>'" + value.getArgumentKey() + "' AS "+value.getJsonCast()+")";
        String param = value.getCastJsonField("parameter->>'" + value.getArgumentKey() + "'");
        return (expr == null)
                ? param
                : String.format(expr, param);
    };

    public String getProcessId() {
        return this.processId;
    }

    @Override
    protected SqlBuilder getSqlBuilder(EntityGraph child, boolean subSelect) {
        return new SqlParamTableBuilder(child, subSelect, this.processId);
    }

    public JsonObject getValuesAsJson() {
        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        for (Value param : super.getValueList()) {
            JsonElement el = gson.toJsonTree(param.getValue());
            obj.add(param.getArgumentKey(), el);
        }
        return obj;
    }

    @Override
    public List<Value> getValueList() {
        return new ArrayList<> ();
    }
}
