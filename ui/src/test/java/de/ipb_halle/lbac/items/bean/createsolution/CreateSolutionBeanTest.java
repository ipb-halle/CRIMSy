/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.createsolution;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;

/**
 * @author flange
 */
@ExtendWith(ArquillianExtension.class)
class CreateSolutionBeanTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private CreateSolutionBean bean;

    /*
     * Tests for actionStartCreateSolution()
     */
    @Test
    public void test_actionStartCreateSolution() {
        // TODO
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CreateSolutionBeanTest.war").addClass(CreateSolutionBean.class);
    }
}