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

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;

import java.io.Serializable;
import java.util.Date;

/**
 * Job 
 *
 * @author fbroda
 */
public class Job implements DTO {


    private Integer jobid;
    private byte[]  input;
    private Date    jobdate;
    private JobType jobtype;
    private byte[]  output;
    private User    owner;
    private String  queue;
    private JobStatus status;

    public Job() {
        this.jobdate = new Date();
        this.status = JobStatus.PENDING;
    }

    public Job(JobEntity entity, User owner) {
        this.jobid = entity.getJobId();
        this.input = entity.getInput();
        this.jobdate = entity.getJobDate();
        this.jobtype = entity.getJobType();
        this.output = entity.getOutput();
        this.owner = owner;
        this.queue = entity.getQueue();
        this.status = entity.getStatus();
    }
    
    public JobEntity createEntity() {
        return new JobEntity()
                .setInput(this.input)
                .setJobDate(this.jobdate)
                .setJobId(this.jobid)
                .setJobType(this.jobtype)
                .setOutput(this.output)
                .setOwnerId(this.owner.getId())
                .setQueue(this.queue)
                .setStatus(this.status);
    }

    public byte[] getInput() {
        return this.input;
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

    public User getOwner() {
        return this.owner;
    }

    public String getQueue() {
        return this.queue;
    }

    public JobStatus getStatus() { 
        return this.status; 
    }

    public Job setInput(byte[] input) { 
        this.input = input;
        return this;
    }

    public Job setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return this;
    }

    public Job setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }

    public Job setJobType(JobType jobtype) {
        this.jobtype = jobtype;
        return this;
    }

    public Job setOutput(byte[] output) { 
        this.output = output;
        return this;
    }

    public Job setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    public Job setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public Job setStatus(JobStatus status) {
        this.status = status;
        return this;
    }
}
