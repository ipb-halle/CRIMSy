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
package de.ipb_halle.lbac.datalink;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.Payload;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A record object linking to other entities like Material,
 * Item, Document and possibly carrying additional data 
 * (e.g. in case of assay outcomes). Payload data is stored 
 * as JSON in the database. The records are ordered by rank, counting from
 * zero. Reordering of records is deemed unnecessary and _currently_ not
 * planned.
 *
 * @author fbroda
 */
public class LinkedData implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Long recordid;
    private ExpRecord expRecord;
    private Material material;
    private Item item;
//    private Document document;
    private int rank;
    private Payload payload;
    private LinkedDataType linkedDataType;

    private transient boolean edit;
    private transient int index;

    /**
     * constructor
     */
    public LinkedData(ExpRecord expRecord, LinkedDataType type, int rank) {
        this.expRecord = expRecord;
        this.edit = true;
        this.rank = rank;
        this.linkedDataType = type;
    }

    /**
     * constructor
     */
    public LinkedData(LinkedDataEntity entity, ExpRecord expRecord, Material material, Item item) {
        this.expRecord = expRecord;
        this.edit = false;
        this.material = material;
        this.item = item;
        this.rank = entity.getRank();
        this.recordid = entity.getRecordId();
        this.linkedDataType = entity.getType();
        if (entity.getPayload() != null) {
            this.payload = Payload.fromString(this.linkedDataType, entity.getPayload());
            if (this.payload != null) {
                payload.setExpRecord(expRecord);
            }
        }
    }

    public LinkedDataEntity createEntity() {
        LinkedDataEntity entity = new LinkedDataEntity()
                .setExpRecordId(this.expRecord.getExpRecordId())
                .setRank(this.rank)
                .setRecordId(this.recordid)
                .setType(this.linkedDataType);
        
        if (this.payload != null) {
            entity.setPayload(this.payload.toString());
        }

        if (this.item != null) {
            entity.setItemId(this.item.getId());
        }
        if (this.material != null) {
            entity.setMaterialId(this.material.getId());
        }
        return entity;
    }

    public ExpRecord getExpRecord() {
        return this.expRecord;
    }

    public boolean isMaterialEditable() {
        return item == null && edit;
    }

    public boolean isItemEditable() {
        return edit;
    }

    public boolean isEdit() {
        return edit;
    }

    public String getFacelet() {
        return this.linkedDataType.toString();
    }

    public int getIndex() {
        return this.index;
    }

    public Item getItem() {
        return this.item;
    }

    public String getItemLabel() {
        if (item != null) {
            return item.getItemIdPlain();
        }
        return "";
    }

    public LinkedDataType getLinkedDataType() {
        return this.linkedDataType;
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

    public Payload getPayload() {
        if (this.payload != null) {
            return this.payload.setExpRecord(this.expRecord);
        }
        return null;
    }

    public int getRank() {
        return this.rank;
    }

    public Long getRecordId() {
        return this.recordid;
    }

    public LinkedData setExpRecord(ExpRecord expRecord) {
        this.expRecord = expRecord;
        if (this.payload != null) {
            this.payload.setExpRecord(expRecord);
        }
        return this;
    }

    public LinkedData setEdit(boolean edit) {
        this.edit = edit;
        return this;
    }

    public LinkedData setIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * Sets the item and additionally updates the material if item is not null
     *
     * @param item the item for this record
     * @return this
     */
    public LinkedData setItem(Item item) {
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
    public LinkedData setMaterial(Material material) {
        if (this.item == null) {
            this.material = material;
        }
        return this;
    }

    public LinkedData setPayload(Payload payload) {
        this.payload = payload;
        if (payload != null) {
            this.payload.setExpRecord(this.expRecord);
        }
        return this;
    }

    public LinkedData setRank(int rank) {
        this.rank = rank;
        return this;
    }

    public LinkedData setRecordId(Long recordid) {
        this.recordid = recordid;
        return this;
    }
}
