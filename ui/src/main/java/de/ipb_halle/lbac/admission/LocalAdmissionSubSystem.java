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
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fbroda
 */
public class LocalAdmissionSubSystem extends AbstractAdmissionSubSystem {

    private final AdmissionSubSystemType subSystemType;
    private Logger logger;

    /**
     * default constructor
     */
    public LocalAdmissionSubSystem() {
        this.subSystemType = AdmissionSubSystemType.LOCAL;
        logger = LogManager.getLogger(LocalAdmissionSubSystem.class);
    }

    /**
     * authenticate a local user and update the modified field on success
     *
     * @param u the user
     * @param cred the credential string
     * @param bean current sessions UserBean
     * @return true if user has been authenticated
     */
    @Override
    public boolean authenticate(User u, String cred, UserBean bean) {
        if (bean.getNodeService().getLocalNodeId().equals(u.getNode().getId())
                && (bean.getCredentialHandler().match(cred, u.getPassword()))) {
            u.updateModified();
            bean.getMemberService().save(u);
            return true;
        }
        return false;
    }

    /**
     * @return the AdmissionSubSystemType implemented by this class
     */
    @Override
    public AdmissionSubSystemType getSubSystemType() {
        return this.subSystemType;
    }

    /**
     * lookup a user in the directory
     *
     * @param login the login name (or email address)
     * @param bean
     * @return Found user if there is exactly one in database
     */
    @Override
    public User lookup(String login, UserBean bean) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("login", login);
        cmap.put("node_id", bean.getNodeService().getLocalNodeId());
        cmap.put("subSystemType", getSubSystemType());
        List<User> lu = bean.getMemberService().loadUsers(cmap);
        if (lu != null && lu.isEmpty()) {
            logger.warn("No user found with login: " + login);
        }
        if (lu != null && lu.size() > 1) {
            logger.error("More than one user found with login: " + login);
        }
        if ((lu != null) && (lu.size() == 1)) {
            return lu.get(0);
        }
        return null;
    }
}
