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

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.IAdmissionSubSystem;
import de.ipb_halle.lbac.message.LocalUUIDConverter;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.johnzon.mapper.JohnzonConverter;

/**
 * Base class for users and groups Users and groups are managed hierarchically,
 * i.e. groups may be members of groups and memberships are inherited. As users
 * and groups are synchronized among cloud nodes, one needs to (a) keep track of
 * updates and (b) remove objects which have disappeared (been removed) on the
 * source node.
 */
public abstract class Member implements Obfuscatable, DTO {

    private UUID id;

    private String name;

    /**
     * keeps track of the node id which is authoritative for this Member object
     */
    private Node node;

    /**
     * subSystem which manages the user or group information. This could be the
     * local database, LDAP, Shibboleth or some other source.
     */
    private AdmissionSubSystemType subSystemType;

    /**
     * The subSystemData stores information to identify the instance of the
     * subSystem, e.g. a specific server.
     */
    private String subSystemData;

    /**
     * The locally cached information should be valid only for a certain time
     * period, after which it should be updated from an authoritative subSystem
     * (e.g. LDAP or a remote node).
     */
    private Date modified;

    /**
     * default constructor
     */
    public Member() {
        this.id = UUID.randomUUID();
        this.modified = new Date();
    }

    /**
     * DTO constructor
     */
    public Member(MemberEntity me, Node node) {
        this.id = me.getId();
        this.name = me.getName();
        this.node = node;
        this.subSystemData = me.getSubSystemData();
        this.subSystemType = me.getSubSystemType();
        this.modified = me.getModified();
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

    public UUID getId() {
        return this.id;
    }

    public Date getModified() {
        return this.modified;
    }

    public String getName() {
        return this.name;
    }

    public Node getNode() {
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

    /**
     * obfuscate secrets
     */
    @Override
    public void obfuscate() {
        this.subSystemData = null;
        this.subSystemType = null;
    }

    public void setId(UUID i) {
        this.id = i;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setNode(Node n) {
        this.node = n;
    }

    /**
     * set entity values 
     */
    public void setMemberEntity(MemberEntity me) {
        me.setId(this.id);
        me.setModified(this.modified);
        me.setName(this.name);
        me.setNode(this.node.getId());
        me.setSubSystemData(this.subSystemData);
        me.setSubSystemType(this.subSystemType);
    }

    public void setModified(Date d) {
        this.modified = d;
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
