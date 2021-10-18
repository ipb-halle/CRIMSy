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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import de.ipb_halle.molecularfaces.component.openvectoreditor.OpenVectorEditorCore;

/**
 * Controller for the material type sequence
 * 
 * @author flange
 */
public class SequenceInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    private SequenceType sequenceType = null;
    private String sequenceJson = "";
    private SequenceData sequenceData = SequenceData.builder().build();

    private static final List<SequenceType> POSSIBLE_SEQUENCE_TYPES = Arrays.asList(SequenceType.values());
    private final OpenVectorEditorJsonConverter converter = new OpenVectorEditorJsonConverter();

    public void actionSelectSequenceType() {
        if (sequenceType == SequenceType.PROTEIN) {
            sequenceJson = OpenVectorEditorCore.EMPTY_PROTEIN_SEQUENCE_JSON;
        } else {
            sequenceJson = "";
        }
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
        try {
            sequenceJson = converter.sequenceDataToJson(sequenceData);
        } catch (IOException e) {
            // TODO
        }
    }

    public String getSequenceJson() {
        return sequenceJson;
    }

    public void setSequenceJson(String sequenceJson) {
        this.sequenceJson = sequenceJson;
        try {
            sequenceData = converter.jsonToSequenceData(sequenceJson, sequenceType);
        } catch (OpenVectorEditorJsonConverterException | IOException e) {
            // TODO
        }
    }

    public List<SequenceType> getPossibleSequenceTypes() {
        return POSSIBLE_SEQUENCE_TYPES;
    }
}