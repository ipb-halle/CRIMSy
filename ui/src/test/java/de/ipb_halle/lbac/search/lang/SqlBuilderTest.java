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
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * This class will provide some test cases for the NodeService class.
 */
@RunWith(Arquillian.class)
public class SqlBuilderTest extends TestBase {


    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("SqlBuilderTest.war");
    }
    
   
    /**
     */
    @Test
    public void testSqlBuilder() {

        EntityGraph graph = new EntityGraph(NodeEntity.class);
        SqlBuilder builder = new SqlBuilder(graph);

        Condition condition = new Condition(
            new Attribute(AttributeType.INSTITUTION), 
            Operator.ILIKE,
            new Value("%TEST%"));

        assertNotSame("fake assertion!", builder.query(condition));
    }

}
