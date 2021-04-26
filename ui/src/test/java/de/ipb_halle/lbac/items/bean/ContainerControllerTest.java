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

import de.ipb_halle.lbac.container.Container;
import static de.ipb_halle.lbac.container.Container.DimensionType.NONE;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.mocks.ItemBeanContainerControllerMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ContainerControllerTest {
    
    Item item = new Item();
    Item anotherItem = new Item();
    ItemBean bean;
    Container container = new Container();
    ContainerController controller;
    
    @Before
    public void setUp() {
        item.setId(1);
        anotherItem.setId(2);
        bean = new ItemBeanContainerControllerMock(item);
        container.setRows(4);
        container.setColumns(3);
        Item[][] items = new Item[4][3];
        items[1][0] = item;
        items[0][1] = anotherItem;
        container.setItems(items);
        controller = new ContainerController(bean, container);
        bean.setContainerController(controller);
        controller.setMessagePresenter(new MessagePresenterMock());
        
    }
    
    @Test
    public void test001_getStyleOfContainerPlace() {
        Assert.assertEquals("possible-place", controller.getStyleOfContainerPlace(0, 0));
        Assert.assertEquals("own-place", controller.getStyleOfContainerPlace(1, 0));
        Assert.assertEquals("occupied-place", controller.getStyleOfContainerPlace(0, 1));
    }
    
    @Test
    public void test002_getToolTipForContainerPlace() {
        Assert.assertEquals("container_slot_free_place", controller.getToolTipForContainerPlace(0, 0));
        Assert.assertEquals("ID: 1", controller.getToolTipForContainerPlace(1, 0));
        Assert.assertEquals("ID: 2", controller.getToolTipForContainerPlace(0, 1));
    }
    
    @Test
    public void test003_clickCheckBox() {
        controller.actionClickCheckBox(0, 0);
        Assert.assertFalse(controller.getItemPositions()[0][0]);
        controller.setItemAtPosition(0, 0);
        controller.actionClickCheckBox(0, 0);
        Assert.assertTrue(controller.getItemPositions()[0][0]);
        controller.removeItemFromPosition(0, 0);
        Assert.assertFalse(controller.getItemPositions()[0][0]);
    }
    
    @Test
    public void test004_isContainerPlaceDisabled() {
        Assert.assertFalse(controller.isContainerPlaceDisabled(0, 0));
        Assert.assertFalse(controller.isContainerPlaceDisabled(1, 0)); //own item
        Assert.assertTrue(controller.isContainerPlaceDisabled(0, 1));
    }
    
    @Test
    public void test004_resolveItemPositions() {
        controller.setItemAtPosition(0, 0);
        Set<int[]> positions = controller.resolveItemPositions();
        Assert.assertEquals(1, positions.size());
    }
    
    @Test
    public void test005_resolveItemPositions() {
        // default: not swapped --> dimension 1 is labeled with letters 
        Assert.assertEquals("A", controller.getDimensionLabel(1, 0));
    }
    
    @Test
    public void test006_checkRowAndColLists() {
        Assert.assertEquals(4, controller.getRows().size());
        Assert.assertEquals(3, controller.getColumns().size());
        
        container.setRows(null);
        container.setColumns(null);
        container.setItems(null);
        controller = new ContainerController(bean, container);
        
        Assert.assertEquals(0, controller.getRows().size());
        Assert.assertEquals(0, controller.getColumns().size());
        
        container.setRows(10);
        container.setColumns(null);
        container.setItems(new Item[10][1]);
        controller = new ContainerController(bean, container);
        
        Assert.assertEquals(10, controller.getRows().size());
        Assert.assertEquals(1, controller.getColumns().size());
    }
    
    @Test
    public void test007_isContainerSubComponentRendered() {
        Assert.assertTrue(controller.isContainerSubComponentRendered(Container.DimensionType.TWO_DIMENSION.toString()));
        Assert.assertFalse(controller.isContainerSubComponentRendered(Container.DimensionType.ONE_DIMENSION.toString()));
        
    }
    
    @Test
    public void test008_isContainerSubComponentRendered() {
        Assert.assertFalse(controller.isContainerSubComponentRendered(null));
        Assert.assertFalse(controller.isContainerSubComponentRendered("NONE"));
        Assert.assertFalse(controller.isContainerSubComponentRendered("ZERO_DIMENSION"));
        Assert.assertFalse(controller.isContainerSubComponentRendered("ONE_DIMENSION"));
        Assert.assertTrue(controller.isContainerSubComponentRendered("TWO_DIMENSION"));
    }
    
}
