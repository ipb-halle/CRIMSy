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
package de.ipb_halle.lbac.material.sequence.search;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import javax.inject.Inject;
import org.junit.Test;

/**
 *
 * @author flange
 */
@RunWith(Arquillian.class)
public class DataBaseTransactionContextTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    OuterDatabaseTransactionService outerService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("SequenceSaveAndLoadTest.war");
        deployment.addClass(InnerDatabaseTransactionService.class);
        deployment.addClass(OuterDatabaseTransactionService.class);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }

    @Test
    public void test001() {
        outerService.outerDataBaseAction();
    }
}
