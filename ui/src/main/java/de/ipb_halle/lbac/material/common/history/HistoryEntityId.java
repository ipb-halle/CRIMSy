/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common.history;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 * An embeddable entity that can act as composite primary key in history
 * entities. It consists of the columns:
 * <ul>
 * <li>id
 * <li>mdate
 * <li>actorid
 * </ul>
 * 
 * @author flange
 */
@Embeddable
public class HistoryEntityId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date mdate;
    private Integer actorid;

    public HistoryEntityId() {
    }

    public HistoryEntityId(Integer id, Date mdate, Integer actorid) {
        this.id = id;
        this.mdate = mdate;
        this.actorid = actorid;
    }

    // auto-generated by Eclipse
    @Override
    public int hashCode() {
        return Objects.hash(actorid, id, mdate);
    }

    // auto-generated by Eclipse
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof HistoryEntityId))
            return false;
        HistoryEntityId other = (HistoryEntityId) obj;
        return Objects.equals(actorid, other.actorid)
                && Objects.equals(id, other.id)
                && Objects.equals(mdate, other.mdate);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }
}
