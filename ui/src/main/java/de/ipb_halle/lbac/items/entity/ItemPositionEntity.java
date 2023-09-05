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
package de.ipb_halle.lbac.items.entity;

import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fbroda
 */
@Entity
@Table(name = "item_positions")
public class ItemPositionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private Integer containerid;

    @Column
    private Integer itemid;

    @Column
    private Integer itemrow;

    @Column
    private Integer itemcol;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getContainerId() {
        return containerid;
    }

    public void setContainerId(Integer containerid) {
        this.containerid = containerid;
    }

    public Integer getItemId() {
        return itemid;
    }

    public void setItemId(Integer itemid) {
        this.itemid = itemid;
    }

    public Integer getItemRow() {
        return itemrow;
    }

    public void setItemRow(Integer itemrow) {
        this.itemrow = itemrow;
    }

    public Integer getItemCol() {
        return itemcol;
    }

    public void setItemCol(Integer itemcol) {
        this.itemcol = itemcol;
    }
}
