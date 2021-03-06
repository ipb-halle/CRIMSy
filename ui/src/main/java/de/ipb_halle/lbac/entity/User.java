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
package de.ipb_halle.lbac.entity;

import java.io.Serializable;
import java.util.UUID;

import static de.ipb_halle.lbac.admission.GlobalAdmissionContext.PUBLIC_ACCOUNT_ID;

public class User extends Member implements Serializable, Obfuscatable, DTO {

    private final static long serialVersionUID = 1L;
    private final static UUID publicAccountId = UUID.fromString(PUBLIC_ACCOUNT_ID);

    private String email;

    private String phone;

    private String login;

    private String password;

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
    }

    @Override
    public UserEntity createEntity() {
        UserEntity u = new UserEntity();
        setMemberEntity(u);
        u.setEmail(this.email);
        u.setPhone(this.phone);
        u.setLogin(this.login);
        u.setPassword(this.password);
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

    @Override
    public boolean isGroup() {
        return false;
    }

    public boolean isPublicAccount() {
        return getId().equals(User.publicAccountId);
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public void obfuscate() {
        super.obfuscate();
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

    @Override
    public String toString() {
        return "User{id=" + getId() + ", login="
                + this.login + ", name="
                + getName() + ", email="
                + this.email + ",\n      node="
                + this.getNode() + "}";
    }

}
