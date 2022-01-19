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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.service.NodeService;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author fmauz
 */
@SessionScoped
public class UserBeanMock extends UserBean {

    
    public void init(){
        
    }
    
    private static final long serialVersionUID = 1L;

    private User currentAccount;

    public void setCurrentAccount(User u) {
        this.currentAccount = u;
    }

    @Override
    public User getCurrentAccount() {
        return currentAccount;
    }

    public void setLdapProperties(LdapProperties properties) {
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
