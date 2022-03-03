/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.pageobjects.pages.settings.usermanagement;

/**
 * Model class for input data in {@link UserDialog}.
 * 
 * @author flange
 */
public class UserModel {
    private String name;
    private String login;
    private String shortcut;
    private String email;
    private String password;
    private String passwordRepeat;
    private String phone;

    /*
     * Fluent setters
     */
    public UserModel name(String name) {
        this.name = name;
        return this;
    }

    public UserModel login(String login) {
        this.login = login;
        return this;
    }

    public UserModel shortcut(String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public UserModel email(String email) {
        this.email = email;
        return this;
    }

    public UserModel password(String password) {
        this.password = password;
        return this;
    }

    public UserModel passwordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
        return this;
    }

    public UserModel phone(String phone) {
        this.phone = phone;
        return this;
    }

    /*
     * Getters
     */
    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public String getPhone() {
        return phone;
    }
}