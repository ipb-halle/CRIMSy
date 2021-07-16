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

import de.ipb_halle.lbac.util.HexUtil;

import java.io.Serializable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class LdapHelper implements Serializable {

    /**
     * This class performs all LDAP lookups and synchronizes the local node with
     * settings from LDAP
     */
    private final static long serialVersionUID = 1L;

    private Logger logger;

    private LdapProperties ldapProperties;

    /**
     * constructor
     *
     * @param prop
     */
    public LdapHelper() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * authenticate (i.e. check password of) user
     *
     * @param l login
     * @param p password
     * @return true if presented password p authenticates the user with login l
     * @throws Exception upon any LDAP problems
     */
    public boolean authenticate(String l, String p) throws Exception {
        if (this.ldapProperties == null) {
            logger.warn("LDAP Properties not initialized.(Authentificate)");
            return false;
        }
        try {
            DirContext ctx = new InitialDirContext(this.ldapProperties.getLdapEnv());
            try {
                SearchControls ctrl = new SearchControls();
                ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

                NamingEnumeration<SearchResult> srch = ctx.search(this.ldapProperties.get("LDAP_BASE_DN"),
                        this.ldapProperties.get("LDAP_SEARCH_FILTER_LOGIN").replaceAll("@", l), ctrl);
                while (srch.hasMore()) {
                    SearchResult result = srch.next();
                    String dn = result.getNameInNamespace();
                    srch.close();
                    ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
                    ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, p);
                    DirContext lx = (DirContext) ctx.lookup(dn);
                    return lx.getNameInNamespace().equals(dn);
                }
            } finally {
                ctx.close();
            }
        } catch (Exception e) {
            this.logger.warn("authenticate() caught an exception: ", (Throwable) e);
            throw new Exception(e);
        }
        return false;
    }

    /**
     * Filter the group memberships. Only matching groups are used within LBAC
     * to prevent leaking of internal information.
     *
     * @param dn the distinguished name of the group
     * @return true if the group dn endsWith(LDAP_GROUP_FILTER_DN)
     */
    protected boolean filterGroup(String dn) {
        String filter = ldapProperties.get("LDAP_GROUP_FILTER_DN");

        try {
            LdapName name = new LdapName(dn);

            try {
                LdapName filterName = new LdapName(filter);

                return name.startsWith(filterName);
            } catch (InvalidNameException f) {
                this.logger.warn("filterGroup() invalid filter expression:" + filter);
                return false;
            }
        } catch (InvalidNameException e) {
            this.logger.warn("filterGroup() invalid name: " + dn);
            return false;
        }
    }

    /**
     * recursively query LDAP for nested group memberships
     *
     * @param ctx directory context
     * @param groupDN distinguished name of group
     * @param ldapObjects a map containing the memberships, LdapObject mapped by
     * DN
     */
    private void queryLdapGroups(DirContext ctx, String groupDN, Map<String, LdapObject> ldapObjects) {
        try {
            Attributes attrs = ctx.getAttributes(groupDN);
            String cn = (String) attrs.get(this.ldapProperties.get("LDAP_ATTR_COMMON_NAME")).get();
            String uniqueId = HexUtil.toHex(attrs.get(this.ldapProperties.get("LDAP_ATTR_UNIQUE_ID")).get().toString().getBytes());

            LdapObject group = new LdapObject()
                    .setDN(groupDN)
                    .setName(cn)
                    .setType(MemberType.GROUP)
                    .setUniqueId(uniqueId);

            // add early to cache to prevent loops
            ldapObjects.put(groupDN, group);
            try {
                NamingEnumeration<?> groups = ((BasicAttribute) attrs.get(this.ldapProperties.get("LDAP_ATTR_MEMBER_OF"))).getAll();
                while (groups.hasMore()) {
                    String memberdn = groups.next().toString();
//                  this.logger.info("queryLdapGroups(): found member dn " + memberdn + " for group " + groupDN);
                    if (filterGroup(memberdn)) {
                        group.addMembership(memberdn);
                        // prevent loops
                        if (ldapObjects.get(memberdn) == null) {
                            queryLdapGroups(ctx, memberdn, ldapObjects);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                // will occur for groups which are not members elsewhere and can be ignored safely
            }
        } catch (Exception e) {
            this.logger.warn("queryLdapGroups() caught exception: ", e);
        }
    }

    /**
     * query the LDAP for current user data, i.e. name (cn), mail and ldap group
     * memberships. Nested group memberships are subsequently resolved.
     *
     * @param login the login
     * @param ldapObjects map of LdapObjects by DN for storing memberships. If
     * ldapObjects is null, no recursive lookup of memberships will be performed
     * @return ldap object of user
     */
    public LdapObject queryLdapUser(String login, Map<String, LdapObject> ldapObjects) {
        if (this.ldapProperties == null) {
            logger.warn("LDAP properties not initialized(queryLdapUser).");
            return null;
        }
        try {
            DirContext ctx = new InitialDirContext(this.ldapProperties.getLdapEnv());
            try {
                SearchControls ctrl = new SearchControls();
                ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> srch = ctx.search(this.ldapProperties.get("LDAP_BASE_DN"),
                        this.ldapProperties.get("LDAP_SEARCH_FILTER_LOGIN").replaceAll("@", login), ctrl);
                while (srch.hasMore()) {
                    SearchResult result = srch.next();
                    String dn = result.getNameInNamespace();
                    Attributes attrs = result.getAttributes();
                    String cn = (String) attrs.get(this.ldapProperties.get("LDAP_ATTR_COMMON_NAME")).get();
                    String mail = "";
                    String phone = "";
                    String uniqueId = login;
                    try {
                        uniqueId = HexUtil.toHex(attrs.get(this.ldapProperties.get("LDAP_ATTR_UNIQUE_ID")).get().toString().getBytes());
                        String prop = this.ldapProperties.get("LDAP_ATTR_EMAIL");
                        mail = ((prop != null) && (prop.length() > 0)) ? (String) attrs.get(prop).get() : "";
                        prop = this.ldapProperties.get("LDAP_ATTR_PHONE");
                        phone = ((prop != null) && (prop.length() > 0)) ? (String) attrs.get(prop).get() : "";
                    } catch (NullPointerException ne) {
                        this.logger.warn("queryLdapUser(): user has no valid email address or phone number");
                    }
                    LdapObject user = new LdapObject()
                            .setType(MemberType.USER)
                            .setDN(dn)
                            .setName(cn)
                            .setEmail(mail)
                            .setLogin(login)
                            .setPhone(phone)
                            .setUniqueId(uniqueId);

                    user.addMembership(dn);

                    if (ldapObjects != null) {

                        ldapObjects.put(dn, user);
                        NamingEnumeration<?> groups = ((BasicAttribute) attrs.get(this.ldapProperties.get("LDAP_ATTR_MEMBER_OF"))).getAll();
                        while (groups.hasMore()) {

                            String groupDn = groups.next().toString();
                            boolean groupToSave = filterGroup(groupDn);
                            if (groupToSave) {
                                user.addMembership(groupDn);
                                queryLdapGroups(ctx, groupDn, ldapObjects);
                            }

                        }
                    }
                    srch.close();
                    return user;
                }
            } finally {
                ctx.close();
            }
        } catch (Exception e) {
            this.logger.warn("queryLdapUser() caught exception: ", (Throwable) e);
        }
        return null;
    }

    public void setLdapProperties(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

}
