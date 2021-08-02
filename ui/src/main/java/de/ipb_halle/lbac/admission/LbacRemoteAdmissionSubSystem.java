/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

public class LbacRemoteAdmissionSubSystem extends AbstractAdmissionSubSystem {

    private static final long serialVersionUID = 1L;

    private final AdmissionSubSystemType subSystemType;

    /**
     * default constructor
     */
    public LbacRemoteAdmissionSubSystem() {
        this.subSystemType = AdmissionSubSystemType.LBAC_REMOTE;
    }

    /**
     * authenticate a user
     *
     * @param u the user
     * @param cred the credential string
     * @param bean the current sessions UserBean
     * @return Always false; authenticating remote users is currently not
     * implemented.
     */
    @Override
    public boolean authenticate(User u, String cred, UserBean bean) {
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
     * @param bean the current sessions UserBean
     * @return always null; lookup of remote users is not implemented.
     */
    @Override
    public User lookup(String login, UserBean bean) {
        return null;
    }
}
