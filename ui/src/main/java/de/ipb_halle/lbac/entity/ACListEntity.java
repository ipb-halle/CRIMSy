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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ACLISTS")
public class ACListEntity implements Serializable {

    /**
     * An ACList (access control list) can be assigned to (database) objects to
     * control access permissions of users and groups. In contrast to filesystem
     * ACLs, where each filesystem object stores its own ACL, multiple database
     * records (objects) may point to the same access control list. Special care
     * has to be taken, if the ACL of a database record is changed: It might be
     * necessary to create a new ACL object or the "new" ACL might already exist
     * in the database. In this case, the database record should point to the
     * already existing ACL.
     *
     * ACLists are considered immutable (although implementation is currently
     * not).
     */
    private final static long serialVersionUID = 1L;

    @Id
    private UUID id;

    /**
     * ACLists could be named?
     */
    @Column
    private String name;

    /**
     * The permCode which allows quick checks, wether a given ACList is already
     * known to the system. Only ACLists with equal permCodes need to be
     * compared on equality.
     */
    @Column
    private int permCode;

    /**
     * default constructor
     */
    public ACListEntity() {
        this.id = UUID.randomUUID();
    }

    /**
     * Gets the Id of the ACList
     *
     * @return id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Gets the name of the acList
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the permissioncode of the ACList
     *
     * @return Permissioncode
     */
    public int getPermCode() {
        return this.permCode;
    }

    /**
     * Sets the id of the AC List
     *
     * @param i id of the ACList
     *
     * @return Returns the acList for chaining
     */
    public ACListEntity setId(UUID i) {
        this.id = i;
        return this;
    }

    /**
     * Sets the name of the AC List
     *
     * @param n
     * @return Returns the ACList for chaining
     */
    public ACListEntity setName(String n) {
        this.name = n;
        return this;
    }

    /**
     * 
     * @param permCode
     * @return 
     */
    public ACListEntity setPermCode(int permCode) {
        this.permCode = permCode;
        return this;
    }

}
