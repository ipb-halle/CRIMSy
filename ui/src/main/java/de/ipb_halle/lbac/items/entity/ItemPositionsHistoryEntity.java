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

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "item_positions_history")
public class ItemPositionsHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private int itemid;

    @Column
    private int containerid;
    @Column
    private Integer actorid;
    @Column
    private Date mdate;

    @Column
    private Integer row_old;
    @Column
    private Integer row_new;
    @Column
    private Integer col_old;
    @Column
    private Integer col_new;

    public Integer getRow_old() {
        return row_old;
    }

    public void setRow_old(Integer row_old) {
        this.row_old = row_old;
    }

    public Integer getRow_new() {
        return row_new;
    }

    public void setRow_new(Integer row_new) {
        this.row_new = row_new;
    }

    public Integer getCol_old() {
        return col_old;
    }

    public void setCol_old(Integer col_old) {
        this.col_old = col_old;
    }

    public Integer getCol_new() {
        return col_new;
    }

    public void setCol_new(Integer col_new) {
        this.col_new = col_new;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public int getContainerid() {
        return containerid;
    }

    public void setContainerid(int containerid) {
        this.containerid = containerid;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

}
