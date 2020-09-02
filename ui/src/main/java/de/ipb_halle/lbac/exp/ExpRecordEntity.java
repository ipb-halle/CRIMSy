/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.exp.ExpRecordType;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.johnzon.mapper.JohnzonConverter;

/**
 * @author fbroda
 */
@Entity
@Table(name = "exp_records")
public class ExpRecordEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long exprecordid;


    @Column
    private Date creationtime;
   
    @Column
    private Date changetime;

    @Column
    private Integer experimentid;

    @Column
    private Long next;

    @Column
    private int revision;

    @Column
    private ExpRecordType type;


    public Integer getExperimentId() { 
        return this.experimentid; 
    }

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public Date getChangeTime() {
        return this.changetime;
    }

    public Date getCreationTime() {
        return this.creationtime;
    }
    public Long getNext() {
        return this.next;
    }

    public int getRevision() {
        return this.revision;
    }

    public ExpRecordType getType() {
        return this.type;
    }

    public ExpRecordEntity setChangeTime(Date changetime) {
        this.changetime = changetime;
        return this;
    }

    public ExpRecordEntity setCreationTime(Date creationtime) {
        this.creationtime = creationtime;
        return this;
    }

    public ExpRecordEntity setExperimentId(Integer experimentid) {
        this.experimentid = experimentid;
        return this;
    }

    public ExpRecordEntity setExpRecordId(Long exprecordid) {
        this.exprecordid = exprecordid;
        return this;
    }

    public ExpRecordEntity setNext(Long next) {
        this.next = next;
        return this;
    }

    public ExpRecordEntity setRevision(int revision) {
        this.revision = revision;
        return this;
    }

    public ExpRecordEntity setType(ExpRecordType type) {
        this.type = type;
        return this;
    }
}
