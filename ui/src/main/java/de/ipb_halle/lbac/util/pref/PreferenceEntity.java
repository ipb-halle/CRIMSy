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
package de.ipb_halle.lbac.util.pref;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Represents a user preference
 *
 * @author fbroda
 */
@Entity
@Table(name = "preferences")
public class PreferenceEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    @NotNull
    private Integer user_id;

    @Column
    @NotNull
    private String key; 

    @Column
    private String value;


    public PreferenceEntity() {

    }

    public Integer getId() {
        return this.id;
    }

    public Integer getUserId() {
        return this.user_id;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    /**
     *
     * @param id
     * @return
     */
    public PreferenceEntity setId(Integer i) {
        this.id = id;
        return this;
    }

    /**
     *
     * @param u user id
     * @return
     */
    public PreferenceEntity setUserId(Integer u) {
        this.user_id = u;
        return this;
    }

    /**
     *
     * @param key 
     * @return
     */
    public PreferenceEntity setKey(String key) {
        this.key = key; 
        return this;
    }

    /**
     *
     * @param v value
     * @return
     */
    public PreferenceEntity setValue(String v) {
        this.value = v;
        return this;
    }
}
