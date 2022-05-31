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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.List;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class GlobalAdmissionContextTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("GlobalAdmissionContextTest.war"));
    }

    @Test
    public void test01_createNewGroup() {
        Object o = entityManagerService.doSqlQuery("Select id,name from usersgroups");
        for (Object oo : (List) o) {
            Object[] user = (Object[]) oo;
            System.out.println(user[0] + " : " + user[1]);
        }
        context.createAdminAccount();
    }
}
