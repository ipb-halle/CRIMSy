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
package de.ipb_halle.lbac.container;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Container implements DTO, Serializable, Searchable {

    public enum DimensionType {
        NONE,
        ZERO_DIMENSION,
        ONE_DIMENSION,
        TWO_DIMENSION
    }
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Integer id;
    private String label;
    private Project project;
    private Integer rows;
    private Integer columns;
    private ContainerType type;
    private String fireArea;
    private String gmoSafetyLevel;
    private String barCode;
    private Item[][] items;
    List<Container> containerHierarchy = new ArrayList<>();
    private boolean deactivated;
    private boolean swapDimensions;
    private boolean zeroBased;

    private String autoCompleteString;

    /**
     * default constructor
     */
    public Container() {
        deactivated = false;
        swapDimensions = false;
        zeroBased = false;
        type = new ContainerType("xxx", 100, deactivated, deactivated);
    }

    public Container(
            ContainerEntity dbentity,
            Project p,
            Container parentContainer) {
        this.id = dbentity.getId();
        this.label = dbentity.getLabel();
        this.containerHierarchy.add(parentContainer);
        this.project = p;
        this.rows = dbentity.getRows();
        this.columns = dbentity.getColumns();
        this.fireArea = dbentity.getFireArea();
        this.gmoSafetyLevel = dbentity.getGmoSafetyLevel();
        this.barCode = dbentity.getBarcode();
        this.deactivated = dbentity.isDeactivated();
        this.swapDimensions = dbentity.isSwapDimensions();
        this.zeroBased = dbentity.isZeroBased();
        this.items = createEmptyItemArray();

    }

    public DimensionType getDimensionType() {
        if (rows == null) {
            return DimensionType.NONE;
        } else {
            if (columns == null) {
                return DimensionType.ONE_DIMENSION;
            } else {
                return DimensionType.TWO_DIMENSION;
            }
        }
    }

    public Item[][] getItems() {
        return items;
    }

    public void setItems(Item[][] items) {
        this.items = items;
    }

    public Container(
            ContainerEntity dbentity) {
        this.id = dbentity.getId();
        this.label = dbentity.getLabel();
        this.rows = dbentity.getRows();
        this.columns = dbentity.getColumns();
        this.fireArea = dbentity.getFireArea();
        this.gmoSafetyLevel = dbentity.getGmoSafetyLevel();
        this.barCode = dbentity.getBarcode();
        this.deactivated = dbentity.isDeactivated();
        this.swapDimensions = dbentity.isSwapDimensions();
        this.zeroBased = dbentity.isZeroBased();
        items = createEmptyItemArray();
    }

    public int getId() {
        return id;
    }

    public Item getItemAtPos(Integer rowIndex, Integer colIndex) {
        if (rows == null) {
            return null;
        }
        if (columns == null) {
            return items[0][rowIndex];
        }
        return items[rowIndex][colIndex];
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getNameToDisplay() {
        return label;
    }

    public Container getParentContainer() {
        if (containerHierarchy.size() > 0) {
            return containerHierarchy.get(0);
        }
        return null;
    }

    public void setParentContainer(Container parentContainer) {
        containerHierarchy.clear();
        containerHierarchy.add(parentContainer);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

    public String getFireArea() {
        return fireArea;
    }

    public void setFireArea(String fireArea) {
        this.fireArea = fireArea;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getLocation(boolean reverse, boolean htmlSupport) {
        if (containerHierarchy.isEmpty()) {
            return "";
        }
        String locationString;
        if (reverse) {
            Collections.reverse(containerHierarchy);
        }
        if (containerHierarchy.isEmpty()) {
            return "";
        } else {
            locationString = containerHierarchy
                    .stream()
                    .map(r -> r.getLabel())
                    .collect(Collectors.joining(" -> <br>"));
        }
        if (reverse) {
            Collections.reverse(containerHierarchy);
        }
        if (!htmlSupport) {
            locationString = locationString.replace("<br>", "");
        }
        return locationString;

    }

    public Item[][] createEmptyItemArray() {
        if ((rows != null) || (columns != null)) {
            return new Item[columns == null ? 1 : rows][columns == null ? rows : columns];
        }
        return null;
    }

    public List<Container> getContainerHierarchy() {
        return containerHierarchy;
    }

    public String getGmoSafetyLevel() {
        return gmoSafetyLevel;
    }

    public void setGmoSafetyLevel(String level) {
        this.gmoSafetyLevel = level;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

    @Override
    public ContainerEntity createEntity() {
        ContainerEntity dbe = new ContainerEntity();
        dbe.setId(id);
        if (this.getParentContainer() != null) {
            dbe.setParentcontainer(this.getParentContainer().getId());
        }
        if (this.project != null) {
            dbe.setProjectid(this.project.getId());
        }
        dbe.setType(this.getType().getName());
        dbe.setBarcode(this.barCode);
        dbe.setRows(rows);
        dbe.setColumns(columns);
        dbe.setFireArea(this.fireArea);
        dbe.setGmoSafetyLevel(this.gmoSafetyLevel);
        dbe.setLabel(this.label);
        dbe.setDeactivated(this.deactivated);
        dbe.setSwapDimensions(this.swapDimensions);
        dbe.setZeroBased(this.zeroBased);
        return dbe;
    }

    public Container copy() {
        Container c = new Container();
        c.setBarCode(barCode);
        c.setDeactivated(deactivated);
        c.setRows(rows);
        c.setColumns(columns);
        c.setFireArea(fireArea);
        c.setGmoSafetyLevel(gmoSafetyLevel);
        c.setId(id);
        c.setItems(items);
        c.setLabel(label);
        c.setProject(project);
        c.setType(type);
        c.setSwapDimensions(this.swapDimensions);
        c.setZeroBased(this.zeroBased);
        c.getContainerHierarchy().addAll(containerHierarchy);
        return c;
    }

    public String getAutoCompleteString() {
        return autoCompleteString;
    }

    public void setAutoCompleteString(String autoCompleteString) {
        this.autoCompleteString = autoCompleteString;
    }

    public Set<int[]> getPositionsOfItem(int itemId) {
        Set<int[]> places = new HashSet<>();
        if (items == null) {
            return places;
        }
        for (int x = 0; x < items.length; x++) {
            for (int y = 0; y < items[x].length; y++) {

                if (items[x][y] != null) {
                    if (items[x][y].getId() == itemId) {
                        places.add(new int[]{x, y});
                    }
                }

            }
        }
        return places;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Container)) {
            return false;
        }
        Container otherUser = (Container) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.CONTAINER);
    }

    public boolean getSwapDimensions() {
        return swapDimensions;
    }

    public void setSwapDimensions(boolean sd) {
        this.swapDimensions = sd;
    }

    public boolean getZeroBased() {
        return zeroBased;
    }

    public void setZeroBased(boolean zb) {
        this.zeroBased = zb;
    }

    public Integer getRows() {
        return rows;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

}
