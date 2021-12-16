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
package de.ipb_halle.lbac.material.sequence.search.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import static de.ipb_halle.lbac.base.JsonAssert.assertJsonEquals;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;

import java.util.Arrays;
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

    private final String SQL_LOAD_PARAMETER = "SELECT id,cdate,cast(processid as varchar),parameter FROM temp_search_parameter WHERE processid=':processid' ORDER BY parameter";
    @Inject
    SearchParameterService searchParameter;
    private static final long serialVersionUID = 1L;

    private String json1 = "{\"field0\":1,\"field2\":\"Hallo\"}";
    private String json2 = "{\"field3\":1}";

    @Test
    public void test001_saveLoad() throws Exception {
        createAndSaveParameter();

        checkParameterOfProcess(processId, json1);
        checkParameterOfProcess(processId2, json2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test002_removeParameter() throws Exception {
        createAndSaveParameter();

        searchParameter.removeParameter(processId);
        List parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId.toString()));
        Assert.assertTrue(parameter.isEmpty());
        parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId2.toString()));
        Assert.assertEquals(1, parameter.size());

    }

    @SuppressWarnings("unchecked")
    private void checkParameterOfProcess(UUID processId, String expectedJson) {
        List<Object[]> parameter = (List) entityManagerService.doSqlQuery(SQL_LOAD_PARAMETER.replace(":processid", processId.toString()));
        Assert.assertEquals(1, parameter.size());
        Assert.assertEquals(processId, UUID.fromString((String) parameter.get(0)[2]));
        assertJsonEquals(expectedJson, (String) parameter.get(0)[3]);
    }

    private void createAndSaveParameter() throws Exception {
        processId = UUID.randomUUID();
        processId2 = UUID.randomUUID();
        searchParameter.saveParameter(processId.toString(), json1);
        searchParameter.saveParameter(processId2.toString(), json2);

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
