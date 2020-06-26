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
import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.ACListService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class InputValidatorTest extends TestBase {

    @Inject
    private ContainerService containerService;
    private Container c;
    private InputValidator validator;
    private ErrorMessagePresenterMock messagePresenter;
    private boolean allowDuplicateNames;

    @Before
    @Override
    public void setUp() {
        validator = new InputValidator(containerService);
        validator.setContainerService(containerService);
        messagePresenter = new ErrorMessagePresenterMock();
        validator.setErrorMessagePresenter(messagePresenter);
        c = new Container();
        c.setBarCode(null);
        c.setDimension("3;3;1");
        c.setFireSection("F1");
        c.setGmosavety("S0");
        c.setLabel("R302");
        c.setType(new ContainerType("ROOM", 100));
        allowDuplicateNames = false;
        entityManagerService.doSqlUpdate("DELETE FROM containers");
    }

    @Test
    public void test001_isLabelValide() {
        Container c2 = new Container();
        c2.setLabel("R302");
        c2.setType(new ContainerType("ROOM", 100));
        containerService.saveContainer(c2);

        Assert.assertFalse("test001: containername already in use", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_name_invalide"));
        messagePresenter.errorMessages.clear();

        allowDuplicateNames = true;
        Assert.assertTrue("test001: containername already in use but is accepted", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());
        allowDuplicateNames = false;

        c.setLabel("");
        Assert.assertFalse("test001: containername is empty", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_name_invalide"));
        messagePresenter.errorMessages.clear();

        c.setLabel(null);
        Assert.assertFalse("test001: containername is null", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_name_invalide"));
        messagePresenter.errorMessages.clear();

        c.setLabel("VALIDE_CONTAINER_NAME");
        Assert.assertTrue("test001: Valide containername not accepted", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());
    }

    @Test
    public void test002_checkProjectValidity() {
        Project p = new Project();
        p.setName("TEST_PROJECT");

        Assert.assertFalse("test002: project expected, but not found", validator.isInputValideForCreation(c, "TEST_PROJECT", null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_project_invalide"));
        messagePresenter.errorMessages.clear();

        Assert.assertTrue("test002: no project set and  expected", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());

        c.setProject(p);
        Assert.assertFalse("test002: project set, but none expected", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_project_invalide"));
        messagePresenter.errorMessages.clear();

        c.setProject(p);
        Assert.assertFalse("test002: project set, but wrong name expected", validator.isInputValideForCreation(c, "TEST_PROJECT-FAILURE", null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_project_invalide"));
        messagePresenter.errorMessages.clear();

        Assert.assertTrue("test002: project found and expected", validator.isInputValideForCreation(c, "TEST_PROJECT", null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());
    }

    @Test
    public void test003_isLocationAvailable() {
        Container c2 = new Container();
        c2.setLabel("PARENT_ROOM");
        c2.setType(new ContainerType("ROOM", 100));
        c.setType(new ContainerType("CUPBOARD", 20));
        containerService.saveContainer(c2);

        Assert.assertTrue("test003: no location expected and set", validator.isInputValideForCreation(c, null, null, 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());

        Assert.assertFalse("test003: no location set but expected", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_invalide"));
        messagePresenter.errorMessages.clear();

        Assert.assertFalse("test003: location set but none expected", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_invalide"));
        messagePresenter.errorMessages.clear();

        c.setParentContainer(c2);
        Assert.assertTrue("test003: location set and expected", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());
    }

    @Test
    public void test004_isLocationBiggerThan() {
        Container c2 = new Container();
        c2.setLabel("PARENT_ROOM");
        c2.setType(new ContainerType("ROOM", 100));

        c.setParentContainer(c2);
        Assert.assertFalse("test004: container to big for location", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_to_small"));
        messagePresenter.errorMessages.clear();

        c.setType(new ContainerType("CUPBOARD", 20));
        Assert.assertTrue("test004: container should fit into location ", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.isEmpty());
    }

    @Test
    public void test005_isDimensionsValide() {
        Assert.assertFalse("test005: invalide dimension", validator.isInputValideForCreation(c, null, "PARENT_ROOM", -1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_invalide"));
        messagePresenter.errorMessages.clear();

        Assert.assertFalse("test005: invalide dimension", validator.isInputValideForCreation(c, null, "PARENT_ROOM", -1, -11, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_invalide"));
        messagePresenter.errorMessages.clear();

        Assert.assertFalse("test005: invalide dimension", validator.isInputValideForCreation(c, null, "PARENT_ROOM", 1, 1, allowDuplicateNames));
        Assert.assertTrue(messagePresenter.errorMessages.get(0).equals("container_input_location_invalide"));
        messagePresenter.errorMessages.clear();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("InputValidatorTest.war")
                .addClass(ContainerService.class)
                .addClass(ACListService.class)
                .addClass(SystemSettings.class)
                .addClass(ItemService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(ArticleService.class)
                .addClass(KeyManager.class)
                .addClass(ContainerNestingService.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(MoleculeService.class)
                .addClass(LdapProperties.class)
                .addClass(ProjectService.class);
    }
}
