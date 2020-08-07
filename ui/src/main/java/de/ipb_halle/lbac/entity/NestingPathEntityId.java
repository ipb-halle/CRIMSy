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
package de.ipb_halle.lbac.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class NestingPathEntityId implements Serializable {

    private final static long serialVersionUID = 1L;

    private UUID nestingpathsets_id;
    private UUID memberships_id;

    /**
     * Default constructor
     */
    public NestingPathEntityId() {
    }

    /**
     * Constructor
     *
     * @param nestingpathsets_id
     * @param memberships_id
     */
    public NestingPathEntityId(UUID nestingpathsets_id, UUID memberships_id) {
        this.nestingpathsets_id = nestingpathsets_id;
        this.memberships_id = memberships_id;
    }

    /**
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
        final NestingPathEntityId other = (NestingPathEntityId) obj;
        if (!Objects.equals(this.nestingpathsets_id, other.nestingpathsets_id)) {
            return false;
        }
        if (!Objects.equals(this.memberships_id, other.memberships_id)) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return
     */
    public UUID getNestingpathsets_id() {
        return nestingpathsets_id;
    }

    /**
     *
     * @return
     */
    public UUID getMemberships_id() {
        return memberships_id;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.nestingpathsets_id);
        hash = 17 * hash + Objects.hashCode(this.memberships_id);
        return hash;
    }

    /**
     *
     * @param nestingpathsets_id
     */
    public void setNestingpathsets_id(UUID nestingpathsets_id) {
        this.nestingpathsets_id = nestingpathsets_id;
    }

    /**
     *
     * @param memberships_id
     */
    public void setMemberships_id(UUID memberships_id) {
        this.memberships_id = memberships_id;
    }

}
