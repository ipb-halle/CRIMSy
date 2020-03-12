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
package de.ipb_halle.lbac.entity;

import java.io.Serializable;


/**
 * Represents a List of Users or other Groups as members
 *
 * @author fbroda
 */
public class Group extends Member implements Serializable, DTO {

    private final static long serialVersionUID = 1L;

    /* default constructor */
    public Group() {
        super();
    }

    /*
     * DTO constructor
     */
    public Group(GroupEntity ge, Node node) {
        super((MemberEntity) ge, node);
    }

    /**
     *
     * @return
     */
    @Override
    public GroupEntity createEntity() {
        GroupEntity g = new GroupEntity();
        setMemberEntity(g);
        return g;
    }

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
