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
import de.ipb_halle.lbac.container.Container.DimensionType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the rendering and the actions for manipulating the container of an
 * item and its position in it
 *
 * @author fmauz
 */
public class ContainerController {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private boolean[][] itemPositions;
    private ItemBean itemBean;
    private Container container;
    private List<Integer> columnsList = new ArrayList<>();
    private List<Integer> rowsList = new ArrayList<>();
    private MessagePresenter messagePresenter = JsfMessagePresenter.getInstance();

    /**
     * Sets the container and creates the boolean position matrix (for the
     * checkBoxes), and the index lists for rows and cols(for ui:repeat) .
     *
     * @param itemBean
     * @param container
     */
    public ContainerController(ItemBean itemBean, Container container) {
        this.itemBean = itemBean;
        setNewContainer(container);
    }

    private void createInitialPositionMatrix() {
        Item[][] items = container.getItems();
        itemPositions = new boolean[items.length][items[0].length];
        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items[i].length; j++) {
                itemPositions[i][j] = isCurrentItemAtPosition(items, j, j);
            }
        }
    }

    private boolean isCurrentItemAtPosition(Item[][] items, int x, int y) {
        Integer currentItemId = null;
        if (itemBean.getState().getOriginalItem() != null) {
            currentItemId = itemBean.getState().getOriginalItem().getId();
        }
        return (items[x][y] != null && items[x][y].getId().equals(currentItemId));
    }

    /**
     * Creates a new container with empty itemPositions (default false) and
     * creates the index lists for rows and cols(for ui:repeat) .
     *
     * @param c
     */
    public void setNewContainer(Container c) {
        this.container = c;
        if (container != null && container.getItems() != null) {
            Item[][] items = container.getItems();
            itemPositions = new boolean[items.length][items[0].length];
            createInitialPositionMatrix();
            createColumnList();
            createRowList();
        } else {
            itemPositions = null;
            rowsList.clear();
            columnsList.clear();
        }
    }

    /**
     * checks if item is in at least one slot of the container
     *
     * @return
     */
    public boolean isItemPositionValide() {
        if (container != null
                && (container.getDimensionType() == DimensionType.ONE_DIMENSION
                || container.getDimensionType() == DimensionType.TWO_DIMENSION)) {
            return resolveItemPositions().size() > 0;
        }
        return true;
    }

    /**
     * Creates a list of column indices which is used in the ui:repeat element
     *
     * @return
     */
    private void createColumnList() {
        columnsList.clear();
        if (container.getColumns() == null) {
            columnsList.add(1);
        } else {
            for (int i = 0; i < container.getColumns(); i++) {
                columnsList.add(i);
            }
        }
    }

    /**
     * Creates a list of row indices which is used in the ui:repeat element
     *
     * @return
     */
    private void createRowList() {
        rowsList.clear();
        for (int i = 0; i < container.getRows(); i++) {
            rowsList.add(i);
        }

    }

    public boolean isContainerSubComponentRendered(String typename) {
        if (typename == null) {
            return itemBean.getContainer() == null;
        }
        DimensionType type = DimensionType.valueOf(typename);
        if (null != type) {
            return itemBean.getContainer() != null && itemBean.getContainer().getDimensionType() == type;
        }
        return false;
    }

    public List<Integer> getColumns() {
        return columnsList;
    }

    public List<Integer> getRows() {
        return rowsList;
    }

    /**
     * Compute the row and column label for containers. Honour swapping of axes
     * and whether counting starts from '1' or '0'. Containers will be drawn
     * with position 'A0' / 'A1' in the upper left corner.
     *
     * @param dimension <code>0 == x-axis, 1 == y-axis</code>
     * @param index the index value on the axis
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

    public boolean[][] getItemPositions() {
        return itemPositions;
    }

    public void setItemPositions(boolean[][] itemPositions) {
        this.itemPositions = itemPositions;
    }

    /**
     * Get the item of the saved current item
     *
     * @return
     */
    public Set<int[]> resolveItemPositions() {
        if (itemPositions == null) {
            return new HashSet<>();
        }
        Set<int[]> positions = new HashSet<>();
        for (int x = 0; x < itemPositions.length; x++) {
            for (int y = 0; y < itemPositions[0].length; y++) {
                if (itemPositions[x][y]) {
                    positions.add(new int[]{x, y});
                }
            }
        }
        return positions;
    }

    /**
     * Checks if another item is present at place x/y.
     *
     * @param x
     * @param y
     * @return true if another item blocks the slot
     */
    public boolean isContainerPlaceDisabled(int x, int y) {
        if (container.getItemAtPos(x, y) == null) {
            return false;
        } else if (itemBean.getState().getOriginalItem() == null) {
            return false;

        } else {
            return !Objects.equals(container.getItemAtPos(x, y).getId(), itemBean.getState().getOriginalItem().getId());
        }
    }

    public void actionClickCheckBox(int x, int y) {
        if (itemPositions[x][y]) {
            itemPositions = new boolean[itemPositions.length][itemPositions[0].length];
            itemPositions[x][y] = true;
        }
    }

    public String getToolTipForContainerPlace(int x, int y) {
        if (container.getItemAtPos(x, y) != null) {
            return "ID: " + container.getItemAtPos(x, y).getId();
        }
        return messagePresenter.presentMessage("container_slot_free_place");
    }

    public String getStyleOfContainerPlace(int x, int y) {
        try {
            if (container.getItemAtPos(x, y) == null) {
                return "possible-place";
            } else if (itemBean.getState().getOriginalItem() != null && Objects.equals(container.getItemAtPos(x, y).getId(), itemBean.getState().getOriginalItem().getId())) {
                return "own-place";
            } else {
                return "occupied-place";
            }
        } catch (Exception e) {
            return "no class set due error";
        }
    }

    public void setItemAtPosition(int y, int x) {
        itemPositions[x][y] = true;
    }

    public void removeItemFromPosition(int y, int x) {
        itemPositions[x][y] = false;
    }

    public Container getContainer() {
        return container;
    }

}
