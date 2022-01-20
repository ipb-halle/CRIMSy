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

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the NodeService class.
 */
@ExtendWith(ArquillianExtension.class)
public class SqlBuilderTest extends TestBase {

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("SqlBuilderTest.war");
    }

    /**
     */
    @Test
    public void testSqlBuilder_1() {

        EntityGraph graph = new EntityGraph(NodeEntity.class);
        SqlBuilder builder = new SqlBuilder(graph);

        Condition condition = new Condition(
                new Attribute("nodes", AttributeType.INSTITUTION),
                Operator.ILIKE,
                new Value("%TEST%"));

        builder.query(condition);
        assertEquals("Invalid argument list size", 1, builder.getValueList().size());
        Value v = builder.getValueList().get(0);
        assertEquals("Invalid argument key", "field0", v.getArgumentKey());
        assertEquals("Invalid argument value", "%TEST%", v.getValue());
    }

    @Test
    public void testSqlBuilder_2() {

        EntityGraph graph = new EntityGraph(MaterialEntity.class)
                .addChild(new EntityGraph(MaterialIndexEntryEntity.class)
                        .addLinkField("materialid", "materialid"));
        SqlBuilder builder = new SqlBuilder(graph);

        Condition condition = new Condition(
                new Attribute("materials/material_indices", AttributeType.TEXT),
                Operator.EQUAL,
                new Value("Benzol"));

        String query = builder.query(condition).replace("\n", "");
        assertTrue("Query contains expected keywords",
                query.startsWith("SELECT ")
                && query.contains(" FROM ")
                && query.contains(" AS a JOIN ")
                && query.contains(" ON a.materialid = a_0.materialid ")
                && query.contains(" WHERE "));
    }

    @Test
    public void testSqlBuilder_3() {

        EntityGraph graph = new EntityGraph(ItemEntity.class)
                .addChild(new EntityGraph(MaterialIndexEntryEntity.class)
                        .addLinkField("materialid", "materialid"));
        SqlBuilder builder = new SqlBuilder(graph);

        Condition condition1 = new Condition(
                new Attribute("items/material_indices", AttributeType.TEXT),
                Operator.EQUAL,
                new Value("Benzol"));

        Condition condition2 = new Condition(
                new Attribute("items", AttributeType.TEXT),
                Operator.EQUAL,
                new Value("HPLC Grade"));

        Condition condition3 = new Condition(
                Operator.AND,
                condition1,
                condition2
        );

//        assertNotSame("fake assertion!", builder.query(condition3));
        builder.query(condition3);
        assertEquals("Invalid argument list size", 2, builder.getValueList().size());
    }

    @Test
    public void testSqlCountBuilder() {
        EntityGraph graph = new EntityGraph(ItemEntity.class)
                .addChild(new EntityGraph(MaterialEntity.class)
                        .addLinkField("materialid", "materialid"));

        graph.addAttributeType(AttributeType.TOPLEVEL);
        SqlCountBuilder countBuilder = new SqlCountBuilder(
                graph,
                new Attribute("items", new AttributeType[] {AttributeType.TOPLEVEL, AttributeType.BARCODE}));

        // needs condition, otherwise JOIN gets removed
        String sql = countBuilder.query(
            new Condition(new Attribute("items/materials", AttributeType.BARCODE),
                Operator.EQUAL,
                new Value(Integer.valueOf(1))
            )
        ).replace("\n", "");
        Assert.assertEquals("SELECT COUNT( DISTINCT a.label) FROM items AS a JOIN materials AS a_0 ON a.materialid = a_0.materialid WHERE (a_0.materialid = :field0)", sql);

    }

    @Test
    public void testSqlBuilder_4() {
        EntityGraph graph = new EntityGraph(HorrorEntity.class);
        Map<String, DbField> map = graph.getFieldMap();
        Set<String> columnNames = new HashSet<>(
                Arrays.asList(new String[]{"aclist_id", "anzahl",
            "besitzer_id", "foobar", "id", "ort", "strasse"}));
        assertEquals("Set of column names does not match", columnNames, map.keySet());

        SqlBuilder builder = new SqlBuilder(graph);
        Condition condition = new Condition(new Attribute(AttributeType.TEXT), Operator.IS_NOT_NULL);
        String query = builder.query(condition);
        assertTrue("Field order matches", query.startsWith("SELECT DISTINCT a.id, a.anzahl, a.foobar, "));
        assertTrue("Condition contains", query.contains("WHERE (a.foobar IS NOT NULL)"));

    }

    @Test
    public void testSqlBuilder_5() {
        String raw_query = "SELECT fip, fap, fum FROM foobar";
        EntityGraph graph = new EntityGraph(raw_query);
        graph
                .setTableName("foobar")
                .setGraphName("foobar")
                .addField(new DbField(false, false)
                        .addAttributeType(AttributeType.TEXT)
                        .setEntityGraph(graph)
                        .setColumnName("fip")
                        .setTableName("foobar"))
                .addField(new DbField(false, false)
                        .setEntityGraph(graph)
                        .setColumnName("fap")
                        .setTableName("foobar"));


        Condition condition = new Condition(new Attribute("foobar", AttributeType.TEXT),
                Operator.IS_NOT_NULL);
        String query = new SqlBuilder(graph).query(condition);
        raw_query = "SELECT DISTINCT * \nFROM ( "
                + raw_query
                + ")  AS a";
//        assertEquals("Query matches", raw_query, query);
        assertTrue("Query matches", query.startsWith(raw_query));
    }
}
