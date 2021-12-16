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
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
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
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author fblocal
 */
@RunWith(Arquillian.class)
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

        int i = 0;
    }

    @Test
    public void testJsonParameter() {
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
        list.add(new Value(UUID.randomUUID()).setArgumentKey("field14"));

        builder.valueList = list;
        String json = builder.getValuesAsJson().toString();
        assertTrue("JSON Object build correctly", false);
    }
}
