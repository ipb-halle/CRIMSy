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

import de.ipb_halle.crimsy_api.DTO;
import de.ipb_halle.lbac.entity.Obfuscatable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Access control entry An access control entry (ACEntry) belongs to a specific
 * access control list (ACList). It will be automatically cleaned up, when a
 * ACList gets deleted.
 */
public class ACEntry implements DTO<ACEntryEntity>, Serializable, Obfuscatable {

    private static final long serialVersionUID = 1L;

    private ACList aclist;

    private Member member;

    /**
     * Permission to read an object
     */
    private boolean permREAD;

    /**
     * Permission to edit an object (may or may not include permission to create
     * the object in the first place.
     */
    private boolean permEDIT;

    /**
     * Permission to create an object. This does NOT include permission to edit
     * the object afterwards
     */
    private boolean permCREATE;

    /**
     * Permission to delete an object
     */
    private boolean permDELETE;

    /**
     * Permission to change ownership of an object (without need of confirmation
     * of the receiving party).
     */
    private boolean permCHOWN;

    /**
     * Permission to grant permissions to other people (This does NOT include
     * the Super privilege?).
     */
    private boolean permGRANT;

    /**
     * This permission should include ALL other permissions and (administrative)
     * actions which do not fit anywhere else. Should be used rather rarely!
     */
    private boolean permSUPER;

    /**
     * Default constructor
     */
    public ACEntry() {
    }

    public ACEntry(ACList aclist, Member member) {
        this.aclist = aclist;
        this.member = member;
    }

    /**
     * Constructor
     *
     * @param en
     * @param acl
     * @param m
     */
    public ACEntry(ACEntryEntity en, ACList acl, Member m) {
        this.member = m;
        this.aclist = acl;
        this.permREAD = en.getPermRead();
        this.permEDIT = en.getPermEdit();
        this.permCREATE = en.getPermCreate();
        this.permCHOWN = en.getPermChown();
        this.permDELETE = en.getPermDelete();
        this.permGRANT = en.getPermGrant();
        this.permSUPER = en.getPermSuper();
    }

    /**
     * Creates a hibernate db entity
     *
     * @return
     */
    @Override
    public ACEntryEntity createEntity() {
        ACEntryEntity entity = new ACEntryEntity();
        entity.setId(new ACEntryId(aclist.getId(), member.getId()));
        entity.setPermChown(permCHOWN);
        entity.setPermCreate(permCREATE);
        entity.setPermDelete(permDELETE);
        entity.setPermEdit(permEDIT);
        entity.setPermGrant(permGRANT);
        entity.setPermRead(permREAD);
        entity.setPermSuper(permSUPER);
        return entity;
    }

    /**
     * compares on equality; ignores permissions
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ACEntry) {
            ACEntry ae = (ACEntry) o;
            if ((getACListId() != null) && getACListId().equals(ae.getACListId())
                    && (getMemberId() != null) && getMemberId().equals(ae.getMemberId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the ACList
     *
     * @return
     */
    public ACList getACList() {
        return this.aclist;
    }

    /**
     * Gets the id of the AC List
     *
     * @return
     */
    public Integer getACListId() {
        return (this.aclist != null) ? this.aclist.getId() : null;
    }

    /*
     * @return the member of this ACEntry
     */
    public Member getMember() {
        return this.member;
    }

    /**
     * @return the member Id or null if member is not set.
     */
    public Integer getMemberId() {
        return (this.member != null) ? this.member.getId() : null;
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

    /**
     * Returns the permissionstate of a permission type
     *
     * @param p Type of Permission
     * @return is the requested type of permission granted to the user
     */
    public boolean getPerm(ACPermission p) {
        switch (p) {
            case permREAD:
                return this.permREAD;
            case permEDIT:
                return this.permEDIT;
            case permCREATE:
                return this.permCREATE;
            case permDELETE:
                return this.permDELETE;
            case permCHOWN:
                return this.permCHOWN;
            case permGRANT:
                return this.permGRANT;
            case permSUPER:
                return this.permSUPER;
        }
        return false;
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
        return ((getMemberId() != null) ? getMemberId().hashCode() : 0)
                + ((getACListId() != null) ? getACListId().hashCode() : 0);
    }

    /**
     * wipe any sensitive information
     */
    @Override
    public void obfuscate() {
        this.member.obfuscate();
    }

    /**
     * permCode is computed to quickly compare two ACLists.The permCode is
     * independent of the aclist_id.
     *
     * @return
     */
    public int permCode() {
        return getPerm() + getMemberId().hashCode();
    }

    /**
     * permEquals returns true if the ACEntry have the same Member and
     * permissions are set identical.This is needed for comparing two ACList
     * objects.The aclist_id is ignored in this comparison.
     *
     * @param ae
     * @return
     */
    public boolean permEquals(ACEntry ae) {
        return ((ae != null)
                && (ae.getPerm() == getPerm())
                && ae.getMemberId().equals(getMemberId()));

    }

    /**
     * set the parent ACList for this ACEntry
     */
    public void setACList(ACList acl) {
        this.aclist = acl;
    }

    /**
     * set the member for this ACEntry
     */
    public void setMember(Member m) {
        this.member = m;
    }

    /**
     * set the permission variables according to the given permissions
     */
    public void setPerm(ACPermission[] perms) {
        if (perms == null) {
            return;
        }
        for (ACPermission p : perms) {
            switch (p) {
                case permREAD:
                    this.permREAD = true;
                    break;
                case permEDIT:
                    this.permEDIT = true;
                    break;
                case permCREATE:
                    this.permCREATE = true;
                    break;
                case permDELETE:
                    this.permDELETE = true;
                    break;
                case permCHOWN:
                    this.permCHOWN = true;
                    break;
                case permGRANT:
                    this.permGRANT = true;
                    break;
                case permSUPER:
                    this.permSUPER = true;
                    break;
            }
        }
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
        sb.append(this.member.getId().toString());
        sb.append(" (");
        sb.append(this.member.getName());
        sb.append(") -> ");
        sb.append(this.permREAD ? "READ " : "");
        sb.append(this.permEDIT ? "EDIT " : "");
        sb.append(this.permCREATE ? "CREATE " : "");
        sb.append(this.permDELETE ? "DELETE " : "");
        sb.append(this.permCHOWN ? "CHOWN " : "");
        sb.append(this.permGRANT ? "GRANT " : "");
        sb.append(this.permSUPER ? "SUPER" : "");
        return sb.toString();
    }

    public ACPermission[] getAcPermissionArray() {
        List<ACPermission> perms = new ArrayList<>();
        if (permCHOWN) {
            perms.add(ACPermission.permCHOWN);
        }
        if (permCREATE) {
            perms.add(ACPermission.permCREATE);
        }
        if (permDELETE) {
            perms.add(ACPermission.permDELETE);
        }
        if (permEDIT) {
            perms.add(ACPermission.permEDIT);
        }
        if (permGRANT) {
            perms.add(ACPermission.permGRANT);
        }
        if (permREAD) {
            perms.add(ACPermission.permREAD);
        }
        if (permSUPER) {
            perms.add(ACPermission.permSUPER);
        }
        return perms.toArray(new ACPermission[perms.size()]);
    }

}
