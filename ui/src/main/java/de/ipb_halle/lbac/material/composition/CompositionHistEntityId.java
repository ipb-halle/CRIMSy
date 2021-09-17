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
package de.ipb_halle.lbac.material.composition;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class CompositionHistEntityId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Date mtime;
    private Integer actorid;

    public CompositionHistEntityId() {
    }

    public CompositionHistEntityId(Integer id, Date mtime, Integer actorid) {
        this.id = id;
        this.mtime = mtime;
        this.actorid = actorid;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert (mtime != null);
            assert (id != null);
            assert (actorid != null);

            if (o instanceof CompositionHistEntityId) {
                CompositionHistEntityId otherId = (CompositionHistEntityId) o;
                return id.equals(otherId.id)
                        && mtime.equals(otherId.mtime)
                        && actorid.equals(otherId.actorid);
            }
        }
        return false;
    }

    public Date getMtime() {
        return mtime;
    }

    @Override
    public int hashCode() {
        return id + mtime.hashCode() + actorid;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
