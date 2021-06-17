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
package de.ipb_halle.lbac.admission;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a List of Users or other Groups as members
 *
 * @author fbroda
 */
@Entity
@DiscriminatorValue("G")
public class GroupEntity extends MemberEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public String toString() {
        return "Group{id=" + getId() + ", name="
                + getName() + ",\n     node="
                + this.getNode() + "}";
    }

}
