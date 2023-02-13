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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.mocks.ContainerPositionServiceMock;
import de.ipb_halle.lbac.items.mocks.ItemLabelServiceMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import jakarta.inject.Inject;
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
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ValidatorTest extends TestBase {

    private final ContainerPositionServiceMock containerServiceMock = new ContainerPositionServiceMock();
    private final ItemLabelServiceMock labelServiceMock = new ItemLabelServiceMock();
    private final Item item = new Item();
    private ContainerController containerController;
    private final String customLabel = "customLabel";
    private boolean isCustomLabel;
    private Validator validator;

    @Inject
    private ContainerService containerService;

    @Inject
    private UserBeanMock userBean;

    @BeforeEach
    public void init() {
        userBean.setCurrentAccount(publicUser);
        validator = new Validator(containerServiceMock, labelServiceMock);
        validator.setMessagePresenter(MessagePresenterMock.getInstance());
        containerServiceMock.arePositionsFree = true;
        labelServiceMock.isLabelAvailable = true;
        isCustomLabel = false;
        containerController = new ContainerController(
                item,
                containerService,
                userBean,
                MessagePresenterMock.getInstance());
    }

    @Test
    public void test001_itemValideToSave_noCustomLabel() {
        Assert.assertTrue(validator.itemValidToSave(item, containerController, isCustomLabel, customLabel));

        labelServiceMock.isLabelAvailable = false;
        Assert.assertTrue(validator.itemValidToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test002_itemValideToSave_allowedCustomLabel() {
        isCustomLabel = true;

        Assert.assertTrue(validator.itemValidToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test003_itemValideToSave_forbiddenCustomLabel() {
        isCustomLabel = true;
        labelServiceMock.isLabelAvailable = false;

        Assert.assertFalse(validator.itemValidToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test004_itemValideToSave_positionForbidden() {
        containerServiceMock.arePositionsFree = false;

        Assert.assertFalse(validator.itemValidToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ValidatorTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

}
