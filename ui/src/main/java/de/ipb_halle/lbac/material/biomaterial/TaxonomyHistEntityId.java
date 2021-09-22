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
package de.ipb_halle.lbac.material.biomaterial;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class TaxonomyHistEntityId implements Serializable {

    private Integer id;
    private Date mdate;
    private Integer actorid;

    public TaxonomyHistEntityId() {
    }

    public TaxonomyHistEntityId(Integer id, Date mtime, Integer actorid) {
        this.id = id;
        this.mdate = mtime;
        this.actorid = actorid;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert(mdate != null);
            assert(id != null);
            assert(actorid != null);

            if (o instanceof TaxonomyHistEntityId) {
                TaxonomyHistEntityId otherId = (TaxonomyHistEntityId) o;
                return id.equals(otherId.id)
                    && mdate.equals(otherId.mdate)
                    && actorid.equals(otherId.actorid);
            }
        }
        return false;
    }

    public Date getMdate() {
        return mdate;
    }

    public int hashCode() {
        return id.intValue() + mdate.hashCode() + actorid.intValue();
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer taxonomyId) {
        this.id = taxonomyId;
    }

}
