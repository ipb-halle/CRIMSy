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
package de.ipb_halle.lbac.container.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class ContainerNestingId implements Serializable {

    private int sourceid;
    private int targetid;

    public ContainerNestingId() {
    }

    public ContainerNestingId(int sourceid, int targetid) {
        this.sourceid = sourceid;
        this.targetid = targetid;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            if (o instanceof ContainerNestingId) {
                ContainerNestingId otherId = (ContainerNestingId) o;
                return (otherId.sourceid == this.sourceid)
                    && (otherId.targetid == this.targetid);
            }
        }
        return false;
    }

    public int getSourceid() {
        return sourceid;
    }

    @Override
    public int hashCode() {
        return this.sourceid + this.targetid;
    }

    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    public int getTargetid() {
        return targetid;
    }

    public void setTargetid(int targetid) {
        this.targetid = targetid;
    }

}
