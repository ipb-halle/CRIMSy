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
package de.ipb_halle.lbac.exp.assay;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A record object holding an outcome (either single valued or multivalued) for
 * a specific material or item. The records are ordered by rank, counting from
 * zero. Reordering of records is deemed unnecessary and _currently_ not
 * planned.
 *
 * @author fbroda
 */
public class AssayRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Long recordid;
    private Assay assay;
    private transient boolean edit;
    private Material material;
    private Item item;
    private int rank;
    private AssayOutcome outcome;
    private transient AssayOutcomeType outcomeType;

    /**
     * constructor
     */
    public AssayRecord(Assay assay, int rank) {
        this.assay = assay;
        this.edit = true;
        this.outcomeType = assay.getOutcomeType();
        this.rank = rank;
        this.outcome = new SinglePointOutcome(assay.getPossibleUnits().iterator().next().getUnit())
                .setAssay(assay);
    }

    /**
     * constructor
     */
    public AssayRecord(AssayRecordEntity entity, Assay assay, Material material, Item item) {
        this.assay = assay;
        this.edit = false;
        this.material = material;
        this.item = item;
        this.rank = entity.getRank();
        this.recordid = entity.getRecordId();
        this.outcomeType = assay.getOutcomeType();
        outcome = AssayOutcome.fromString(this.outcomeType, entity.getOutcome());
        if (outcome != null) {
            outcome.setAssay(assay);
        }
    }

    public AssayRecordEntity createEntity() {
        AssayRecordEntity entity = new AssayRecordEntity()
                .setExpRecordId(this.assay.getExpRecordId())
                .setRank(this.rank)
                .setRecordId(this.recordid)
                .setOutcome(this.outcome.toString());
        // .setType(this.outcome.getType())

        if (this.item != null) {
            entity.setItemId(this.item.getId());
        }
        if (this.material != null) {
            entity.setMaterialId(this.material.getId());
        }
        return entity;
    }

    public Assay getAssay() {
        return this.assay;
    }

    public boolean isMaterialEditable() {
        return item == null;
    }

    public boolean isItemEditable() {
        return true;
    }

    public boolean isEdit() {
        return edit;
    }

    public String getFacelet() {
        return this.outcomeType.toString();
    }

    public Item getItem() {
        return this.item;
    }

    public Material getMaterial() {
        return this.material;
    }

    public String getMaterialName() {
        if (material != null) {
            return material.getFirstName();
        }
        return "";
    }

    public String getItemLabel() {
        if (item != null) {
            return item.getItemIdPlain();
        }
        return "";
    }

    public AssayOutcome getOutcome() {
        return this.outcome.setAssay(this.assay);
    }

    public int getRank() {
        return this.rank;
    }

    public Long getRecordId() {
        return this.recordid;
    }

    public AssayRecord setAssay(Assay assay) {
        this.assay = assay;
        if (this.outcome != null) {
            this.outcome.setAssay(assay);
        }
        return this;
    }

    public AssayRecord setEdit(boolean edit) {
        this.edit = edit;
        return this;
    }

    /**
     * Sets the item and additionally updates the material if item is not null
     *
     * @param item the item for this record
     * @return this
     */
    public AssayRecord setItem(Item item) {
        this.item = item;
        if (item != null) {
            this.material = item.getMaterial();
        }
        return this;
    }

    /**
     * set the material for this AssayRecord; this method will only succeed if
     * item is not set. Otherwise the item associated material takes precedence.
     *
     * @param material the new material
     * @return this
     */
    public AssayRecord setMaterial(Material material) {
        if (this.item == null) {
            this.material = material;
        }
        return this;
    }

    public AssayRecord setOutcome(AssayOutcome outcome) {
        this.outcome = outcome.setAssay(this.assay);
        // this.outcomeType = outcome.getType();
        return this;
    }

    public AssayRecord setRank(int rank) {
        this.rank = rank;
        return this;
    }

    public AssayRecord setRecordId(Long recordid) {
        this.recordid = recordid;
        return this;
    }
}
