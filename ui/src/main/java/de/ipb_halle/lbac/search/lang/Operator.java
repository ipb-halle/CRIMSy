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

/**
 * Operators for search conditions. Operators possess certain features;
 * they may be
 * <ul>
 * <li>unary or binary (ternary operators are currently not supported).</li>
 * <li>leaf operators or operators for condition nodes</li>
 * <li>prefix operators or postfix operators (applies only to unary operators)</li>
 * </ul>
 *
 * @author fmauz
 */
public enum Operator {
    
    AND(" AND ", false, false),
    OR(" OR ",   false, false),
    NOT(" NOT ", false, true, true),

    IS_NULL(" IS NULL", true, true),
    IS_NOT_NULL(" IS NOT NULL", true, true),
    LESS(" < "),
    LESS_EQUAL(" <= "),
    EQUAL(" = "),
    GREATER_EQUAL(" >= "),
    GREATER(" = "),
    LIKE(" LIKE "),
    ILIKE(" ILIKE "),
    SUBSTRUCTURE(" >= ");

    private boolean leafOperator;
    private boolean prefixOperator;
    private boolean unary;
    private String sql;

    private Operator(String sql, boolean leafOperator, boolean unary) {
            this(sql, leafOperator, unary, false);
    }

    private Operator(String sql, boolean leafOperator, boolean unary, boolean prefixOperator) {
        this.leafOperator = leafOperator;
        this.prefixOperator = prefixOperator;
        this.unary = unary;
        this.sql = sql;
    }

    private Operator(String sql) {
        this.leafOperator = true;
        this.prefixOperator = false; 
        this.unary = false;
        this.sql = sql;
    }

    public boolean isLeafOperator() {
        return this.leafOperator;
    }

    public boolean isPrefixOperator() {
        return this.prefixOperator;
    }

    public boolean isPostfixOperator() {
        return ! this.prefixOperator;
    }

    public boolean isUnary() {
        return this.unary;
    }

    public String getSql() {
        return this.sql;        
    }
}
