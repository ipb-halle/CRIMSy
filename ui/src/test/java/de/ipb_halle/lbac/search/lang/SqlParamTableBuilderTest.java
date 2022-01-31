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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.base.JsonAssert;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.material.sequence.SequenceEntity;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchConditionBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.persistence.criteria.JoinType;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author fblocal
 */
@ExtendWith(ArquillianExtension.class)
public class SqlParamTableBuilderTest extends TestBase {

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("SqlParameterTableBuilderTest.war");
    }

    /**
     */
    @Test
    public void test1() {

        EntityGraph graph = new EntityGraph(NodeEntity.class);
        SqlParamTableBuilder builder = new SqlParamTableBuilder(graph);

        Condition condition = new Condition(
                new Attribute("nodes", AttributeType.INSTITUTION),
                Operator.ILIKE,
                new Value("%TEST%"));

        String sql = builder.query(condition);
        String processId = builder.getProcessId();
        String json = builder.getValuesAsJson().toString();

        assertTrue("sql contains processId", sql.contains(processId));
        assertTrue("sql contains parameter table query",
                sql.contains(SqlParamTableBuilder.paramTableNameQuery));

        assertTrue("JSON contains field 'field0'", json.contains("field0"));
        assertTrue("JSON contains test value '%TEST%'", json.contains("%TEST%"));
    }

    @Test
    public void test002_createSequenceSql() {
        String expectedJson = "SELECT DISTINCT a.id, a.sequenceString, a.annotations, a.circular, a.sequenceType \n"
                + "FROM sequences AS a\n"
                + " JOIN materials AS a_0 ON a.id = a_0.materialid\n"
                + " LEFT JOIN material_compositions AS a_0_0 ON a_0.materialid = a_0_0.materialid\n"
                + " LEFT JOIN  ( SELECT DISTINCT sub_a_0_0_0.aclist_id, sub_a_0_0_0.owner_id, sub_a_0_0_0.ctime, sub_a_0_0_0.materialtypeid, sub_a_0_0_0.materialid, sub_a_0_0_0.projectid, sub_a_0_0_0.deactivated \n"
                + "FROM materials AS sub_a_0_0_0\n"
                + " JOIN ACENTRIES AS sub_a_0_0_0_5 ON sub_a_0_0_0.aclist_id = sub_a_0_0_0_5.aclist_id\n"
                + " JOIN MEMBERSHIPS AS sub_a_0_0_0_5_0 ON sub_a_0_0_0_5.member_id = sub_a_0_0_0_5_0.group_id\n"
                + " JOIN  ( SELECT processid, parameter FROM temp_search_parameter )  AS sub_a_0_0_0_6 ON '%s' = sub_a_0_0_0_6.processid \n"
                + "WHERE ((((sub_a_0_0_0.owner_id = CAST(sub_a_0_0_0_6.parameter->>'field0' AS INTEGER)) AND (sub_a_0_0_0_5.member_id = CAST(sub_a_0_0_0_6.parameter->>'field1' AS INTEGER))) OR (sub_a_0_0_0_5_0.member_id = CAST(sub_a_0_0_0_6.parameter->>'field2' AS INTEGER))) AND (sub_a_0_0_0_5.permREAD IS TRUE)) )  AS a_0_0_0 ON a_0_0.componentid = a_0_0_0.materialid\n"
                + " LEFT JOIN sequences AS a_0_0_0_3 ON a_0_0_0.materialid = a_0_0_0_3.id\n"
                + " JOIN ACENTRIES AS a_0_1 ON a_0.aclist_id = a_0_1.aclist_id\n"
                + " JOIN MEMBERSHIPS AS a_0_1_0 ON a_0_1.member_id = a_0_1_0.group_id\n"
                + " JOIN  ( SELECT processid, parameter FROM temp_search_parameter )  AS a_1 ON '%s' = a_1.processid \n"
                + "WHERE ((a_0.materialtypeid IN (SELECT jsonb_array_elements_text(a_1.parameter->'field3')::INTEGER)) AND (a_0_0_0_3.sequenceType = CAST(a_1.parameter->>'field4' AS VARCHAR)) AND ((((a_0.owner_id = CAST(a_1.parameter->>'field5' AS INTEGER)) AND (a_0_1.member_id = CAST(a_1.parameter->>'field6' AS INTEGER))) OR (a_0_1_0.member_id = CAST(a_1.parameter->>'field7' AS INTEGER))) AND (a_0_1.permREAD IS TRUE)))";

        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        requestBuilder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA, 1);
        requestBuilder.addMaterialType(MaterialType.SEQUENCE);

        EntityGraph sequenceGraph = new EntityGraph(SequenceEntity.class);
        sequenceGraph.addAttributeType(AttributeType.TOPLEVEL);
        sequenceGraph.addAttributeType(AttributeType.DIRECT);
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();

        EntityGraph materialGraph = graphBuilder.buildEntityGraph(false);

        materialGraph.addLinkField("id", "materialid").setJoinType(JoinType.INNER);
        sequenceGraph.addChildInherit(materialGraph);

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(sequenceGraph, "sequences/materials");
        Condition condition = builder.convertRequestToCondition(requestBuilder.build(), ACPermission.permREAD);
        SqlParamTableBuilder sqlBuilder = new SqlParamTableBuilder(sequenceGraph);
        String sql = sqlBuilder.query(condition);
        Assert.assertEquals(String.format(expectedJson, sqlBuilder.getProcessId(), sqlBuilder.getProcessId()), sql);

    }

    @Test
    public void testJsonParameter() {

        String expectedJson = "{\"field1\":5,"
                + "\"field2\":\"Hallo Welt\","
                + "\"field3\":9876543210,"
                + "\"field4\":true,"
                + "\"field5\":1.234,"
                + "\"field6\":1.234,"
                + "\"field7\":%d,"
                + "\"field8\":[1,2,3],"
                + "\"field9\":[1,2,3],"
                + "\"field10\":[\"fip\",\"fap\",\"fup\"],"
                + "\"field11\":[\"fup\",\"fap\",\"fip\"],"
                + "\"field12\":\"OTHER\","
                + "\"field13\":0,"
                + "\"field14\":\"19960117-c899-4098-85d3-7b04889f2d67\"}";
        EntityGraph graph = new EntityGraph(NodeEntity.class);
        SqlParamTableBuilder builder = new SqlParamTableBuilder(graph);

        List<Value> list = new ArrayList<>();
        list.add(new Value(5).setArgumentKey("field1"));
        list.add(new Value("Hallo Welt").setArgumentKey("field2"));
        list.add(new Value(9876543210l).setArgumentKey("field3"));
        list.add(new Value(true).setArgumentKey("field4"));
        list.add(new Value(1.234d).setArgumentKey("field5"));
        list.add(new Value(1.234f).setArgumentKey("field6"));

        Date date = new Date();

        list.add(new Value(date).setArgumentKey("field7"));

        Integer[] intArray = {1, 2, 3};
        list.add(new Value(Arrays.asList(intArray)).setArgumentKey("field8"));
        list.add(new Value(new HashSet<>(Arrays.asList(intArray))).setArgumentKey("field9"));

        String[] stringArray = {"fip", "fap", "fup"};
        list.add(new Value(Arrays.asList(stringArray)).setArgumentKey("field10"));
        list.add(new Value(new HashSet<>(Arrays.asList(stringArray))).setArgumentKey("field11"));

        list.add(new Value(TopicCategory.OTHER).setArgumentKey("field12"));
        list.add(new Value(AdmissionSubSystemType.BUILTIN).setArgumentKey("field13"));
        UUID uuid = UUID.fromString("19960117-c899-4098-85d3-7b04889f2d67");
        list.add(new Value(uuid).setArgumentKey("field14"));

        builder.valueList = list;
        String json = builder.getValuesAsJson().toString();
        JsonAssert.assertJsonEquals(String.format(expectedJson, date.getTime()), json);

    }
}
