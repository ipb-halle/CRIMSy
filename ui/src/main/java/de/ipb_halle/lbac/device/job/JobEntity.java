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
package de.ipb_halle.lbac.device.job;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

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
 * Job entity
 * A job entity object collects information necessary 
 * to execute a job outside the DMZ.
 * <ul>
 * <li>input data to the job</li>
 * <li>output data of the job (not relevant for all types of jobs)</li>
 * <li>name of the destination queue</li>
 * <li>job type</li>
 * <li>job status</li>
 * <li>job date (for expiry)</li>
 * </ul>
 *
 * @author fbroda
 */
@Entity
@Table(name = "jobs")
public class JobEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer jobid;

    /**
     * input data 
     */
    @Column
    private byte[] input;

    @Column
    private Date jobdate;


    @Column
    private JobType jobtype;

    /**
     * output data (some jobs do not produce output data)
     */
    @Column
    private byte[] output;

    @Column
    private Integer ownerid;

    /**
     * destination queue name 
     */
    @Column
    private String queue;

    @Column
    private JobStatus status;




    public byte[] getInput() {
        return this.input;
    }

    public Date getJobDate() {
        return this.jobdate;
    }

    public Integer getJobId() {
        return this.jobid;
    }

    public JobType getJobType() {
        return this.jobtype;
    }

    public byte[] getOutput() {
        return this.output;
    }

    public Integer getOwnerId() {
        return this.ownerid;
    }

    public String getQueue() {
        return this.queue;
    }

    public JobStatus getStatus() {
        return this.status;
    }

    public JobEntity setInput(byte[] input) { 
        this.input = input;
        return this;
    }

    public JobEntity setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return this;
    }

    public JobEntity setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }

    public JobEntity setJobType(JobType jobtype) {
        this.jobtype = jobtype;
        return this;
    }

    public JobEntity setOutput(byte[] output) {
        this.output = output;
        return this;
    }

    JobEntity setOwnerId(Integer ownerid) {
        this.ownerid = ownerid;
        return this;
    }

    public JobEntity setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public  JobEntity setStatus(JobStatus status) {
        this.status = status;
        return this;
    }
}
