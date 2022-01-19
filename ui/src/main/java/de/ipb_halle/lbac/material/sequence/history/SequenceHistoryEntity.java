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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import de.ipb_halle.lbac.material.common.history.HistoryEntityId;

/**
 * 
 * @author flange
 */
@Entity
@Table(name = "sequences_history")
public class SequenceHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // includes columns id, actorid and mtime
    @EmbeddedId
    private HistoryEntityId id;

    @Column
    private String digest;
    @Column
    private String action;
    @Column
    private String sequenceString_old;
    @Column
    private String sequenceString_new;
    @Column
    private Boolean circular_old;
    @Column
    private Boolean circular_new;
    @Column
    private String annotations_old;
    @Column
    private String annotations_new;

    /*
     * Getters/Setters
     */
    public HistoryEntityId getId() {
        return id;
    }

    public void setId(HistoryEntityId id) {
        this.id = id;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSequenceString_old() {
        return sequenceString_old;
    }

    public void setSequenceString_old(String sequenceString_old) {
        this.sequenceString_old = sequenceString_old;
    }

    public String getSequenceString_new() {
        return sequenceString_new;
    }

    public void setSequenceString_new(String sequenceString_new) {
        this.sequenceString_new = sequenceString_new;
    }

    public Boolean isCircular_old() {
        return circular_old;
    }

    public void setCircular_old(Boolean circular_old) {
        this.circular_old = circular_old;
    }

    public Boolean isCircular_new() {
        return circular_new;
    }

    public void setCircular_new(Boolean circular_new) {
        this.circular_new = circular_new;
    }

    public String getAnnotations_old() {
        return annotations_old;
    }

    public void setAnnotations_old(String annotations_old) {
        this.annotations_old = annotations_old;
    }

    public String getAnnotations_new() {
        return annotations_new;
    }

    public void setAnnotations_new(String annotations_new) {
        this.annotations_new = annotations_new;
    }
}