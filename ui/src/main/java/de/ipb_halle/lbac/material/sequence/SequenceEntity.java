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

import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.FieldOrder;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Database entity for the material {@link Sequence}.
 *
 * @author flange
 */
@Entity
@Table(name = "sequences")
public class SequenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @FieldOrder(order = 1)
    private Integer id;

    @AttributeTag(type = AttributeType.SEQUENCE_STRING)
    @Column
    @FieldOrder(order = 2)
    private String sequenceString;

    @AttributeTag(type = AttributeType.SEQUENCE_TYPE)
    @Column
    private String sequenceType;

    @Column
    private Boolean circular;

    @Column
    private String annotations;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSequenceString() {
        return sequenceString;
    }

    public void setSequenceString(String sequenceString) {
        this.sequenceString = sequenceString;
    }

    public String getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(String sequenceType) {
        this.sequenceType = sequenceType;
    }

    public Boolean isCircular() {
        return circular;
    }

    public void setCircular(Boolean circular) {
        this.circular = circular;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }
}
