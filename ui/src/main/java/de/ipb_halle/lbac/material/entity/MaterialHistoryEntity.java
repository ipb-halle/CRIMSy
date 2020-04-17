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
package de.ipb_halle.lbac.material.entity;

import de.ipb_halle.lbac.message.LocalUUIDConverter;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.johnzon.mapper.JohnzonConverter;

/**
 *
 * @author fmauz
 */
@Entity
 @Table(name = "materials_hist")
public class MaterialHistoryEntity implements Serializable {

    @EmbeddedId
    private MaterialHistoryId id;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID actorid;

    @Column
    private String digest;

    @Column
    private String action;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID aclistid_old;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID aclistid_new;

    @Column
    private Integer projectid_old;

    @Column
    private Integer projectid_new;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID ownerid_old;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID ownerid_new;

    public MaterialHistoryId getId() {
        return id;
    }

    public void setId(MaterialHistoryId id) {
        this.id = id;
    }

    public UUID getActorid() {
        return actorid;
    }

    public void setActorid(UUID actorid) {
        this.actorid = actorid;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public UUID getAclistid_old() {
        return aclistid_old;
    }

    public void setAclistid_old(UUID aclistid_old) {
        this.aclistid_old = aclistid_old;
    }

    public UUID getAclistid_new() {
        return aclistid_new;
    }

    public void setAclistid_new(UUID aclistid_new) {
        this.aclistid_new = aclistid_new;
    }

    public Integer getProjectid_old() {
        return projectid_old;
    }

    public void setProjectid_old(Integer projectid_old) {
        this.projectid_old = projectid_old;
    }

    public Integer getProjectid_new() {
        return projectid_new;
    }

    public void setProjectid_new(Integer projectid_new) {
        this.projectid_new = projectid_new;
    }

    public UUID getOwnerid_old() {
        return ownerid_old;
    }

    public void setOwnerid_old(UUID ownerid_old) {
        this.ownerid_old = ownerid_old;
    }

    public UUID getOwnerid_new() {
        return ownerid_new;
    }

    public void setOwnerid_new(UUID ownerid_new) {
        this.ownerid_new = ownerid_new;
    }

}
