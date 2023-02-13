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
package de.ipb_halle.lbac.container.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represantation of the database table 'containertypes'
 *
 * @author fmauz
 */
@Entity
@Table(name = "containertypes")
public class ContainerTypeEntity implements Serializable {

    @Id
    private String name;

    @Column
    private Integer rank;

    @Column
    private boolean transportable;

    @Column
    private boolean unique_name;

    /**
     *
     * @return
     */
    public boolean isTransportable() {
        return transportable;
    }

    /**
     *
     * @return
     */
    public boolean isUnique_name() {
        return unique_name;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public Integer getRank() {
        return rank;
    }

    /**
     *
     * @param rank
     */
    public void setRank(Integer rank) {
        this.rank = rank;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param transportable
     */
    public void setTransportable(boolean transportable) {
        this.transportable = transportable;
    }

    /**
     *
     * @param unique_name
     */
    public void setUnique_name(boolean unique_name) {
        this.unique_name = unique_name;
    }

}
