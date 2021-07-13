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
package de.ipb_halle.lbac.admission.mock;

import de.ipb_halle.lbac.admission.LdapHelper;
import de.ipb_halle.lbac.admission.LdapObject;
import de.ipb_halle.lbac.admission.MemberType;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class LdapHelperMock extends LdapHelper {

    public AuthentificationMode authMode = AuthentificationMode.ALLOWED;
    public boolean userExists = true;

    private String ldabUserName;
    private String ldabUserId;
    private String ldabUserEmail;

    private static final long serialVersionUID = 1L;

    public LdapHelperMock() {

    }

    /**
     * Returns the state of the authentificationMode
     *
     * @param l
     * @param p
     * @return
     * @throws Exception
     */
    @Override
    public boolean authenticate(String l, String p) throws Exception {
        if (authMode == AuthentificationMode.ALLOWED) {
            return true;
        }
        if (authMode == AuthentificationMode.FORBIDDEN) {
            return false;
        }
        if (authMode == AuthentificationMode.EXCEPTION) {
            throw new Exception();
        }
        return false;
    }

    @Override
    public LdapObject queryLdapUser(String login, Map<String, LdapObject> ldapObjects) {
        if (userExists) {
            LdapObject ldabObject = new LdapObject();
            ldabObject.setUniqueId(ldabUserId);
            ldabObject.setName(ldabUserName);
            ldabObject.setType(MemberType.USER);
            ldabObject.setEmail(ldabUserEmail);
            return ldabObject;
        } else {
            return null;
        }
    }

    public LdapHelperMock setLdabUserName(String ldabUserName) {
        this.ldabUserName = ldabUserName;
        return this;
    }

    public LdapHelperMock setLdabUserId(String ldabUserId) {
        this.ldabUserId = ldabUserId;
        return this;
    }

    public LdapHelperMock setLdabUserEmail(String ldabUserEmail) {
        this.ldabUserEmail = ldabUserEmail;
        return this;
    }

    public enum AuthentificationMode {
        ALLOWED,
        FORBIDDEN,
        EXCEPTION
    }

}
