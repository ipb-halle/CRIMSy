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
package de.ipb_halle.lbac.items.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class ItemHistoryId implements Serializable {

    private int itemid;
    private UUID actorid;
    private Date mdate;

    public ItemHistoryId() {
    }

    public ItemHistoryId(int itemid, UUID actorid, Date mdate) {
        this.itemid = itemid;
        this.actorid = actorid;
        this.mdate = mdate;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public UUID getActorid() {
        return actorid;
    }

    public void setActorid(UUID actorid) {
        this.actorid = actorid;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

}
