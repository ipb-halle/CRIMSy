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
package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.device.print.LabelData;
import de.ipb_halle.lbac.device.print.LabelType;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.project.Project;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author fmauz
 */
@LabelType(name = "Item")
public class Item extends ACObject implements DTO, Serializable {

    private Integer id;
    private Double amount;
    private String unit;
    private Article article;
    private Double concentration;
    private Container container;
    private Double containerSize;
    private ContainerType containerType;
    private String description;
    private Material material;
    private Project project;
    private String purity;
    private Solvent solvent;
    private Date cTime;
    private SortedMap<Date, ItemHistory> history = new TreeMap<>();
    private List<Container> nestedContainer = new ArrayList<>();
    private Date expiry_date;

    public Item() {

    }

    public Item(ItemEntity entity,
            Article art,
            Container con,
            Material mat,
            User owner,
            Project project,
            Solvent sol,
            List<Container> nestedContainer,
            ACList aclist) {
        this.id = entity.getId();
        this.amount = entity.getAmount();
        this.unit = entity.getUnit();
        this.article = art;
        this.concentration = entity.getConcentration();
        this.container = con;
        this.containerSize = entity.getContainersize();
        this.expiry_date = entity.getExpiry_date();
        if (entity.getContainertype() != null) {
            this.containerType = new ContainerType(entity.getContainertype(), 0, true, false);
        }
        this.description = entity.getDescription();
        this.material = mat;
        this.project = project;
        this.purity = entity.getPurity();
        this.solvent = sol;
        this.cTime = entity.getCtime();
        this.nestedContainer = nestedContainer;
        this.setACList(aclist);
        this.setOwner(owner);
    }

    @Override
    public ItemEntity createEntity() {
        ItemEntity entity = new ItemEntity();
        entity.setId(id);
        entity.setAmount(amount);
        entity.setUnit(unit);
        if (article != null) {
            entity.setArticleid(article.getId());
        }
        entity.setConcentration(concentration);
        if (container != null) {
            entity.setContainerid(container.getId());
        }
        entity.setContainersize(containerSize);
        entity.setDescription(description);
        if (containerType != null) {
            entity.setContainertype(containerType.getName());
        }
        entity.setMaterialid(material.getId());
        if (getOwner() != null) {
            entity.setOwner(getOwner().getId());
        }
        if (project != null) {
            entity.setProjectid(project.getId());
        }
        entity.setPurity(purity);
        if (solvent != null) {
            entity.setSolventid(solvent.getId());
        }
        entity.setAclist_id(getACList().getId());
        entity.setCtime(cTime);
        entity.setExpiry_date(expiry_date);
        return entity;
    }

    /**
     * compute a 10 digit string including the Interleave 25 check sum ToDo: add
     * a configuratble offset to the item id
     */
    @LabelData(name = "itemId25")
    public String getItemId25() {
        int j = 3;
        int k = 0;
        String s = String.format("%09d", this.id);
        int l = s.length();

        /* 
         * Code 25 expects an even number of digits
         * --> prepend a zero as needed
         *
        if((l % 2) == 0) {
            s = "0" + s;
            l++;
        }
         */
        for (int i = 0; i < l; i++) {
            k = k + j * ((int) s.charAt(i) - 0x30);
            j = (j + 2) % 4;
        }
        k = (10 - (k % 10)) % 10;
        return s + Integer.toString(k);
    }

    @LabelData(name = "itemIdPlain")
    public String getItemIdPlain() {
        return String.format("%08d", id.intValue());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Double getConcentration() {
        return concentration;
    }

    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public Double getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(Double containerSize) {
        this.containerSize = containerSize;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getPurity() {
        return purity;
    }

    public void setPurity(String purity) {
        this.purity = purity;
    }

    public Solvent getSolvent() {
        return solvent;
    }

    public void setSolvent(Solvent solvent) {
        this.solvent = solvent;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public Date getcTime() {
        return cTime;
    }

    public void setcTime(Date cTime) {
        this.cTime = cTime;
    }

    public SortedMap<Date, ItemHistory> getHistory() {
        return history;
    }

    public void setHistory(SortedMap<Date, ItemHistory> history) {
        this.history = history;
    }

    public List<Container> getNestedContainer() {
        return nestedContainer;
    }

    public String getNestedLocation() {
        if (container == null) {
            return "";
        }
        String location = "";
        for (Container c : nestedContainer) {
            location += c.getLabel() + ".";
        }

        return location + container.getLabel();
    }

    public Item copy() {
        Item copiedItem = new Item();
        copiedItem.setAmount(getAmount());
        copiedItem.setArticle(getArticle());
        copiedItem.setConcentration(getConcentration());
        copiedItem.setContainer(getContainer());
        copiedItem.setContainerSize(getContainerSize());
        copiedItem.setContainerType(getContainerType());
        copiedItem.setDescription(getDescription());
        copiedItem.setId(getId());
        copiedItem.setMaterial(getMaterial());
        copiedItem.setOwner(getOwner());
        copiedItem.setProject(getProject());
        copiedItem.setPurity(getPurity());
        copiedItem.setSolvent(getSolvent());
        copiedItem.setUnit(getUnit());
        copiedItem.setcTime(getcTime());
        copiedItem.setHistory(getHistory());
        copiedItem.setACList(getACList());
        return copiedItem;
    }

}
