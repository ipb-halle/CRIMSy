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
package de.ipb_halle.lbac.container;

import de.ipb_halle.lbac.container.entity.ContainerTypeEntity;
import de.ipb_halle.lbac.entity.DTO;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for a containertype. The localized name is NOT set after the creation of
 * an object and must be set afterwards. It overrides the equals and hashcode
 * method so that two types are the same, if the name is the same.
 *
 * @author fmauz
 */
public class ContainerType implements Serializable, DTO {

    public final static int HIGHEST_RANK = 100;

    private String localizedName;
    private String name;
    private int rank;
    private boolean transportable;
    private boolean unique_name;

    /**
     * Contructor by the db entity
     *
     * @param entity
     */
    public ContainerType(ContainerTypeEntity entity) {
        this.name = entity.getName();
        this.rank = entity.getRank();
        this.localizedName = name;
        this.transportable = entity.isTransportable();
        this.unique_name = entity.isUnique_name();
    }

    /**
     * Contructor by attributes
     *
     * @param name not null
     * @param rank
     * @param isTransportable
     * @param isNameUnique
     */
    public ContainerType(String name,
            int rank,
            boolean isTransportable,
            boolean isNameUnique) {
        if (name == null) {
            throw new Error("Name of a containertype must not be null");
        }
        this.name = name;
        this.rank = rank;
        this.localizedName = name;
        this.transportable = isTransportable;
        this.unique_name = isNameUnique;
    }

    @Override
    public ContainerTypeEntity createEntity() {
        ContainerTypeEntity dbe = new ContainerTypeEntity();
        dbe.setName(name);
        dbe.setRank(rank);
        dbe.setTransportable(transportable);
        dbe.setUnique_name(unique_name);
        return dbe;
    }

    /**
     * Two objects are equal, if the other object is of type ContainerType and
     * has the same name
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ContainerType other = (ContainerType) obj;
        return Objects.equals(this.name, other.name);
    }

    /**
     *
     * @return
     */

    public String getLocalizedName() {
        return localizedName;
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
    public int getRank() {
        return rank;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
        return hash;
    }

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
     * @param localizedName
     */
    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
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
     * @param rank
     */
    public void setRank(int rank) {
        this.rank = rank;
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
