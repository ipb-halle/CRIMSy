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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
public class InputValidatorTest extends TestBase {

    @Inject
    private ContainerService containerService;
    private Container c;
    private InputValidator validator;
    private MessagePresenterMock messagePresenter;

    @BeforeEach
    public void init() {
        validator = new InputValidator(containerService);
        validator.setContainerService(containerService);
        messagePresenter = MessagePresenterMock.getInstance();
        messagePresenter.resetMessages();
        validator.setErrorMessagePresenter(messagePresenter);
        c = new Container();
        c.setBarCode(null);
        c.setRows(3);
        c.setColumns(3);
        c.setFireArea("F1");
        c.setGmoSafetyLevel("S0");
        c.setLabel("R302");
        c.setType(new ContainerType("ROOM", 100, false, true));

    }

    @Test
    public void test001_isLabelValide() {
        Container c2 = new Container();
        c2.setLabel("R302");
        c2.setType(new ContainerType("ROOM", 100, false, true));
        containerService.saveContainer(c2);

        //Try to save a new container with a already saved name and a 
        //unique_name type. This should lead to a error message.
        c.setType(new ContainerType("mock_no_unique_name", 99, false, true));
        Assert.assertFalse("test001: containername already in use", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_name_invalide"));
        messagePresenter.resetMessages();

        //Try to save a new container with a already saved name and a 
        //NOT unique_name type. This should lead to no error message.
        c.setType(new ContainerType("ROOM", 100, false, false));
        Assert.assertTrue("test001: containername already in use but is accepted", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());

        c.setLabel("");
        Assert.assertFalse("test001: containername is empty", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_name_invalide"));
        messagePresenter.resetMessages();

        c.setLabel(null);
        Assert.assertFalse("test001: containername is null", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_name_invalide"));
        messagePresenter.resetMessages();

        c.setLabel("VALIDE_CONTAINER_NAME");
        Assert.assertTrue("test001: Valide containername not accepted", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test002_checkProjectValidity() {
        Project p = new Project();
        p.setName("TEST_PROJECT");

        Assert.assertFalse("test002: project expected, but not found", validator.isInputValideForCreation(c, "TEST_PROJECT", null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_project_invalide"));
        messagePresenter.resetMessages();

        Assert.assertTrue("test002: no project set and  expected", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());

        c.setProject(p);
        Assert.assertFalse("test002: project set, but none expected", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_project_invalide"));
        messagePresenter.resetMessages();

        c.setProject(p);
        Assert.assertFalse("test002: project set, but wrong name expected", validator.isInputValideForCreation(c, "TEST_PROJECT-FAILURE", null, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_project_invalide"));
        messagePresenter.resetMessages();

        Assert.assertTrue("test002: project found and expected", validator.isInputValideForCreation(c, "TEST_PROJECT", null, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test003_isLocationAvailable() {
        Container c2 = new Container();
        c2.setLabel("PARENT_ROOM");
        c2.setType(new ContainerType("ROOM", 100, false, true));
        c.setType(new ContainerType("CUPBOARD", 20, true, false));
        containerService.saveContainer(c2);

        Assert.assertTrue("test003: no location expected and set", validator.isInputValideForCreation(c, null, null, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());

        c.setParentContainer(c2);
        Assert.assertTrue("test003: location set and expected", validator.isInputValideForCreation(c, null, c2, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test004_isLocationBiggerThan() {
        Container c2 = new Container();
        c2.setLabel("PARENT_ROOM");
        c2.setType(new ContainerType("ROOM", 100, false, true));

        c.setParentContainer(c2);
        Assert.assertFalse("test004: container to big for location", validator.isInputValideForCreation(c, null, c2, 1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_location_to_small"));
        messagePresenter.resetMessages();

        c.setType(new ContainerType("CUPBOARD", 20, true, false));
        Assert.assertTrue("test004: container should fit into location ", validator.isInputValideForCreation(c, null, c2, 1, 1));
        Assert.assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test005_isDimensionsValide() {
        Container c2 = new Container();
        c2.setLabel("PARENT_ROOM");
        c2.setType(new ContainerType("ROOM", 100, false, true));
        Assert.assertFalse("test005: invalide dimension", validator.isInputValideForCreation(c, null, c2, -1, 1));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_dimensions"));
        messagePresenter.resetMessages();

        Assert.assertFalse("test005: invalide dimension", validator.isInputValideForCreation(c, null, c2, -1, -11));
        Assert.assertTrue(messagePresenter.getLastErrorMessage().equals("container_input_dimensions"));
        messagePresenter.resetMessages();

        Assert.assertTrue("test005: invalide dimension", validator.isInputValideForCreation(c, null, c2, 1, 1));

        messagePresenter.resetMessages();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("InputValidatorTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
