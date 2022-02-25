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
package de.ipb_halle.lbac.items.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class ContainerSelectionDialogControllerTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @Test
    public void test_actionOnSelect() {
        AtomicReference<Container> containerFromCallback = new AtomicReference<>();
        Consumer<Container> callback = (c) -> containerFromCallback.set(c);
        ContainerSelectionDialogController controller = new ContainerSelectionDialogController(new ArrayList<>(),
                callback, messagePresenter);
        Container container = new Container();
        container.setId(42);

        controller.actionOnSelect(container);
        assertTrue(container.isEqualTo(containerFromCallback.get()));
    }

    @Test
    public void test_getDimensionString() {
        ContainerSelectionDialogController controller = new ContainerSelectionDialogController(new ArrayList<>(), null,
                messagePresenter);
        Container container = new Container();
        assertEquals("-", controller.getDimensionString(container));

        container.setRows(4);
        container.setColumns(10);
        container.setItems(new Item[4][10]);
        assertEquals("4 x 10", controller.getDimensionString(container));
    }

    @Test
    public void test_getAvailableContainers_checkLocalizedNames() {
        Container c1 = new Container();
        Container c2 = new Container();
        Container parent = new Container();
        c1.setId(42);
        c2.setId(99);
        parent.setId(123);
        c1.setLabel("Container1");
        c2.setLabel("Container2");
        parent.setLabel("parent");
        c1.setType(new ContainerType("FREEZER", 90, false, false));
        c2.setType(new ContainerType("FRIDGE", 90, false, false));
        parent.setType(new ContainerType("ROOM", 100, false, false));
        c1.setParentContainer(parent);
        List<Container> availableContainers = Arrays.asList(c1, c2);

        assertEquals("FREEZER", c1.getType().getLocalizedName());
        assertEquals("FRIDGE", c2.getType().getLocalizedName());
        assertEquals("ROOM", parent.getType().getLocalizedName());

        ContainerSelectionDialogController controller = new ContainerSelectionDialogController(availableContainers,
                null, messagePresenter);
        List<Container> containers = controller.getAvailableContainers();

        assertEquals(containers, availableContainers);
        assertEquals("container_type_FREEZER", containers.get(0).getType().getLocalizedName());
        assertEquals("container_type_FRIDGE", containers.get(1).getType().getLocalizedName());
        assertEquals("container_type_ROOM", containers.get(0).getParentContainer().getType().getLocalizedName());
    }
}
