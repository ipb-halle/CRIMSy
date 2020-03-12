/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "termvectors")
public class TermVectorEntity implements Serializable {

    @EmbeddedId
    private TermVectorId id;

    @Column(name = "termfrequency")
    private int termFrequency;

    public TermVectorEntity() {
    }

    public TermVectorId getId() {
        return id;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public TermVectorEntity setId(TermVectorId id) {
        this.id = id;
        return this;
    }

    public TermVectorEntity setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
        return this;
    }

}
