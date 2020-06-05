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
package de.ipb_halle.lbac.container.bean;

import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.mock.ErrorMessagePresenterMock;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.material.service.TaxonomyService;
import de.ipb_halle.lbac.material.service.TissueService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.ACListService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class InputBValidatorTest extends TestBase {

    @Inject
    private ContainerService containerService;

    @Test
    public void test001_inputValidationForNewContainer() {
        InputValidator validator = new InputValidator();
        validator.setContainerService(containerService);
        validator.setErrorMessagePresenter(new ErrorMessagePresenterMock());
        Container c = new Container();
        c = new Container();
        c.setBarCode(null);
        c.setDimension("3;3;1");
        c.setFireSection("F1");
        c.setGmosavety("S0");
        c.setLabel("R302");

        c.setType(new ContainerType("ROOM", 90));
        Assert.assertFalse(validator.isInputValideForCreation(c, "wrongPreferredName", null,1,1));
        Project p = new Project();
        p.setName("Test-Project");
        c.setProject(p);
        Assert.assertFalse(validator.isInputValideForCreation(c, "wrongPreferredName", null,1,1));
        Assert.assertTrue(validator.isInputValideForCreation(c, "Test-Project", null,1,1));
        c.setProject(null);
        Assert.assertTrue(validator.isInputValideForCreation(c, "", null,1,1));
        Assert.assertTrue(validator.isInputValideForCreation(c, null, null,1,1));
        c.setProject(p);
        Assert.assertFalse(validator.isInputValideForCreation(c, null, null,1,1));
        Assert.assertFalse(validator.isInputValideForCreation(c, "", null,1,1));

        c.setProject(null);
        containerService.saveContainer(c);

        Assert.assertFalse(validator.isInputValideForCreation(c, "", null,1,1));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("InputBValidatorTest.war")
                .addClass(ContainerService.class)
                .addClass(ACListService.class)
                .addClass(SystemSettings.class)
                .addClass(ItemService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(ArticleService.class)
                .addClass(KeyManager.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(LdapProperties.class)
                .addClass(ProjectService.class);
    }
}
