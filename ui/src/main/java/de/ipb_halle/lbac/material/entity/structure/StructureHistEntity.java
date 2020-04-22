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
package de.ipb_halle.lbac.material.entity.structure;

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
@Table(name = "structures_hist")
public class StructureHistEntity implements Serializable {

    @EmbeddedId
    private StructureHistEntityId id;

    @Column
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID actorid;

    @Column
    private String digest;

    @Column
    private String sumformula_old;

    @Column
    private String sumformula_new;

    @Column
    private Double molarmass_old;

    @Column
    private Double molarmass_new;

    @Column
    private Double exactmolarmass_old;

    @Column
    private Double exactmolarmass_new;

    @Column
    private Integer moleculeid_old;

    @Column
    private Integer moleculeid_new;

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

    public String getSumformula_old() {
        return sumformula_old;
    }

    public void setSumformula_old(String sumformula_old) {
        this.sumformula_old = sumformula_old;
    }

    public String getSumformula_new() {
        return sumformula_new;
    }

    public void setSumformula_new(String sumformula_new) {
        this.sumformula_new = sumformula_new;
    }

    public Double getMolarmass_old() {
        return molarmass_old;
    }

    public void setMolarmass_old(Double molarmass_old) {
        this.molarmass_old = molarmass_old;
    }

    public Double getMolarmass_new() {
        return molarmass_new;
    }

    public void setMolarmass_new(Double molarmass_new) {
        this.molarmass_new = molarmass_new;
    }

    public Double getExactmolarmass_old() {
        return exactmolarmass_old;
    }

    public void setExactmolarmass_old(Double exactmolarmass_old) {
        this.exactmolarmass_old = exactmolarmass_old;
    }

    public Double getExactmolarmass_new() {
        return exactmolarmass_new;
    }

    public void setExactmolarmass_new(Double exactmolarmass_new) {
        this.exactmolarmass_new = exactmolarmass_new;
    }

    public Integer getMoleculeid_old() {
        return moleculeid_old;
    }

    public void setMoleculeid_old(Integer moleculeid_old) {
        this.moleculeid_old = moleculeid_old;
    }

    public Integer getMoleculeid_new() {
        return moleculeid_new;
    }

    public void setMoleculeid_new(Integer moleculeid_new) {
        this.moleculeid_new = moleculeid_new;
    }

    public StructureHistEntityId getId() {
        return id;
    }

    public void setId(StructureHistEntityId id) {
        this.id = id;
    }

}
