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

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Access control entry An access control entry (ACEntry) belongs to a specific
 * access control list (ACList). It will be automatically cleaned up, when a
 * ACList gets deleted.
 */
@Entity
@Table(name = "ACENTRIES")
public class ACEntryEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @EmbeddedId
    private ACEntryId id;

    /**
     * Permission to read an object
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_READ)
    private boolean permREAD;

    /**
     * Permission to edit an object (may or may not include permission to create
     * the object in the first place.
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_EDIT)
    private boolean permEDIT;

    /**
     * Permission to create an object. This does NOT include permission to edit
     * the object afterwards
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_CREATE)
    private boolean permCREATE;

    /**
     * Permission to delete an object
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_DELETE)
    private boolean permDELETE;

    /**
     * Permission to change ownership of an object (without need of confirmation
     * of the receiving party).
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_CHOWN)
    private boolean permCHOWN;

    /**
     * Permission to grant permissions to other people (This does NOT include
     * the Super privilege?).
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_GRANT)
    private boolean permGRANT;

    /**
     * This permission should include ALL other permissions and (administrative)
     * actions which do not fit anywhere else. Should be used rather rarely!
     */
    @Column
    @AttributeTag(type=AttributeType.PERM_SUPER)
    private boolean permSUPER;

    /* default constructor */
    public ACEntryEntity() {
        this.id = new ACEntryId();
    }
    
    /**
     * compares on equality; ignores permissions
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof ACEntryEntity)) {
            ACEntryEntity ae = (ACEntryEntity) o;
            return this.id.equals(ae.getId());
        }
        return false;
    }

    public ACEntryId getId() {
        return id;
    }

    
    /**
     * computes a permission code by assigning bit values (1, 2, 4, ...) to the
     * individual permissions.
     *
     * @return the permcode
     */
    public int getPerm() {
        return (permREAD ? 1 << 0 : 0)
                + (permEDIT ? 1 << 1 : 0)
                + (permCREATE ? 1 << 2 : 0)
                + (permDELETE ? 1 << 3 : 0)
                + (permCHOWN ? 1 << 4 : 0)
                + (permGRANT ? 1 << 5 : 0)
                + (permSUPER ? 1 << 6 : 0);
    }


    public boolean getPermRead() {
        return this.permREAD;
    }

    public boolean getPermEdit() {
        return this.permEDIT;
    }

    public boolean getPermCreate() {
        return this.permCREATE;
    }

    public boolean getPermDelete() {
        return this.permDELETE;
    }

    public boolean getPermChown() {
        return this.permCHOWN;
    }

    public boolean getPermGrant() {
        return this.permGRANT;
    }

    public boolean getPermSuper() {
        return this.permSUPER;
    }

    /**
     * compute the permission independent hash code
     */
    @Override
    public int hashCode() {
        return ((id.getMemberId()!= null) ? id.getMemberId().hashCode() : 0)
                + ((id.getAclId()!= null) ? id.getAclId().hashCode() : 0);
    }

    /**
     * permCode is computed to quickly compare two ACLists. The permCode is
     * independent of the aclist_id.
     */
    public int permCode() {
        return getPerm() + id.getMemberId().hashCode();
    }

    /**
     * permEquals returns true if the ACEntry have the same Member and
     * permissions are set identical. This is needed for comparing two ACList
     * objects. The aclist_id is ignored in this comparison.
     */
    public boolean permEquals(ACEntryEntity ae) {
        if ((ae != null)
                && (ae.getPerm() == getPerm())
                && ae.id.getMemberId().equals(id.getMemberId())) {
            return true;
        }
        return false;
    }

    /**
     * set the Id of this ACEntry
     *
     * @param i the id
     */
    public void setId(ACEntryId i) {
        this.id = i;
    }


    public void setPermRead(boolean b) {
        this.permREAD = b;
    }

    public void setPermEdit(boolean b) {
        this.permEDIT = b;
    }

    public void setPermCreate(boolean b) {
        this.permCREATE = b;
    }

    public void setPermDelete(boolean b) {
        this.permDELETE = b;
    }

    public void setPermChown(boolean b) {
        this.permCHOWN = b;
    }

    public void setPermGrant(boolean b) {
        this.permGRANT = b;
    }

    public void setPermSuper(boolean b) {
        this.permSUPER = b;
    }

    /**
     *
     * @return returns a readable represantation of an AclistEntry
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ACE ");
        sb.append(this.id.getMemberId().toString());
        sb.append(" -> ");
        sb.append(this.permREAD ? "READ " : "");
        sb.append(this.permEDIT ? "EDIT " : "");
        sb.append(this.permCREATE ? "CREATE " : "");
        sb.append(this.permDELETE ? "DELETE " : "");
        sb.append(this.permCHOWN ? "CHOWN " : "");
        sb.append(this.permGRANT ? "GRANT " : "");
        sb.append(this.permSUPER ? "SUPER" : "");
        return sb.toString();
    }

}
