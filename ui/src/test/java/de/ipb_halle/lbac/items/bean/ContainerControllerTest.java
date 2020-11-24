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
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.mocks.ItemBeanContainerControllerMock;
import de.ipb_halle.lbac.items.mocks.ItemBeanMock;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ContainerControllerTest {

    @Test
    public void test001_xxx() {
        Item item = new Item();
        item.setId(1);
        Item anotherItem = new Item();
        item.setId(2);
        ItemBean bean = new ItemBeanContainerControllerMock(item);

        Container container = new Container();
        container.setDimension("2;2;1");
        Item[][][] items = new Item[4][4][1];
        items[1][0][0] = item;
        items[0][1][0] = anotherItem;
        container.setItems(items);

        ContainerController controller = new ContainerController(bean, container);
        Assert.assertEquals("possible-place", controller.getStyleOfContainerPlace(0, 0));

        Assert.assertEquals("own-place", controller.getStyleOfContainerPlace(1, 0));
        Assert.assertEquals("occupied-place", controller.getStyleOfContainerPlace(0, 1));

    }
}
