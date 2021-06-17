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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.DTO;
import java.io.Serializable;

/**
 * Simple key - value store
 */
public class Preference implements Serializable, DTO<PreferenceEntity> {

    private final static long serialVersionUID = 1L;

    /**
     * setting the key and user is only possible at construction time
     */
    private Integer id;
    private String key;
    private String value;
    private User user;

    public Preference(PreferenceEntity entity, User user) {
        if (user == null) {
            throw new NullPointerException(
                    "User must not be null in Preference");
        }
        if (entity.getKey() == null) {
            throw new NullPointerException(
                    "Key must not be null in Preference");
        }

        this.id = entity.getId();
        this.key = entity.getKey();
        this.value = entity.getValue();
        this.user = user;
    }

    /**
     * Creates an Preference
     * 
     * @param key
     * @param val
     * @param user
     */
    public Preference(User user, String key, String val) {
        if (user == null) {
            throw new NullPointerException(
                    "User must not be null in Preference");
        }
        if (key == null) {
            throw new NullPointerException(
                    "Key must not be null in Preference");
        }

        this.key = key;
        this.value = val;
        this.user = user;
    }

    @Override
    public PreferenceEntity createEntity() {
        PreferenceEntity entity = new PreferenceEntity();
        entity.setId(this.id);
        entity.setUserId(this.user.getId());
        return entity.setKey(key).setValue(value);
    }

    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public User getUser() {
        return user;
    }

    public void setValue(String v) {
        this.value = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Preference that = (Preference) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Preference{" + "id='" + id + '\'' + ", key='" + key + '\''
                + ", value='" + value + '\'' + ", user='" + user.getName()
                + '\'' + '}';
    }

}
