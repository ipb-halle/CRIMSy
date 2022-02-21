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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.mocks.ItemBeanContainerControllerMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.Set;
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
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerControllerTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private ContainerService containerService;

    @Inject
    private UserBeanMock userBean;

    Item item = new Item();
    Item anotherItem = new Item();  
    Container container = new Container();
    ContainerController containerController;

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
        containerController = new ContainerController(item, containerService, userBean, MessagePresenterMock.getInstance());

    }

    @Test
    public void test001_getStyleOfContainerPlace() {
        Assert.assertEquals("possible-place", containerController.getStyleOfContainerPlace(0, 0));
        Assert.assertEquals("own-place", containerController.getStyleOfContainerPlace(1, 0));
        Assert.assertEquals("occupied-place", containerController.getStyleOfContainerPlace(0, 1));       
    }

    @Test
    public void test002_getToolTipForContainerPlace() {
        Assert.assertEquals("container_slot_free_place", containerController.getToolTipForContainerPlace(0, 0));
        Assert.assertEquals("ID: 1", containerController.getToolTipForContainerPlace(1, 0));
        Assert.assertEquals("ID: 2", containerController.getToolTipForContainerPlace(0, 1));
    }

    @Test
    public void test003_clickCheckBox() {
        containerController.actionClickCheckBox(0, 0);
        Assert.assertFalse(containerController.getItemPositions()[0][0]);
        containerController.setItemAtPosition(0, 0);
        containerController.actionClickCheckBox(0, 0);
        Assert.assertTrue(containerController.getItemPositions()[0][0]);
        containerController.removeItemFromPosition();
        Assert.assertFalse(containerController.getItemPositions()[0][0]);
    }

    @Test
    public void test004_isContainerPlaceDisabled() {
        Assert.assertFalse(containerController.isContainerPlaceDisabled(0, 0)); //empty slot
        Assert.assertFalse(containerController.isContainerPlaceDisabled(1, 0)); //own item
        Assert.assertTrue(containerController.isContainerPlaceDisabled(0, 1));  // another item
    }

    @Test
    public void test004_resolveItemPositions() {
        containerController.setItemAtPosition(0, 0);
        Set<int[]> positions = containerController.resolveItemPositions();
        Assert.assertEquals(2, positions.size());
    }

    @Test
    public void test005_resolveItemPositions() {
        // default: not swapped --> dimension 1 is labeled with letters 
        Assert.assertEquals("A", containerController.getDimensionLabel(1, 0));
    }

    @Test
    public void test006_checkRowAndColLists() {
        Assert.assertEquals(4, containerController.getRows().size());
        Assert.assertEquals(3, containerController.getColumns().size());

        item.getContainer().setRows(null);
        item.getContainer().setColumns(null);
        item.getContainer().setItems(null);
        containerController = new ContainerController(item, containerService, userBean, MessagePresenterMock.getInstance());

        Assert.assertEquals(0, containerController.getRows().size());
        Assert.assertEquals(0, containerController.getColumns().size());

        item.getContainer().setRows(10);
        item.getContainer().setColumns(null);
        item.getContainer().setItems(new Item[10][1]);
        containerController = new ContainerController(item, containerService, userBean, MessagePresenterMock.getInstance());

        Assert.assertEquals(10, containerController.getRows().size());
        Assert.assertEquals(1, containerController.getColumns().size());
    }

    @Test
    public void test007_isContainerSubComponentRendered() {
        Assert.assertTrue(containerController.isContainerSubComponentRendered(Container.DimensionType.TWO_DIMENSION.toString()));
        Assert.assertFalse(containerController.isContainerSubComponentRendered(Container.DimensionType.ONE_DIMENSION.toString()));

    }

    @Test
    public void test008_isContainerSubComponentRendered() {
        Assert.assertFalse(containerController.isContainerSubComponentRendered(null));
        Assert.assertFalse(containerController.isContainerSubComponentRendered("NONE"));
        Assert.assertFalse(containerController.isContainerSubComponentRendered("ZERO_DIMENSION"));
        Assert.assertFalse(containerController.isContainerSubComponentRendered("ONE_DIMENSION"));
        Assert.assertTrue(containerController.isContainerSubComponentRendered("TWO_DIMENSION"));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerControllerTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

}
