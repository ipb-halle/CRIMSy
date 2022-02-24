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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;

/**
 * @author flange
 */
public class Container2dControllerTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    private Item item;
    private Item anotherItem;
    private Item itemNotInContainer;
    private Container container;
    private Container containerWithNoItems;

    @BeforeEach
    public void init() {
        container = new Container();
        container.setId(10);
        container.setRows(4);
        container.setColumns(10);

        item = new Item();
        item.setId(1);
        item.setContainer(container);

        anotherItem = new Item();
        anotherItem.setId(2);
        anotherItem.setContainer(container);

        itemNotInContainer = new Item();
        itemNotInContainer.setId(3);

        Item[][] items = new Item[4][10];
        items[2][4] = item;
        items[3][9] = anotherItem;
        container.setItems(items);

        containerWithNoItems = new Container();
        containerWithNoItems.setId(11);
        containerWithNoItems.setItems(null);
    }

    @Test
    public void test_itemPositionsAfterConstruction() {
        Container2dController controller;

        controller = new Container2dController(null, item, messagePresenter);
        assertNull(controller.getItemPositions());

        controller = new Container2dController(containerWithNoItems, item, messagePresenter);
        assertNull(controller.getItemPositions());

        controller = new Container2dController(container, item, messagePresenter);
        assertThatItemIsAt(2, 4, controller.getItemPositions());

        controller = new Container2dController(container, anotherItem, messagePresenter);
        assertThatItemIsAt(3, 9, controller.getItemPositions());

        controller = new Container2dController(container, itemNotInContainer, messagePresenter);
        assertThatItemIsAt(-1, -1, controller.getItemPositions()); // matrix is false everywhere
    }

    private void assertThatItemIsAt(int x, int y, boolean[][] itemPositions) {
        for (int i = 0; i < itemPositions.length; i++) {
            for (int j = 0; j < itemPositions[i].length; j++) {
                if ((x == i) && (y == j)) {
                    assertTrue(itemPositions[i][j]);
                } else {
                    assertFalse(itemPositions[i][j]);
                }
            }
        }
    }

    @Test
    public void test_columnsAndRowsAfterConstruction() {
        Container2dController controller;

        controller = new Container2dController(containerWithNoItems, item, messagePresenter);
        assertThat(controller.getColumns(), hasSize(0));
        assertThat(controller.getRows(), hasSize(0));

        controller = new Container2dController(container, item, messagePresenter);
        assertEquals(controller.getColumns(), Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        assertEquals(controller.getRows(), Arrays.asList(0, 1, 2, 3));

        container.setRows(10);
        container.setColumns(null);
        container.setItems(new Item[10][1]);
        controller = new Container2dController(container, item, messagePresenter);
        assertEquals(controller.getColumns(), Arrays.asList(1));
        assertEquals(controller.getRows(), Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Test
    public void test_actionClickCheckBox() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);
        assertThatItemIsAt(2, 4, controller.getItemPositions());

        /*-
         * (1) The user clicks on the already active checkbox (= the item is there)
         */
        boolean[] colsInThirdRow = controller.getItemPositions()[2];
        colsInThirdRow[4] = false;
        controller.actionClickCheckBox(2, 4);
        assertThatItemIsAt(-1, -1, controller.getItemPositions()); // matrix is false everywhere

        /*-
         * (2) The user clicks on a non-active checkbox (= the item is somewhere else or nowhere)
         */
        boolean[] colsInSecondRow = controller.getItemPositions()[1];
        colsInSecondRow[7] = true;
        controller.actionClickCheckBox(1, 7);
        assertThatItemIsAt(1, 7, controller.getItemPositions());
    }

    @Test
    public void test_getDimensionLabel() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);

        assertFalse(container.getSwapDimensions());
        assertFalse(container.getZeroBased());
        assertEquals("1", controller.getDimensionLabel(0, 0));
        assertEquals("6", controller.getDimensionLabel(0, 5));
        assertEquals("11", controller.getDimensionLabel(0, 10));
        assertEquals("A", controller.getDimensionLabel(1, 0));
        assertEquals("F", controller.getDimensionLabel(1, 5));
        assertEquals("K", controller.getDimensionLabel(1, 10));
        assertEquals("AA", controller.getDimensionLabel(1, 26));
        assertEquals("AF", controller.getDimensionLabel(1, 31));

        container.setSwapDimensions(true);
        container.setZeroBased(false);
        assertEquals("1", controller.getDimensionLabel(1, 0));
        assertEquals("6", controller.getDimensionLabel(1, 5));
        assertEquals("11", controller.getDimensionLabel(1, 10));
        assertEquals("A", controller.getDimensionLabel(0, 0));
        assertEquals("F", controller.getDimensionLabel(0, 5));
        assertEquals("K", controller.getDimensionLabel(0, 10));
        assertEquals("AA", controller.getDimensionLabel(0, 26));
        assertEquals("AF", controller.getDimensionLabel(0, 31));

        container.setSwapDimensions(false);
        container.setZeroBased(true);
        assertEquals("0", controller.getDimensionLabel(0, 0));
        assertEquals("5", controller.getDimensionLabel(0, 5));
        assertEquals("10", controller.getDimensionLabel(0, 10));
        assertEquals("A", controller.getDimensionLabel(1, 0));
        assertEquals("F", controller.getDimensionLabel(1, 5));
        assertEquals("K", controller.getDimensionLabel(1, 10));
        assertEquals("AA", controller.getDimensionLabel(1, 26));
        assertEquals("AF", controller.getDimensionLabel(1, 31));
    }

    @Test
    public void test_getStyleOfContainerPlace() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);

        assertEquals("possible-place", controller.getStyleOfContainerPlace(0, 0));
        assertEquals("own-place", controller.getStyleOfContainerPlace(2, 4));
        assertEquals("occupied-place", controller.getStyleOfContainerPlace(3, 9));
    }

    @Test
    public void test_getToolTipForContainerPlace() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);

        assertEquals("container_slot_free_place", controller.getToolTipForContainerPlace(0, 0));
        assertEquals("ID: 1", controller.getToolTipForContainerPlace(2, 4));
        assertEquals("ID: 2", controller.getToolTipForContainerPlace(3, 9));
    }

    @Test
    public void test004_isContainerPlaceDisabled() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);

        assertFalse(controller.isContainerPlaceDisabled(0, 0));
        assertFalse(controller.isContainerPlaceDisabled(2, 4));
        assertTrue(controller.isContainerPlaceDisabled(3, 9));
    }

    @Test
    public void test004_resolveItemPositions() {
        Container2dController controller = new Container2dController(container, item, messagePresenter);
        Set<int[]> positions = controller.resolveItemPositions();
        assertThat(positions, hasSize(1));
        assertArrayEquals(new int[] { 2, 4 }, positions.iterator().next());

        controller = new Container2dController(containerWithNoItems, item, messagePresenter);
        assertThat(controller.resolveItemPositions(), is(empty()));
    }
}