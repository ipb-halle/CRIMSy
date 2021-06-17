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
import java.io.Serializable;


public abstract class AbstractAdmissionSubSystem implements IAdmissionSubSystem,Serializable {

    private MemberService       memberService;
    private MembershipService   membershipService;
    private NodeService         nodeService;

    public IAdmissionSubSystem setMemberService(MemberService ms) {
        this.memberService = ms;
        return this;
    }

    public IAdmissionSubSystem setMembershipService(MembershipService ms) {
        this.membershipService = ms;
        return this;
    }

    public IAdmissionSubSystem setNodeService(NodeService ns) {
        this.nodeService = ns;
        return this;
    }

}

