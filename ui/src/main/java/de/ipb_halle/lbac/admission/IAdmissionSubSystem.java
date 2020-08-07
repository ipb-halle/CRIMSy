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

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;


public interface IAdmissionSubSystem {

    public boolean authenticate(User u, String cred, UserBean bean);
    public AdmissionSubSystemType getSubSystemType();
    public User lookup(String login, UserBean bean);

    public IAdmissionSubSystem setMemberService(MemberService ms);
    public IAdmissionSubSystem setMembershipService(MembershipService ms);
    public IAdmissionSubSystem setNodeService(NodeService ns);

}

