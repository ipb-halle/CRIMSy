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
package de.ipb_halle.lbac.material.sequence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the material type sequence
 * 
 * @author flange
 */
public class SequenceInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    private SequenceType sequenceType = null;
    private SequenceData sequenceData = SequenceData.builder().build();

    private static final List<SequenceType> POSSIBLE_SEQUENCE_TYPES = Arrays.asList(SequenceType.values());

    /*
     * Actions
     */
    public void actionSelectSequenceType() {
        sequenceData = SequenceData.builder().sequenceType(sequenceType).build();
    }

    /*
     * Getters/Setters
     */
    public boolean isSequenceTypeSelected() {
        return sequenceType != null;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }

    public SequenceData getSequenceData() {
        return sequenceData;
    }

    public void setSequenceData(SequenceData sequenceData) {
        this.sequenceData = sequenceData;
        this.sequenceType = sequenceData.getSequenceType();
    }

    public List<SequenceType> getPossibleSequenceTypes() {
        return POSSIBLE_SEQUENCE_TYPES;
    }
}
