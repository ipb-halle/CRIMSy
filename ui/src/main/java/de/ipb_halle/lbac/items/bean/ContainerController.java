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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class ContainerController {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private boolean[][] itemPositions;

    private ItemBean itemBean;
    private Container container;

    public ContainerController(ItemBean itemBean, Container container) {
        this.itemBean = itemBean;
        this.container = container;
        if (container != null && container.getDimensionIndex() != null) {
            itemPositions = new boolean[container.getDimensionIndex()[0]][container.getDimensionIndex()[1]];
            if (itemBean.getState().getOriginalItem() != null) {
                for (int x = 0; x < container.getDimensionIndex()[0]; x++) {
                    for (int y = 0; y < container.getDimensionIndex()[1]; y++) {
                        if (container.getItemAtPos(x, y, 0) != null && container.getItemAtPos(x, y, 0).getId() == itemBean.getState().getOriginalItem().getId()) {
                            itemPositions[x][y] = true;
                        }
                    }
                }
            }
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

    public List<Integer> getTotalSlots(int dimension) {
        if (itemBean.getContainer() == null) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < itemBean.getContainer().getDimensionIndex()[dimension]; i++) {
            result.add(i);
        }
        return result;
    }

    public String getLetterByIndex(int index) {
        return Character.toString((char) (65 + index));
    }

    public boolean[][] getItemPositions() {
        return itemPositions;
    }

    public void setItemPositions(boolean[][] itemPositions) {
        this.itemPositions = itemPositions;
    }

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

    public boolean isContainerPlaceDisabled(int x, int y) {
        if (container.getItemAtPos(x, y, 0) == null) {
            return false;
        } else if (itemBean.getState().getOriginalItem() == null) {
            return false;

        } else {
            return !Objects.equals(container.getItemAtPos(x, y, 0).getId(), itemBean.getState().getOriginalItem().getId());
        }
    }

    public String getToolTipForContainerPlace(int x, int y) {
        if (container.getItemAtPos(x, y, 0) != null) {
            return "ID: " + container.getItemAtPos(x, y, 0).getId();
        }
        return "free place";
    }

    public String getStyleOfContainerPlace(int x, int y) {

        try {
            if (container.getItemAtPos(x, y, 0) == null) {
                return "possible-place";
            } else if (itemBean.getState().getOriginalItem() != null && Objects.equals(container.getItemAtPos(x, y, 0).getId(), itemBean.getState().getOriginalItem().getId())) {
                return "own-place";
            } else {
                return "occupied-place";
            }
        } catch (Exception e) {
            return "no class set due error";
        }

    }
}
