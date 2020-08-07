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

/**
 * MembershipAnnouncement This class announces a user and its memberships to
 * remote nodes. To make things manageable, nesting of memberships is not
 * supported on remote nodes.
 */
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.webservice.WebRequest;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MembershipWebRequest
        extends WebRequest
        implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     */
    @XmlElements(
            @XmlElement(name = "group", type = Group.class))
    private Set<Group> groups;

    @XmlElement
    private User userToAnnounce;

    public MembershipWebRequest() {
        this.groups = new HashSet<>();
    }

    public Set<Group> getGroups() {
        return this.groups;
    }

    public void setGroups(Set<Group> gl) {
        this.groups = gl;
    }

    public User getUserToAnnounce() {
        return userToAnnounce;
    }

    public void setUserToAnnounce(User userToAnnounce) {
        this.userToAnnounce = userToAnnounce;
    }

}
