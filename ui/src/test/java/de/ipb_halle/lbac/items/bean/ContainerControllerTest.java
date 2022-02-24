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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerControllerTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private ContainerService containerService;

    @Inject
    private UserBeanMock userBean;

    private Item item = new Item();
    private Item anotherItem = new Item();
    private Container container = new Container();
    private ContainerController containerController;

    @BeforeEach
    public void init() {
        item.setId(1);
        anotherItem.setId(2);

        userBean.setCurrentAccount(publicUser);
        container.setRows(4);
        container.setId(10);
        container.setColumns(3);
        Item[][] items = new Item[4][3];
        items[1][0] = item;
        items[0][1] = anotherItem;
        item.setContainer(container);
        container.setItems(items);
        containerController = new ContainerController(item, containerService, userBean,
                MessagePresenterMock.getInstance());
    }

    @Test
    public void test001_isContainerSubComponentRendered() {
        assertTrue(
                containerController.isContainerSubComponentRendered(Container.DimensionType.TWO_DIMENSION.toString()));
        assertFalse(
                containerController.isContainerSubComponentRendered(Container.DimensionType.ONE_DIMENSION.toString()));
    }

    @Test
    public void test002_isContainerSubComponentRendered() {
        assertFalse(containerController.isContainerSubComponentRendered(null));
        assertFalse(containerController.isContainerSubComponentRendered("NONE"));
        assertFalse(containerController.isContainerSubComponentRendered("ZERO_DIMENSION"));
        assertFalse(containerController.isContainerSubComponentRendered("ONE_DIMENSION"));
        assertTrue(containerController.isContainerSubComponentRendered("TWO_DIMENSION"));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerControllerTest.war").addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
