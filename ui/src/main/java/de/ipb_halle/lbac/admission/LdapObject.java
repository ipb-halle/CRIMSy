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

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapObject implements Serializable {

    private transient Logger logger = LogManager.getLogger(this.getClass().getName());

    private static final long serialVersionUID = 1L;

    /**
     * objects distinguished ldap name
     */
    private String dn;

    /**
     * email (only for user objects)
     */
    private String email;

    /**
     * login (may change on marriage!)
     */
    private String login;

    /**
     * email (only for user objects)
     */
    private String phone;

    /**
     * LBAC id (may be null for users not yet known to LBAC)
     */
    private Integer id;

    /**
     * list of DN's, this object is memberOf
     */
    private List<String> membership;

    /**
     * name of group, user, or role
     */
    private String name;

    /**
     * type of LdapObject (GROUP, ROLE, USER)
     */
    private MemberType type;

    /**
     * LDAP unique identifier (e.g. objectId, objectSID)
     */
    private String uniqueId;

    /**
     * default constructor
     */
    public LdapObject() {
        this.membership = new ArrayList<>();
    }

    /**
     * @param dn distinguished name of a group, this object is MEMBER_OF
     * @return
     */
    public LdapObject addMembership(String dn) {
        this.membership.add(dn);
        return this;
    }

    /**
     * create a LBAC Group object from this objects data
     *
     * @return a User object (or null)
     */
    public Group createGroup() {
        if (this.type == MemberType.GROUP) {
            Group g = new Group();
            g.setName(this.name);
            g.setSubSystemData(this.uniqueId);
            g.setSubSystemType(AdmissionSubSystemType.LDAP);
            if (this.id != null) {
                g.setId(this.id);
            } else {
                this.id = g.getId();
            }
            return g;
        }
        return null;
    }

    /**
     * create a LBAC User object from this objects data
     *
     * @return a User object (or null)
     */
    public User createUser() {
        if (this.type == MemberType.USER) {
            User u = new User();
            u.setEmail(this.email);
            u.setLogin(this.login);
            u.setName(this.name);
            u.setPhone(this.phone);
            u.setSubSystemData(this.uniqueId);
            u.setSubSystemType(AdmissionSubSystemType.LDAP);
            if (this.id != null) {
                u.setId(this.id);
            } else {
                this.id = u.getId();
            }
            return u;
        }
        return null;
    }

    /**
     * @return distinguished name of object
     */
    public String getDN() {
        return this.dn;
    }

    /**
     * @return email address
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * @return login
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * @return list of MEMBER_OF distinguished names. In contrast to LBAC
     * membership semantics, this list will never contain references to itself.
     */
    public List<String> getMemberships() {
        return this.membership;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return type of object (i.e. "ROLE", "GROUP", or "USER")
     */
    public MemberType getType() {
        return this.type;
    }

    /**
     * @return the unique LDAP id
     */
    public String getUniqueId() {
        return uniqueId;
    }

    public LdapObject setDN(String s) {
        this.dn = s;
        return this;
    }

    public LdapObject setEmail(String s) {
        this.email = s;
        return this;
    }

    public LdapObject setId(Integer u) {
        this.id = u;
        return this;
    }

    public LdapObject setLogin(String s) {
        this.login = s;
        return this;
    }

    public LdapObject setName(String s) {
        this.name = s;
        return this;
    }

    public LdapObject setPhone(String s) {
        this.phone = s;
        return this;
    }

    public LdapObject setType(MemberType t) {
        this.type = t;
        return this;
    }

    public LdapObject setUniqueId(String s) {
        this.uniqueId = s;
        return this;
    }

    public void debug() {
        logger.info("Found LDAB USER:");
        logger.info("uniqueId : " + uniqueId);
        logger.info("dn : " + dn);
        logger.info("email : " + email);
        logger.info("id : " + id);
        logger.info("login : " + login);
        logger.info("name : " + name);
        logger.info("phone : " + phone);
        logger.info("type : " + type);
        logger.info("uniqueId : " + uniqueId);
    }
}
