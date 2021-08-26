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

import com.fasterxml.jackson.core.JsonProcessingException;

import de.ipb_halle.molecularfaces.component.openvectoreditor.OpenVectorEditorCore;

/**
 * Controller for the material type sequence
 * 
 * @author flange
 */
public class SequenceInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sequenceJson;
    private SequenceData data;
    private List<SequenceType> possibleSequenceTypes = Arrays
            .asList(SequenceType.values());

    public SequenceInformation() {
        data = new SequenceData();
    }

    public SequenceInformation(SequenceData sequenceData) {
        this.data = sequenceData;

        try {
            sequenceJson = new OpenVectorEditorJsonConverter().sequenceDataToJson(data);
        } catch (JsonProcessingException e) {
            // TODO
        }
    }

    public boolean isSequenceTypeSelected() {
        return data.getSequenceType() != null;
    }

    public void actionSelectSequenceType() {
        if (data.getSequenceType() == SequenceType.PROTEIN) {
            sequenceJson = OpenVectorEditorCore.EMPTY_PROTEIN_SEQUENCE_JSON;
        } else {
            sequenceJson = "";
        }
    }

    public SequenceData getData() {
        return data;
    }

    public String getSequenceJson() {
        return sequenceJson;
    }

    public void setSequenceJson(String sequenceJson) {
        this.sequenceJson = sequenceJson;
    }

    public List<SequenceType> getPossibleSequenceTypes() {
        return possibleSequenceTypes;
    }
}