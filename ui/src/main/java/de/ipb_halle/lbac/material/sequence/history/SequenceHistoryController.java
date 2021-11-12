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

import java.util.Objects;

import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.history.HistoryController;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * 
 * @author flange
 */
public class SequenceHistoryController implements HistoryController<SequenceDifference> {
    private final MaterialBean materialBean;

    public SequenceHistoryController(MaterialBean bean) {
        materialBean = bean;
    }

    @Override
    public void applyPositiveDifference(SequenceDifference difference) {
        SequenceData currentData = materialBean.getSequenceInfos().getSequenceData();
        SequenceData newState = calculateState(difference.getOldSequenceData(), difference.getNewSequenceData(), currentData);
        materialBean.getSequenceInfos().setSequenceData(newState);
    }

    @Override
    public void applyNegativeDifference(SequenceDifference difference) {
        SequenceData currentData = materialBean.getSequenceInfos().getSequenceData();
        SequenceData newState = calculateState(difference.getNewSequenceData(), difference.getOldSequenceData(), currentData);
        materialBean.getSequenceInfos().setSequenceData(newState);
    }

    private SequenceData calculateState(SequenceData oldData, SequenceData newData, SequenceData currentData) {
        SequenceData.Builder builder = SequenceData.builder(currentData);

        if (!Objects.equals(oldData.getSequenceString(), newData.getSequenceString())) {
            builder.sequenceString(newData.getSequenceString());
        }
        if (!Objects.equals(oldData.getSequenceType(), newData.getSequenceType())) {
            builder.sequenceType(newData.getSequenceType());
        }
        if (!Objects.equals(oldData.isCircular(), newData.isCircular())) {
            builder.circular(newData.isCircular());
        }
        if (!Objects.equals(oldData.getAnnotations(), newData.getAnnotations())) {
            builder.annotations(newData.getAnnotations());
        }

        return builder.build();
    }
}