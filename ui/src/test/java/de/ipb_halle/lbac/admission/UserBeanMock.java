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

import de.ipb_halle.lbac.service.NodeService;

/**
 *
 * @author fmauz
 */
public class UserBeanMock extends UserBean {

    private User currentAccount;

    public void setCurrentAccount(User u) {
        this.currentAccount = u;
    }

    @Override
    public User getCurrentAccount() {
        return currentAccount;
    }

    public void setLdabProperties(LdapProperties properties) {
        this.ldapProperties = properties;
    }

    public void setNodeService(NodeService service) {
        this.nodeService = service;
    }

    public void setMemberService(MemberService service) {
        this.memberService = service;
    }

    public void setMemberShipService(MembershipService service) {
        this.membershipService = service;
    }

    public void setGlobalAdmissionContext(GlobalAdmissionContext context) {
        this.globalAdmissionContext = context;
    }

}
