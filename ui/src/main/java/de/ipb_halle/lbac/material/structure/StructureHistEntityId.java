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
package de.ipb_halle.lbac.material.structure;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class StructureHistEntityId implements Serializable {

    private int id;
    private Date mdate;

    public StructureHistEntityId() {
    }

    public StructureHistEntityId(int id, Date mdate) {
        this.id = id;
        this.mdate = mdate;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            assert(mdate != null);
            if (o instanceof StructureHistEntityId) {
                StructureHistEntityId otherId = (StructureHistEntityId) o;
                return (otherId.id == this.id)
                    && mdate.equals(otherId.mdate);
            }
        }
        return false;
    }


    public int getId() {
        return id;
    }

    public int hashCode() {
        return this.id + this.mdate.hashCode();
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

}
