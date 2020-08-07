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
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "items_history")
public class ItemHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column
    private String action;

    @Column
    private Double amount_new;

    @Column
    private Double amount_old;

    @Column
    private Double concentration_new;

    @Column
    private Double concentration_old;

    @Column
    private String description_new;

    @Column
    private String description_old;

    @EmbeddedId
    private ItemHistoryId id;

    @Column
    private UUID owner_new;

    @Column
    private UUID owner_old;

    @Column
    private Integer projectid_new;

    @Column
    private Integer projectid_old;

    @Column
    private String purity_new;

    @Column
    private String purity_old;

    public String getAction() {
        return action;
    }

    public Double getAmount_new() {
        return amount_new;
    }

    public Double getAmount_old() {
        return amount_old;
    }

    public Double getConcentration_new() {
        return concentration_new;
    }

    public Double getConcentration_old() {
        return concentration_old;
    }

    public String getDescription_new() {
        return description_new;
    }

    public String getDescription_old() {
        return description_old;
    }

    public ItemHistoryId getId() {
        return id;
    }

    public UUID getOwner_new() {
        return owner_new;
    }

    public UUID getOwner_old() {
        return owner_old;
    }

    public Integer getProjectid_new() {
        return projectid_new;
    }

    public Integer getProjectid_old() {
        return projectid_old;
    }

    public String getPurity_new() {
        return purity_new;
    }

    public String getPurity_old() {
        return purity_old;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setAmount_new(Double amount_new) {
        this.amount_new = amount_new;
    }

    public void setAmount_old(Double amount_old) {
        this.amount_old = amount_old;
    }

    public void setConcentration_new(Double concentration_new) {
        this.concentration_new = concentration_new;
    }

    public void setConcentration_old(Double concentration_old) {
        this.concentration_old = concentration_old;
    }

    public void setDescription_new(String description_new) {
        this.description_new = description_new;
    }

    public void setDescription_old(String description_old) {
        this.description_old = description_old;
    }

    public void setId(ItemHistoryId id) {
        this.id = id;
    }

    public void setOwner_new(UUID owner_new) {
        this.owner_new = owner_new;
    }

    public void setOwner_old(UUID owner_old) {
        this.owner_old = owner_old;
    }

    public void setProjectid_new(Integer projectid_new) {
        this.projectid_new = projectid_new;
    }

    public void setProjectid_old(Integer projectid_old) {
        this.projectid_old = projectid_old;
    }

    public void setPurity_new(String purity_new) {
        this.purity_new = purity_new;
    }

    public void setPurity_old(String purity_old) {
        this.purity_old = purity_old;
    }

}
