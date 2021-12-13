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
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
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
    public void test002_createSequenceSql(){
        User user=new User();
        user.setId(100);
        MaterialEntityGraphBuilder materialGraphBuilder=new MaterialEntityGraphBuilder();
        EntityGraph graph=materialGraphBuilder.buildEntityGraph(true);
        SqlParamTableBuilder sqlBuilder = new SqlParamTableBuilder(graph);
         MaterialSearchConditionBuilder conditionBuilder=new MaterialSearchConditionBuilder(graph,"materials");
        MaterialSearchRequestBuilder requestBuilder=new MaterialSearchRequestBuilder(user,0,10);
        String sql= sqlBuilder.query(conditionBuilder.convertRequestToCondition(requestBuilder.build(),ACPermission.permREAD));
        int i=0;
    }
}
