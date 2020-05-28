/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.lbac.project.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Container {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private int id;
    private Container parentContainer;
    private String label;
    private Project project;
    private String dimension;
    private ContainerType type;
    private String fireSection;
    private String gvoClass;
    private String barCode;
    private Item[][][] items;

    public Container() {

    }

    public Container(
            ContainerEntity dbentity,
            Project p,
            Container parentContainer) {
        this.id = dbentity.getId();
        this.parentContainer = parentContainer;
        this.label = dbentity.getLabel();
        this.project = p;
        this.dimension = dbentity.getDimension();
        this.fireSection = dbentity.getFiresection();
        this.gvoClass = dbentity.getGvo_class();
        this.barCode = dbentity.getBarcode();
        if (dimension != null) {
            String[] size = dimension.split(";");
            if (size.length > 2) {
                items = new Item[Integer.valueOf(size[0])][Integer.valueOf(size[1])][Integer.valueOf(size[2])];
            } else if (size.length == 2) {
                items = new Item[Integer.valueOf(size[0])][Integer.valueOf(size[1])][1];
            } else if (size.length == 1) {
                items = new Item[Integer.valueOf(size[0])][1][1];
            }
        }
    }

    public Item[][][] getItems() {
        return items;
    }

    public void setItems(Item[][][] items) {
        this.items = items;
    }

    public Container(
            ContainerEntity dbentity) {
        this.id = dbentity.getId();
        this.label = dbentity.getLabel();
        this.dimension = dbentity.getDimension();
        this.fireSection = dbentity.getFiresection();
        this.gvoClass = dbentity.getGvo_class();
        this.barCode = dbentity.getBarcode();

    }

    public int getId() {
        return id;
    }

    public Item getItemAtPos(int x, int y, int z) {
        if (dimension == null) {
            return null;
        }
        return items[x][y][z];
    }

    public void setId(int id) {
        this.id = id;
    }

    public Container getParentContainer() {
        return parentContainer;
    }

    public void setParentContainer(Container parentContainer) {
        this.parentContainer = parentContainer;
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

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

    public String getFireSection() {
        return fireSection;
    }

    public void setFireSection(String fireSection) {
        this.fireSection = fireSection;
    }

    public String getGvoClass() {
        return gvoClass;
    }

    public void setGvoClass(String gvoClass) {
        this.gvoClass = gvoClass;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getLocation(boolean inclusiveItsself) {
        if (parentContainer == null) {
            return label;
        } else {
            if (inclusiveItsself) {
                return parentContainer.getLocation(false) + "." + label;
            } else {
                return parentContainer.getLocation(false);
            }
        }
    }

    public int[] getDimensionIndex() {
        if (dimension != null) {
            String[] size = dimension.split(";");
            if (size.length > 2) {
                int x = Integer.valueOf(size[0]);
                int y = Integer.valueOf(size[1]);
                int z = Integer.valueOf(size[2]);
                return new int[]{x, y, z};
            } else if (size.length == 2) {
                int x = Integer.valueOf(size[0]);
                int y = Integer.valueOf(size[1]);
                return new int[]{x, y, 1};
            } else if (size.length == 1) {
                int x = Integer.valueOf(size[0]);
                return new int[]{x, 1, 1};
            }
        }
        return null;
    }

}
