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

import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class TaxonomyDifference implements MaterialDifference, Serializable {

    private Integer actorId;
    private int materialId;
    private Date mDate;

    private List<Integer> newHierarchy = new ArrayList<>();
    private Integer newLevelId;
    private Integer oldLevelId;

    @Override
    public void initialise(int materialId, Integer actorID, Date mDate) {
        this.materialId = materialId;
        this.actorId = actorID;
        this.mDate = mDate;
    }

    @Override
    public Integer getUserId() {
        return actorId;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public Integer getActorId() {
        return actorId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public Date getmDate() {
        return mDate;
    }

    public List<Integer> getNewHierarchy() {
        return newHierarchy;
    }

    public Integer getNewLevelId() {
        return newLevelId;
    }

    public Integer getOldLevelId() {
        return oldLevelId;
    }

    public void setNewHierarchy(List<Integer> newHierarchy) {
        this.newHierarchy = newHierarchy;
    }

    public void setNewLevelId(Integer newLevelId) {
        this.newLevelId = newLevelId;
    }

    public void setOldLevelId(Integer oldLevelId) {
        this.oldLevelId = oldLevelId;
    }

    public boolean isHierarchyChanged() {
        return !newHierarchy.isEmpty();
    }

    public boolean isLevelChanged() {
        if (newLevelId == null || oldLevelId == null) {
            return false;
        } else {
            return !Objects.equals(newLevelId, oldLevelId);
        }
    }

}
