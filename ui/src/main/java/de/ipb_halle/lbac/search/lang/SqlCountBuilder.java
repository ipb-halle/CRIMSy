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
 *
 * @author fmauz
 */
public class SqlCountBuilder extends SqlBuilder {

    Attribute attributeToCount;

    public SqlCountBuilder(EntityGraph graph, Attribute attributeToCount) {
        super(graph);
        this.attributeToCount = attributeToCount;
    }

    @Override
    protected String select(String context) {
        for (DbField field : entityGraph.getAllFields()) {
            if (field.matches(context, attributeToCount)) {
                return String.format("SELECT COUNT( DISTINCT %s)", field.getAliasedColumnName());
            }
        }
        throw new RuntimeException("Could not fond dbfield for attribute");
    }

}
