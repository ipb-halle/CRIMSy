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
package de.ipb_halle.lbac.items.search;

import de.ipb_halle.lbac.items.search.ItemEntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemEntityGraphBuilderTest {

    @Test
    public void test001_createEntityGraph() {

        ItemEntityGraphBuilder graphBuilder = new ItemEntityGraphBuilder();
        graphBuilder.addContainer();
        graphBuilder.addMaterialName();
        graphBuilder.addProject();
        graphBuilder.addUser();

        EntityGraph graph = graphBuilder.buildEntityGraph();
        SqlBuilder builder = new SqlBuilder(graph);

        Condition condition1 = new Condition(
                new Attribute(new AttributeType[]{
            AttributeType.ITEM,
            AttributeType.TEXT
        }),
                Operator.EQUAL,
                new Value("Benzol"));

        String sql = builder.query(condition1);
        int i = 0;

    }
}
