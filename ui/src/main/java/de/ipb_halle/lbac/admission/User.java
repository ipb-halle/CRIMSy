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

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.Obfuscatable;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.io.Serializable;
import java.util.Objects;

public class User extends Member implements Serializable, Obfuscatable, DTO, Searchable {

    private final static long serialVersionUID = 1L;

    private String email;

    private String phone;

    private String login;

    private String password;

    private String shortcut;

    /* default constructor */
    public User() {
        super();
    }

    /*
     * DTO constructor
     */
    public User(UserEntity ue, Node node) {
        super((MemberEntity) ue, node);
        this.email = ue.getEmail();
        this.phone = ue.getPhone();
        this.login = ue.getLogin();
        this.password = ue.getPassword();
        this.shortcut = ue.getShortCut();
    }

    @Override
    public UserEntity createEntity() {
        UserEntity u = new UserEntity();
        setMemberEntity(u);
        u.setEmail(this.email);
        u.setPhone(this.phone);
        u.setLogin(this.login);
        u.setPassword(this.password);
        u.setShortCut(shortcut == "" ? null : shortcut);
        return u;
    }

    public String getEmail() {
        return this.email;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public String getPhone() {
        return this.phone;
    }

    /**
     * A valid shortcut must not be empty
     *
     * @return
     */
    public boolean hasShortCut() {
        return getShortcut() != null
                && !getShortcut().trim().isEmpty();
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    public boolean isPublicAccount() {
        return Objects.equals(getId(), GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public void obfuscate() {
        super.obfuscate();
        this.shortcut=null;
        this.password = null;
    }

    public void setEmail(String e) {
        this.email = e;
    }

    public void setLogin(String l) {
        this.login = l;
    }

    public void setPassword(String p) {
        this.password = p;
    }

    public void setPhone(String p) {
        this.phone = p;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    @Override
    public String toString() {
        return "User{id=" + getId() + ", login="
                + this.login + ", name="
                + getName() + ", email="
                + this.email + ",\n      node="
                + this.getNode() + "}";
    }

    @Override
    public String getNameToDisplay() {
        return getName();
    }

    public String getShortcut() {
        return shortcut;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof User)) {
            return false;
        }
        User otherUser = (User) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.USER);
    }
}
