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

import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.service.InfoObjectService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
//import javax.faces.bean.ApplicationScoped;
//import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//@ManagedBean(name = "ldapProps")
@Named("ldapProps")
@ApplicationScoped
public class LdapProperties implements Serializable {

    private final static long serialVersionUID = 1L;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private transient Logger logger;

    /**
     * LDAP configuration properties
     */
    private List<InfoObject> ldapProperties;
    private Map<String, Integer> ldapPropertyKeys;

    /**
     * contains the value of the LDAP_ENABLE property, which determines whether
     * LDAP is enabled or not
     */
    private boolean ldapEnabled;

    /**
     * the LDAP environment
     */
    private Hashtable<String, String> ldapEnv;

    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    /*
     * default constructor
     */
    public LdapProperties() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.ldapProperties = new ArrayList<>();
        this.ldapPropertyKeys = new HashMap<>();
    }

    public LdapProperties(
            List<InfoObject> infoEntities,
            HashMap<String, Integer> propertyKeys) {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.ldapProperties = infoEntities;
        this.ldapPropertyKeys = propertyKeys;
    }

    private boolean isLdabEnabled(InfoObject info) {
        if (info == null) {
            return false;
        }
        return Boolean.parseBoolean(info.getValue());

    }

    @PostConstruct
    public void LdapBasicsInit() {
        InfoObject ldapEnabledObject = this.infoObjectService.loadByKey("LDAP_ENABLE");
        if (!isLdabEnabled(ldapEnabledObject)) {
            return;
        }
        this.ldapEnabled = Boolean.parseBoolean(this.infoObjectService.loadByKey("LDAP_ENABLE").getValue());

        //*** set default values ***
        initProperty("LDAP_ATTR_COMMON_NAME", "cn");
        initProperty("LDAP_ATTR_EMAIL", "mail");
        initProperty("LDAP_ATTR_PHONE", "telephonenumber");
        initProperty("LDAP_ATTR_LOGIN", "samAccountName");
        initProperty("LDAP_ATTR_MEMBER_OF", "memberOf");
        initProperty("LDAP_ATTR_UNIQUE_ID", "objectId");
        initProperty("LDAP_BASE_DN", "DC=<domain>,DC=de");
        initProperty("LDAP_GROUP_FILTER_DN", "OU=lbacOU,OU=<..>,DC=<domain>,DC=de");

        // currently not used:
        // initProperty("LDAP_DN_LBAC_ROLES", "OU=roles,OU=<name>,OU=<..>,DC=<domain>,DC=de"); 
        // initProperty("LDAP_DN_LBAC_ACCESS_GROUP", "OU=groups,OU=<name>,OU=<..>,DC=<domain>,DC=de");
        // initProperty("LDAP_SEARCH_FILTER_USER", "(&(objectClass=person)(|(cn=@)(samAccountName=@)))");
        initProperty("LDAP_CONTEXT_PROVIDER_URL", "ldap://<server>:<port>");
        initProperty("LDAP_CONTEXT_REFERRAL", "follow");
        initProperty("LDAP_SEARCH_FILTER_LOGIN", "(&(objectClass=person)(samAccountName=@))");
        initProperty("LDAP_CONTEXT_SECURITY_AUTHENTICATION", "simple");
        initProperty("LDAP_CONTEXT_SECURITY_PRINCIPAL", "CN=<user>,CN=users,DC=<domain>,DC=de");
        initProperty("LDAP_CONTEXT_SECURITY_CREDENTIALS", "");

        initialize();
    }

    public String get(String prop) {
        return ldapProperties.get(ldapPropertyKeys.get(prop)).getValue();
    }

    public boolean getLdapEnabled() {
        return this.ldapEnabled;
    }

    public List<InfoObject> getLdapProperties() {
        return ldapProperties;
    }

    public Hashtable<String, String> getLdapEnv() {
        return this.ldapEnv;
    }

    /**
     * Initialize the LDAP context
     */
    private void initialize() {
        ldapEnv = new Hashtable<>();
        if (ldapEnabled) {
            ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnv.put(Context.PROVIDER_URL, get("LDAP_CONTEXT_PROVIDER_URL"));
            ldapEnv.put(Context.REFERRAL, get("LDAP_CONTEXT_REFERRAL"));
            ldapEnv.put(Context.SECURITY_AUTHENTICATION, get("LDAP_CONTEXT_SECURITY_AUTHENTICATION"));
            ldapEnv.put(Context.SECURITY_PRINCIPAL, get("LDAP_CONTEXT_SECURITY_PRINCIPAL"));
            ldapEnv.put(Context.SECURITY_CREDENTIALS, get("LDAP_CONTEXT_SECURITY_CREDENTIALS"));
        }
    }

    /**
     * Initialize a InfoEntity with key, value, owner and ACL
     *
     * @param key the key
     * @param value the value
     * @return the initialized (but detached) InfoEntity
     */
    private void initProperty(String key, String value) {
        InfoObject ie = infoObjectService.loadByKey(key);
        if (ie == null) {
            ie = new InfoObject(key, value);
        }
        this.ldapPropertyKeys.put(key, this.ldapProperties.size());
        this.ldapProperties.add(ie);
    }

    /**
     * save the LDAP settings to the database
     */
    public void save() {
        InfoObject ie = this.infoObjectService.loadByKey("LDAP_ENABLE");
        ie.setValue(Boolean.valueOf(this.ldapEnabled).toString());
        this.infoObjectService.save(ie);

        ListIterator<InfoObject> iter = this.ldapProperties.listIterator();
        while (iter.hasNext()) {
            ie = iter.next();
            infoObjectService.save((InfoObject) ie
                    .setOwner(this.globalAdmissionContext.getAdminAccount())
                    .setACList(this.globalAdmissionContext.getAdminOnlyACL()));
        }
        initialize();
        UIMessage.info(MESSAGE_BUNDLE, "admission_ldap_saved");
    }

    public void setLdapEnabled(boolean b) {
        this.ldapEnabled = b;
    }
}
