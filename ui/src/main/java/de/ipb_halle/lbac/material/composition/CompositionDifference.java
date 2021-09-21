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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class CompositionDifference implements Serializable, MaterialDifference, DTO {

    private static final long serialVersionUID = 1L;
    private int actorId;
    private Date mDate;
    private int materialId;
    private final List<Integer> materialIds_old = new ArrayList<>();
    private final List<Integer> materialIds_new = new ArrayList<>();
    private final List<Double> concentrations_old = new ArrayList<>();
    private final List<Double> concentrations_new = new ArrayList<>();
    private String action;

    public CompositionDifference(String action) {
        this.action = action;
    }

    @Override
    public Integer getActorId() {
        return actorId;
    }

    @Override
    public void initialise(int materialId, Integer actorID, Date mDate) {
        this.mDate = mDate;
        this.actorId = actorID;
        this.materialId = materialId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public void addDifference(
            Integer materialIdOld,
            Integer materialIdNew,
            Double comncentrationOld,
            Double concentrationNew) {
        this.materialIds_new.add(materialIdNew);
        this.materialIds_old.add(materialIdOld);
        this.concentrations_new.add(concentrationNew);
        this.concentrations_old.add(comncentrationOld);

    }

    @Override
    public List<CompositionHistoryEntity> createEntity() {
        List<CompositionHistoryEntity> entities = new ArrayList<>();
        for (int i = 0; i < materialIds_old.size(); i++) {
            CompositionHistoryEntity entity = new CompositionHistoryEntity();
            entity.setId(new CompositionHistEntityId(materialId, mDate, actorId));
            entity.setAction(action);
            entity.setMaterialid_new(materialIds_new.get(i));
            entity.setMaterialid_old(materialIds_old.get(i));
            entity.setConcentration_new(concentrations_new.get(i));
            entity.setConcentration_old(concentrations_old.get(i));
            entities.add(entity);
        }

        return entities;
    }

    public boolean hasDifferences() {
        return materialIds_new.size() > 0;
    }

}