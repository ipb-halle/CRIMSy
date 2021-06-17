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

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.Obfuscatable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ACList implements Serializable, Obfuscatable, DTO {

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

    private Integer id;

    /**
     * ACLists could be named
     */
    private String name;

    /**
     * The permCode which allows quick checks, wether a given ACList is already
     * known to the system. Only ACLists with equal permCodes need to be
     * compared on equality.
     */
    private int permCode;

    private Map<Integer, ACEntry> acEntries = new HashMap<>();

    /**
     * default contructor
     */
    public ACList() {
    }

    public ACList(ACListEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.permCode = entity.getPermCode();
    }

    /**
     * add a new ACE to the the ACList. If an ACEntry for Member m already
     * exists, it gets replaced.
     *
     * @param m the member
     * @param perms the array of permissions
     * @return the instance of ACList
     */
    public ACList addACE(Member m, ACPermission[] perms) {
        ACEntry ace = new ACEntry(this, m);
        ace.setPerm(perms);
        this.acEntries.put(m.getId(), ace);
        updatePermCode();
        return this;
    }

    @Override
    public ACListEntity createEntity() {
        updatePermCode();
        return new ACListEntity()
                .setId(id)
                .setName(name)
                .setPermCode(permCode);

    }

    /**
     * Gets map of ACEntries
     *
     * @return
     */
    public Map<Integer, ACEntry> getACEntries() {
        return this.acEntries;
    }

    /**
     * Gets the Id of the ACList
     *
     * @return id
     */
    public Integer getId() {
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
     * @param perm Permissiontype
     * @param m Member to check for. If m is null method returns false
     * @return true if m is granted permission perm
     */
    public boolean getPerm(ACPermission perm, Member m) {
        if (m == null) {
            return false;
        }

        ACEntry ace = this.acEntries.get(m.getId());
        if (ace != null) {
            return ace.getPerm(perm);
        }
        return false;
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
     * wipe any sensitive user information from the list of ACEntries
     */
    @Override
    public void obfuscate() {
        this.acEntries.values().forEach(AE -> AE.obfuscate());
    }

    /**
     * This method uses ACEntry.permEquals to check wether this ACList object
     * contains the given ACEntry. This check ignores the ACList id.
     *
     * @param ae the ACEntry
     * @return returns true if the given ACEntry is contained in this ACList.
     */
    public boolean permContains(ACEntry ae) {
        return ae.permEquals(this.acEntries.get(ae.getMemberId()));
    }

    /**
     * Compare this ACList against another ACList and return true on equality.
     * The comparison ignores the Id fields of the ACList objects. If the nodeId
     * field is set on any of the two objects, it is compared for equality as
     * well.
     *
     * @param acl Object to check against
     * @return true if both lists contain the identical ACEntries (regarding
     * Member and permissions).
     */
    public boolean permEquals(ACList acl) {
        if (acl.getPermCode() != this.permCode) {
            return false;
        }
        if (acl.getACEntries().keySet().size() != this.acEntries.keySet().size()) {
            return false;
        }
        Iterator<Map.Entry<Integer, ACEntry>> iter = this.acEntries.entrySet().iterator();
        while (iter.hasNext()) {
            if (!acl.permContains(iter.next().getValue())) {
                return false;
            }
        }
        return true;
    }

    /*
     * @aces collection of ACEntries
     */
    public ACList setACEntries(java.util.Collection<ACEntry> aces) {
        if (aces != null) {
            for (ACEntry ace : aces) {
                acEntries.put(ace.getMemberId(), ace);
            }
        }
        updatePermCode();
        return this;
    }

    /**
     * Sets the id of the AC List
     *
     * @param i id of the ACList
     *
     * @return Returns the acList for chaining
     */
    public ACList setId(Integer i) {
        this.id = i;
        return this;
    }

    /**
     * Sets the name of the AC List
     *
     * @param n
     * @return Returns the ACList for chaining
     */
    public ACList setName(String n) {
        this.name = n;
        return this;
    }

    /**
     * Readable representation of an acList
     *
     * @return Readable representation of an acList
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ACL id=");
        sb.append(getId().toString());
        this.acEntries.values().stream().forEach(x -> sb.append("\n   ").append(x.toString()));
        sb.append("\n");
        return sb.toString();
    }

    /**
     * The permCode allows to quickly compare two ACLists on equality. The value
     * of the id field is ignored. This method is used prior to persisting this
     * object to the database. If an object with the same permCode exists, it is
     * compared by the permEquals method so that only unique ACLists are stored
     * (i.e. there are no ACLists which differ only in their id).
     */
    public void updatePermCode() {
        int p = 0;
        Iterator<Map.Entry<Integer, ACEntry>> iter = this.acEntries.entrySet().iterator();
        while (iter.hasNext()) {
            p += iter.next().getValue().permCode();
        }
        this.permCode = p;
    }

}
