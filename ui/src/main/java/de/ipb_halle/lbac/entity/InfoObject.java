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
package de.ipb_halle.lbac.entity;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.User;
import java.io.Serializable;

/**
 * Simple key - value store
 */
public class InfoObject extends ACObject implements Serializable, DTO<InfoObjectEntity> {

    private final static long serialVersionUID = 1L;

    /**
     * setting the key is only possible at construction time
     */
    private String key;
    private String value;

    /**
     * default constructor this constructor is required by Hibernate / JPA
     */
    public InfoObject() {
    }

    public InfoObject(InfoObjectEntity entity, ACList acl, User u) {
        this.key = entity.getKey();
        this.value = entity.getValue();
        setACList(acl);
        setOwner(u);
    }

    public InfoObject(String k) {
        this.key = k;
    }

    /**
     * Creates an InfoEntity
     *
     * @param key
     * @param val
     */
    public InfoObject(String key, String val) {

        this.key = key;
        this.value = val;
    }

    @Override
    public InfoObjectEntity createEntity() {
        InfoObjectEntity entity = new InfoObjectEntity();
        entity.setOwner(getOwner().getId());
        entity.setACList(getACList().getId());
        return entity.setKey(key).setValue(value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String k) {
        this.key = k;
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

        InfoObject that = (InfoObject) o;

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
        return "InfoEntity{"
                + "key='" + key + '\''
                + ", value='" + value + '\''
                + '}';
    }

}
