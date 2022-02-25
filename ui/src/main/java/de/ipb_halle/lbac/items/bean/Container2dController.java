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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * Controller for the composite component container2d.xhtml
 * 
 * @author flange
 */
public class Container2dController {
    private final Container container;
    private final Item originalItem;
    private final MessagePresenter messagePresenter;

    private boolean[][] itemPositions = null;
    private List<Integer> columnsList = new ArrayList<>();
    private List<Integer> rowsList = new ArrayList<>();

    public Container2dController(Container container, Item originalItem, MessagePresenter messagePresenter) {
        this.container = container;
        this.originalItem = originalItem;
        this.messagePresenter = messagePresenter;

        init();
    }

    private void init() {
        if ((container != null) && (container.getItems() != null)) {
            createInitialPositionMatrix();
            createColumnList();
            createRowList();
        }
    }

    private void createInitialPositionMatrix() {
        Item[][] items = container.getItems();
        itemPositions = new boolean[items.length][items[0].length];
        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items[i].length; j++) {
                itemPositions[i][j] = isCurrentItemAtPosition(items, i, j);
            }
        }
    }

    private boolean isCurrentItemAtPosition(Item[][] items, int x, int y) {
        Item itemAtPosition = items[x][y];
        return (itemAtPosition != null) && itemAtPosition.isEqualTo(originalItem);
    }

    /**
     * Fills the list of column indices which is used in the ui:repeat element
     */
    private void createColumnList() {
        if (container.getColumns() == null) {
            columnsList.add(1);
        } else {
            for (int i = 0; i < container.getColumns(); i++) {
                columnsList.add(i);
            }
        }
    }

    /**
     * Fills the list of row indices which is used in the ui:repeat element
     */
    private void createRowList() {
        for (int i = 0; i < container.getRows(); i++) {
            rowsList.add(i);
        }
    }

    /*
     * Actions
     */
    /*-
     * How is this working? We have to consider two different situations:
     * (1) The user clicks on the already active checkbox (= the item is there):
     * - p:selectBooleanCheckbox sets itemPositions[y][x] to false (No setter needed,
     *   because itemPositions[y] is a reference!!!)
     * - actionClickCheckBox(y, x) does nothing
     * 
     * (2) The user clicks on a non-active checkbox (= the item is somewhere else or nowhere):
     * - p:selectBooleanCheckbox sets itemPositions[y][x] to true
     * - actionClickCheckBox(y, x) clears the matrix and sets true at (y,x)
     * 
     * The test of this method tries to do exactly the same.
     */
    public void actionClickCheckBox(int x, int y) {
        if (itemPositions[x][y]) {
            itemPositions = new boolean[itemPositions.length][itemPositions[0].length];
            itemPositions[x][y] = true;
        }
    }

    public void setItemAtPosition(int y, int x) {
        itemPositions[x][y] = true;
    }

    public void removeItemFromPosition() {
        if (itemPositions != null) {
            itemPositions = new boolean[itemPositions.length][itemPositions[0].length];
        }
    }

    /*
     * Getters with logic
     */
    /**
     * Compute the row and column label for containers. Honour swapping of axes and
     * whether counting starts from '1' or '0'. Containers will be drawn with
     * position 'A0' / 'A1' in the upper left corner.
     *
     * @param dimension <code>0 == x-axis, 1 == y-axis</code>
     * @param index     the index value on the axis
     * @return the row or column label
     */
    public String getDimensionLabel(int dimension, int index) {
        if (((container.getSwapDimensions() && (dimension == 0))
                || ((!container.getSwapDimensions()) && (dimension == 1)))) {
            int i = index + 1;
            StringBuilder sb = new StringBuilder();
            do {
                int r = (i + 25) % 26;
                sb.append(Character.toString((char) (65 + r)));
                i -= r;
                i = i / 26;
            } while (i > 0);
            return sb.reverse().toString();
        }
        return Integer.toString(index + (container.getZeroBased() ? 0 : 1));
    }

    public String getStyleOfContainerPlace(int y, int x) {
        Item itemAtPosition = container.getItemAtPos(y, x);
        if (itemAtPosition == null) {
            return "possible-place";
        } else if (isOriginalItem(itemAtPosition)) {
            return "own-place";
        } else {
            return "occupied-place";
        }
    }

    /**
     * Checks if the passed item is the original item. If the original item is null,
     * return false
     *
     * @param item
     * @return
     */
    private boolean isOriginalItem(Item item) {
        if (originalItem == null || item == null) {
            return false;
        }
        return originalItem.isEqualTo(item);
    }

    public String getToolTipForContainerPlace(int x, int y) {
        Item itemAtPosition = container.getItemAtPos(x, y);
        if (itemAtPosition != null) {
            return "ID: " + itemAtPosition.getId();
        }
        return messagePresenter.presentMessage("container_slot_free_place");
    }

    /**
     * Checks if the checkbox for putting a item at a place (x,y) is disabled. It is
     * disabled if: (1) itembean is in history mode (2) another item blocks the
     * place
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isContainerPlaceDisabled(int x, int y) {
// TODO
//        if (itemBean.isHistoryMode()) {
//            return true;
//        }
        Item itemAtPosition = container.getItemAtPos(x, y);
        if (itemAtPosition == null) {
            return false;
        }
        return !isOriginalItem(itemAtPosition);
    }

    public Set<int[]> resolveItemPositions() {
        Set<int[]> positions = new HashSet<>();
        if (itemPositions == null) {
            return positions;
        }
        for (int x = 0; x < itemPositions.length; x++) {
            for (int y = 0; y < itemPositions[0].length; y++) {
                if (itemPositions[x][y]) {
                    positions.add(new int[] { x, y });
                }
            }
        }
        return positions;
    }

    /*
     * Getters
     */
    public List<Integer> getColumns() {
        return columnsList;
    }

    public List<Integer> getRows() {
        return rowsList;
    }

    public boolean[][] getItemPositions() {
        return itemPositions;
    }
}