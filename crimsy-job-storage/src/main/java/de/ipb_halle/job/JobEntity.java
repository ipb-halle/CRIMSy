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
package de.ipb_halle.job;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;

import org.hibernate.annotations.Type;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

/**
 * Job entity A job entity object collects information necessary to execute a
 * job outside the DMZ.
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
    @JdbcType(VarbinaryJdbcType.class)
    private byte[] input;

    @Column
    private Date jobdate;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private JobType jobtype;

    /**
     * output data (some jobs do not produce output data)
     */
    @Column
    @JdbcType(VarbinaryJdbcType.class)
    private byte[] output;

    @Column
    private Integer ownerid;

    /**
     * destination queue name
     */
    @Column
    private String queue;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private JobStatus status;

    public NetJob createNetJob() {
        NetJob j = new NetJob()
                .setInput(this.input)
                .setJobDate(this.jobdate)
                .setJobId(this.jobid)
                .setJobType(this.jobtype)
                .setOutput(this.output)
                .setQueue(this.queue)
                .setStatus(this.status);
        return j;
    }

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

    public JobEntity setOwnerId(Integer ownerid) {
        this.ownerid = ownerid;
        return this;
    }

    public JobEntity setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public JobEntity setStatus(JobStatus status) {
        this.status = status;
        return this;
    }
}
