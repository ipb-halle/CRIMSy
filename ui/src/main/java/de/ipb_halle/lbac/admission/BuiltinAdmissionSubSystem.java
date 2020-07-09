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

import de.ipb_halle.lbac.entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltinAdmissionSubSystem extends AbstractAdmissionSubSystem {

    private final AdmissionSubSystemType subSystemType;

    /**
     * default constructor
     */
    public BuiltinAdmissionSubSystem() {
        this.subSystemType = AdmissionSubSystemType.BUILTIN;
    }

    /**
     * This method will always return false as it is not possible to 
     * authenticate a builtin user (public account or owner account)
     * @param u the user 
     * @param cred the credential string
     * @param bean current sessions UserBean
     * @return true if user has been authenticated
     */
    public boolean authenticate(User u, String cred, UserBean bean) {
        return false;
    }

    /**
     * @return the AdmissionSubSystemType implemented by this class
     */
    public AdmissionSubSystemType getSubSystemType() { return this.subSystemType; }

    /**
     * lookup a user in the directory
     * @param login the login name (or email address)
     * @return always null; lookup of remote users is not implemented.
     */
    public User lookup(String login, UserBean bean) {
        Map<String, Object> cmap = new HashMap<String, Object> ();
        cmap.put("login", login);
        cmap.put("node_id", bean.getNodeService().getLocalNodeId());
        cmap.put("subSystemType", getSubSystemType());
        List<User> lu = bean.getMemberService().loadUsers(cmap);
        if ((lu != null) && (lu.size() == 1)) {
            return lu.get(0);
        }
        return null;
    }
}

