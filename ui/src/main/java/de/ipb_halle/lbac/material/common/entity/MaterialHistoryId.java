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
package de.ipb_halle.lbac.material.common.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class MaterialHistoryId implements Serializable {

    private int id;
    private Date mDate;

    public MaterialHistoryId() {
    }

    public MaterialHistoryId(int materialid, Date mDate) {
        this.id = materialid;
        this.mDate = mDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert(mDate != null);
            if (o instanceof MaterialHistoryId) {
                MaterialHistoryId otherId = (MaterialHistoryId) o;
                return (otherId.id == this.id)
                    && mDate.equals(otherId.mDate);
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return this.id + this.mDate.hashCode();
    }

    public void setId(int materialid) {
        this.id = materialid;
    }

    public Date getmDate() {
        return mDate;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

}
