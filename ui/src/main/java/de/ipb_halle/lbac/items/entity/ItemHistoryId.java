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
package de.ipb_halle.lbac.items.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class ItemHistoryId implements Serializable {

    private int itemid;
    private Integer actorid;
    private Date mdate;

    public ItemHistoryId() {
    }

    public ItemHistoryId(int itemid, Integer actorid, Date mdate) {
        this.itemid = itemid;
        this.actorid = actorid;
        this.mdate = mdate;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert(mdate != null);
            assert(actorid != null);
            if (o instanceof ItemHistoryId) {
                 ItemHistoryId otherId = (ItemHistoryId) o;
                return (otherId.itemid == this.itemid)
                    && actorid.equals(otherId.actorid)
                    && mdate.equals(otherId.mdate);
            }
        }
        return false;
    }

    public int getItemid() {
        return itemid;
    }

    @Override
    public int hashCode() {
        return this.itemid + this.actorid.intValue() + this.mdate.hashCode();
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

}
