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

import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Base class for users and groups Users and groups are managed hierarchically,
 * i.e. groups may be members of groups and memberships are inherited. As users
 * and groups are synchronized among cloud nodes, one needs to (a) keep track of
 * updates and (b) remove objects which have disappeared (been removed) on the
 * source node.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "USERSGROUPS")
@DiscriminatorColumn(name = "memberType", discriminatorType = DiscriminatorType.STRING, length = 1)
public abstract class MemberEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    @AttributeTag(type=AttributeType.MEMBER_NAME)
    private String name;

    /**
     * keeps track of the node id which is authoritative for this MemberEntity
     * object
     */
    @Column(name = "node_id")
    private UUID node;

    /**
     * subSystem which manages the user or group information. This could be the
     * local database, LDAP, Shibboleth or some other source.
     */
    @Column
    @Enumerated(EnumType.ORDINAL)
    private AdmissionSubSystemType subSystemType;

    /**
     * The subSystemData stores information to identify the instance of the
     * subSystem, e.g. a specific server.
     */
    @Column
    private String subSystemData;

    /**
     * The locally cached information should be valid only for a certain time
     * period, after which it should be updated from an authoritative subSystem
     * (e.g. LDAP or a remote node).
     */
    @Column
    private Date modified;

    /**
     * default constructor
     */
    public MemberEntity() {
        this.modified = new Date();
    }

    /**
     * The equals method returns true if<ol>
     * <li>o is non-null</li>
     * <li>both objects are instances of the same class (and therefore
     * subclasses of Member)</li>
     * <li>the id field for both objects is not null and equal</li>
     * </ol>
     *
     * @param o
     * @return true if both objects are equal, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && o.getClass().equals(this.getClass())) {
            Member m = (Member) o;
            if ((getId() != null) && getId().equals(m.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public Integer getId() {
        return this.id;
    }

    public Date getModified() {
        return this.modified;
    }

    public String getName() {
        return this.name;
    }

    public UUID getNode() {
        return this.node;
    }

    public IAdmissionSubSystem getSubSystem() {
        return this.subSystemType.getInstance();
    }

    public String getSubSystemData() {
        return this.subSystemData;
    }

    public AdmissionSubSystemType getSubSystemType() {
        return this.subSystemType;
    }

    public abstract boolean isGroup();

    public abstract boolean isUser();

    public void setId(Integer i) {
        this.id = i;
    }

    public void setModified(Date d) {
        this.modified = d;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setNode(UUID n) {
        this.node = n;
    }

    public void setSubSystemData(String d) {
        this.subSystemData = d;
    }

    public void setSubSystemType(AdmissionSubSystemType t) {
        this.subSystemType = t;
    }

    public void updateModified() {
        this.modified = new Date();
    }
}
