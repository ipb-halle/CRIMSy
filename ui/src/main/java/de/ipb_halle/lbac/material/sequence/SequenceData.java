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

import java.util.Objects;

/**
 * 
 * @author flange
 */
public class SequenceData {
    private final String sequenceString;
    private final SequenceType sequenceType;
    private final Boolean circular;
//    private final List<SequenceAnnotation> annotations;
    private final String annotations;

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(SequenceData data) {
        Builder builder = new Builder();
        builder.sequenceString = data.getSequenceString();
        builder.sequenceType = data.getSequenceType();
        builder.circular = data.isCircular();
        builder.annotations = data.getAnnotations();

        return builder;
    }

    public static class Builder {
        private String sequenceString = null;
        private SequenceType sequenceType = null;
        private Boolean circular = null;
//        private List<SequenceAnnotation> annotations;
        private String annotations = null;

        private Builder() {
        }

        public Builder sequenceString(String sequenceString) {
            this.sequenceString = sequenceString;
            return this;
        }

        public Builder sequenceType(SequenceType sequenceType) {
            this.sequenceType = sequenceType;
            return this;
        }

        public Builder circular(Boolean circular) {
            this.circular = circular;
            return this;
        }

//        public void annotations(List<SequenceAnnotation> annotations) {
//            this.annotations = annotations;
//        }
        public Builder annotations(String annotations) {
            this.annotations = annotations;
            return this;
        }

        public SequenceData build() {
            return new SequenceData(this);
        }
    }

    private SequenceData(Builder builder) {
        this.sequenceString = builder.sequenceString;
        this.sequenceType = builder.sequenceType;
        this.circular = builder.circular;
        this.annotations = builder.annotations;
    }

    public String getSequenceString() {
        return sequenceString;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public Boolean isCircular() {
        return circular;
    }

//    public List<SequenceAnnotation> getAnnotations() {
//        return annotations;
//    }
    public String getAnnotations() {
        return annotations;
    }

    public Integer getSequenceLength() {
        return sequenceString != null ? sequenceString.length() : null;
    }

    // auto-generated by Eclipse
    @Override
    public int hashCode() {
        return Objects.hash(annotations, circular, sequenceString, sequenceType);
    }

    // auto-generated by Eclipse
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SequenceData))
            return false;
        SequenceData other = (SequenceData) obj;
        return Objects.equals(annotations, other.annotations)
                && Objects.equals(circular, other.circular)
                && Objects.equals(sequenceString, other.sequenceString)
                && sequenceType == other.sequenceType;
    }
}
