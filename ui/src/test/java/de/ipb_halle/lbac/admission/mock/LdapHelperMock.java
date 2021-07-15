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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class LdapHelperMock extends LdapHelper {

    public AuthentificationMode authMode = AuthentificationMode.ALLOWED;
    List<LdapObject> ldadObjects = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    public LdapHelperMock() {

    }

    public LdapHelperMock addLdapObject(
            String dn,
            String email,
            String login,
            String name,
            String phone,
            MemberType type,
            String uniqueId) {
        LdapObject object = new LdapObject();
        object.setDN(dn);
        object.setEmail(email);
        object.setLogin(login);
        object.setName(name);
        object.setPhone(phone);
        object.setType(type);
        object.setUniqueId(uniqueId);
        ldadObjects.add(object);
        return this;
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
        LdapObject foundObject = null;

        for (LdapObject o : ldadObjects) {
            if (login.equals(o.getLogin())) {
                foundObject = o;
            }
        }
        if (foundObject != null) {
            for (LdapObject o : ldadObjects) {
                foundObject.addMembership(o.getDN());
                ldapObjects.put(o.getDN(), o);
            }
        }
        return foundObject;

    }

    public enum AuthentificationMode {
        ALLOWED,
        FORBIDDEN,
        EXCEPTION
    }

}
