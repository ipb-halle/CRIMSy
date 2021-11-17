/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author flange
 */
@RunWith(Arquillian.class)
public class SearchParameterServiceTest extends TestBase {

    UUID processId;
    UUID processId2;

    private final String SQL_LOAD_PARAMETER = "SELECT id,cdate,processid,field,value FROM temp_search_parameter WHERE processid=':processid' ORDER BY field";
    @Inject
    SearchParameterService searchParameter;
    private static final long serialVersionUID = 1L;

    @Test
    public void test001_saveLoad() {
        createAndSaveParameter();

        checkParameterOfProcess1();
        checkParameterOfProcess2();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test002_removeParameter() {
        createAndSaveParameter();

        searchParameter.removeParameter(processId);
        List parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId.toString()));
        Assert.assertTrue(parameter.isEmpty());
        parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId2.toString()));
        Assert.assertEquals(1, parameter.size());

    }

    @SuppressWarnings("unchecked")
    private void checkParameterOfProcess2() {
        List<Object[]> parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId2.toString()));
        Assert.assertEquals(1, parameter.size());
        Assert.assertEquals(":field1", (String) parameter.get(0)[3]);
        Assert.assertEquals("value3", (String) parameter.get(0)[4]);
    }

    @SuppressWarnings("unchecked")
    private void checkParameterOfProcess1() {
        List<Object[]> parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId.toString()));
        Assert.assertEquals(2, parameter.size());
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                Assert.assertEquals(":field0", (String) parameter.get(i)[3]);
                Assert.assertEquals("value", (String) parameter.get(i)[4]);
            } else {
                Assert.assertEquals(":field1", (String) parameter.get(i)[3]);
                Assert.assertEquals("value2", (String) parameter.get(i)[4]);
            }
        }
    }

    private void createAndSaveParameter() {
        processId = UUID.randomUUID();
        processId2 = UUID.randomUUID();
        searchParameter.saveParameter(processId, ":field0", "value");
        searchParameter.saveParameter(processId, ":field1", "value2");
        searchParameter.saveParameter(processId2, ":field1", "value3");
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SearchParameterEntityTest.war");
        deployment = UserBeanDeployment.add(deployment);
        deployment.addClass(SearchParameterService.class);

        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
