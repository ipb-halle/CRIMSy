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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Simple key - value store
 */
@Entity
@Table(name = "info")
public class InfoObjectEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * setting the key is only possible at construction time
     */
    @Id
    @Column(name = "key", nullable = false, unique = true, length = -1)
    private String key;

    @Column(name = "value", nullable = true, length = -1)
    private String value;

    /**
     * default constructor this constructor is required by Hibernate / JPA
     */
    public InfoObjectEntity() {
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public InfoObjectEntity setKey(String k) {
        this.key = k;
        return this;
    }

    public InfoObjectEntity setValue(String v) {
        this.value = v;
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InfoObjectEntity that = (InfoObjectEntity) o;

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
