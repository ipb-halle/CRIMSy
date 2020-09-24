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
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class BioMaterialDifference implements Serializable, MaterialDifference {

    protected Integer actorId;
    protected Date mDate;
    protected int materialId;
    protected Integer taxonomyid_old;
    protected Integer taxonomyid_new;
    protected Integer tissueid_old;
    protected Integer tissueid_new;

    @Override
    public Integer getActorId() {
        return actorId;
    }

    @Override
    public void initialise(int materialId, Integer actorID, Date mDate) {
        this.mDate = mDate;
        this.materialId = materialId;
        this.actorId = actorID;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public boolean differenceFound() {
        return hasTaxonomyDiff() || hasTissueDiff();
    }

    public void addTaxonomyDiff(Integer oldId, Integer newId) {
        taxonomyid_old = oldId;
        taxonomyid_new = newId;
    }

    public void addTissueDiff(Integer oldId, Integer newId) {
        tissueid_new = newId;
        tissueid_old = oldId;
    }

    public Integer getTaxonomyid_old() {
        return taxonomyid_old;
    }

    public Integer getTaxonomyid_new() {
        return taxonomyid_new;
    }

    public Integer getTissueid_old() {
        return tissueid_old;
    }

    public Integer getTissueid_new() {
        return tissueid_new;
    }

    public boolean hasTaxonomyDiff() {
        return !Objects.equals(taxonomyid_new, taxonomyid_old);
    }

    public boolean hasTissueDiff() {
        return !Objects.equals(tissueid_new, tissueid_old);
    }

    

    public int getMaterialId() {
        return materialId;
    }

}
