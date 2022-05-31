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
package de.ipb_halle.lbac.material.sequence.history;

import java.util.Date;
import java.util.Objects;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.history.HistoryController;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * DTO for the sequence history
 *
 * @author flange
 */
public class SequenceDifference
        implements MaterialDifference, DTO<SequenceHistoryEntity> {

    private static final long serialVersionUID = 1L;

    private static SequenceHistoryDifferenceEntityConverter converter = new SequenceHistoryDifferenceEntityConverter();

    private int materialId;
    private Integer actorId;
    private Date mDate;
    private SequenceData oldSequenceData;
    private SequenceData newSequenceData;

    public SequenceDifference(SequenceData originalSequenceData, SequenceData editedSequenceData) {
        findAndSaveDifferences(originalSequenceData, editedSequenceData);
    }

    public SequenceDifference() {
    }

    static public SequenceDifference fromSequenceHistoryEntity(
            SequenceHistoryEntity entity) {
        return converter.differenceFromEntity(entity);
    }

    @Override
    public void initialise(int materialId, Integer actorID, Date mDate) {
        this.mDate = mDate;
        this.materialId = materialId;
        this.actorId = actorID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HistoryController<SequenceDifference> createHistoryController(
            MaterialBean bean) {
        return new SequenceHistoryController(bean);
    }

    @Override
    public SequenceHistoryEntity createEntity() {
        return converter.entityFromDifference(this);
    }

    private void findAndSaveDifferences(SequenceData original, SequenceData edited) {
        SequenceData.Builder oldBuilder = SequenceData.builder();
        SequenceData.Builder newBuilder = SequenceData.builder();

        if (!Objects.equals(original.getSequenceString(), edited.getSequenceString())) {
            oldBuilder.sequenceString(original.getSequenceString());
            newBuilder.sequenceString(edited.getSequenceString());
        }
        if (!Objects.equals(original.isCircular(), edited.isCircular())) {
            oldBuilder.circular(original.isCircular());
            newBuilder.circular(edited.isCircular());
        }
        if (!Objects.equals(original.getAnnotations(), edited.getAnnotations())) {
            oldBuilder.annotations(original.getAnnotations());
            newBuilder.annotations(edited.getAnnotations());
        }

        oldSequenceData = oldBuilder.build();
        newSequenceData = newBuilder.build();
    }

    /*
     * Getters/Setters
     */
    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    @Override
    public Integer getActorId() {
        return actorId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public SequenceData getOldSequenceData() {
        return oldSequenceData;
    }

    public void setOldSequenceData(SequenceData oldSequenceData) {
        this.oldSequenceData = oldSequenceData;
    }

    public SequenceData getNewSequenceData() {
        return newSequenceData;
    }

    public void setNewSequenceData(SequenceData newSequenceData) {
        this.newSequenceData = newSequenceData;
    }
}
