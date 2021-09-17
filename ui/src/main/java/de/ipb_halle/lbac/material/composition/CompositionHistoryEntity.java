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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.biomaterial.*;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "components_history")
public class CompositionHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CompositionHistEntityId id;

    @Column
    private String digest;
    @Column
    private String action;
    @Column
    private Integer materialid_old;
    @Column
    private Integer materialid_new;
    @Column
    private Double concentration_old;
    @Column
    private Double concentration_new;

    public CompositionHistEntityId getId() {
        return id;
    }

    public CompositionHistoryEntity setId(CompositionHistEntityId id) {
        this.id = id;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public CompositionHistoryEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getAction() {
        return action;
    }

    public CompositionHistoryEntity setAction(String action) {
        this.action = action;
        return this;
    }

    public Integer getMaterialid_old() {
        return materialid_old;
    }

    public void setMaterialid_old(Integer materialid_old) {
        this.materialid_old = materialid_old;
    }

    public Integer getMaterialid_new() {
        return materialid_new;
    }

    public void setMaterialid_new(Integer materialid_new) {
        this.materialid_new = materialid_new;
    }

    public Double getConcentration_old() {
        return concentration_old;
    }

    public void setConcentration_old(Double concentration_old) {
        this.concentration_old = concentration_old;
    }

    public Double getConcentration_new() {
        return concentration_new;
    }

    public void setConcentration_new(Double concentration_new) {
        this.concentration_new = concentration_new;
    }

}
