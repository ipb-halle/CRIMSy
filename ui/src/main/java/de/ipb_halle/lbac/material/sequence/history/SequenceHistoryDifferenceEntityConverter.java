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

import de.ipb_halle.lbac.material.common.history.HistoryEntityId;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * 
 * @author flange
 */
public class SequenceHistoryDifferenceEntityConverter {
    public SequenceHistoryEntity entityFromDifference(SequenceDifference diff) {
        SequenceData oldSequenceData = diff.getOldSequenceData();
        SequenceData newSequenceData = diff.getNewSequenceData();

        SequenceHistoryEntity entity = new SequenceHistoryEntity();
        entity.setId(new HistoryEntityId(diff.getMaterialId(), diff.getModificationDate(), diff.getActorId()));
        entity.setAction("EDIT");

        entity.setSequenceString_old(oldSequenceData.getSequenceString());
        entity.setSequenceString_new(newSequenceData.getSequenceString());
        entity.setCircular_old(oldSequenceData.isCircular());
        entity.setCircular_new(newSequenceData.isCircular());
        entity.setAnnotations_old(oldSequenceData.getAnnotations());
        entity.setAnnotations_new(newSequenceData.getAnnotations());

        return entity;
    }

    public SequenceDifference differenceFromEntity(SequenceHistoryEntity entity) {
        HistoryEntityId id = entity.getId();

        SequenceDifference diff = new SequenceDifference();
        diff.setMaterialId(id.getId());
        diff.setActorId(id.getActorid());
        diff.setmDate(id.getMdate());

        diff.setOldSequenceData(oldDataToSequenceData(entity));
        diff.setNewSequenceData(newDataToSequenceData(entity));

        return diff;
    }

    private SequenceData oldDataToSequenceData(SequenceHistoryEntity entity) {
        SequenceData.Builder builder = SequenceData.builder();
        builder.sequenceString(entity.getSequenceString_old());
        builder.annotations(entity.getAnnotations_old());
        builder.circular(entity.isCircular_old());

        return builder.build();
    }

    private SequenceData newDataToSequenceData(SequenceHistoryEntity entity) {
        SequenceData.Builder builder = SequenceData.builder();
        builder.sequenceString(entity.getSequenceString_new());
        builder.annotations(entity.getAnnotations_new());
        builder.circular(entity.isCircular_new());

        return builder.build();
    }
}
