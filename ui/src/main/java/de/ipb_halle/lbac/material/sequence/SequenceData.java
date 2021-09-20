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

/**
 * 
 * @author flange
 */
public class SequenceData {
    private String sequenceString;
    private SequenceType sequenceType = null;
    private boolean circular = false;
//    private List<SequenceAnnotation> annotations;
    private String annotations = "";

    public String getSequenceString() {
        return sequenceString;
    }

    public void setSequenceString(String sequenceString) {
        this.sequenceString = sequenceString;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }

    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

//    public List<SequenceAnnotation> getAnnotations() {
//        return annotations;
//    }
    public String getAnnotations() {
        return annotations;
    }

//    public void setAnnotations(List<SequenceAnnotation> annotations) {
//        this.annotations = annotations;
//    }
    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public Integer getSequenceLength() {
        return sequenceString != null ? sequenceString.length() : null;
    }
}