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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.i18n.UIClient;
import de.ipb_halle.lbac.i18n.UIMessage;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import java.io.Serializable;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@ManagedBean(name = "ldapTestBean")
@RequestScoped
public class LdapTestBean implements Serializable {

    private final static long serialVersionUID = 1L;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private String              ldapLogin;
    private String              ldapPassword;

    @Inject
    private LdapProperties      ldapProperties;

    private transient Logger    logger;

    /**
     * default constructor
     */
    public LdapTestBean() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * authenticate a user against LDAP using the current LDAP settings
     * @return true on successful authentication
     * @throws
     */
    private boolean authenticate() throws Exception {
        if(! ldapProperties.getLdapEnabled()) {
            this.logger.warn("authenticate(): LDAP not enabled.");
            return false;
        }

        // authenticate
        LdapHelper helper = new LdapHelper(ldapProperties);
        if(! helper.authenticate(this.ldapLogin, this.ldapPassword)) {
            // login failure
            return false;
        }
        return true;
    }

    /**
     * Perform the LDAP authentication without any user or group
     * lookup and set a FacesMessage
     */
    public void checkLdapConnect() {
        try {

            if ( authenticate() ) {
                UIMessage.info(new UIClient("checkLdapForm:testresult"), MESSAGE_BUNDLE, "admission_ldap_success");
            } else {
                UIMessage.warn(new UIClient("checkLdapForm:testresult"), MESSAGE_BUNDLE, "admission_ldap_failure");
            }
        } catch (Exception e) {
            UIMessage.error(new UIClient("checkLdapForm:testresult"), MESSAGE_BUNDLE, "admission_ldap_error");
        }
    }

    public String getLdapLogin() { return ldapLogin; }
    public String getLdapPassword() { return ldapPassword; }

    public void setLdapLogin(String ldapLogin) { this.ldapLogin = ldapLogin; }
    public void setLdapPassword(String ldapPassword) { this.ldapPassword = ldapPassword; }

}
