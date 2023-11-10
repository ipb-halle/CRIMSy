/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.crimsy_api.DTO;
import java.util.Date;

/**
 * Job 
 *
 * @author fbroda
 */
public abstract class Job<T extends Job> implements DTO {

    private Integer jobid;
    private byte[]  input;
    private Date    jobdate;
    private JobType jobtype;
    private byte[]  output;
    private String  queue;
    private JobStatus status;
    private Integer ownerId;

    protected Job() {
        this.jobdate = new Date();
        this.status = JobStatus.PENDING;
    }

    protected Job(JobEntity entity) {
        this.jobid = entity.getJobId();
        this.input = entity.getInput();
        this.jobdate = entity.getJobDate();
        this.output = entity.getOutput();
        this.queue = entity.getQueue();
        this.status = entity.getStatus();
        this.jobtype = entity.getJobType();
        this.ownerId = entity.getOwnerId();
    }
    
    public JobEntity createEntity() {
        JobEntity e = new JobEntity()
                .setInput(this.input)
                .setJobDate(this.jobdate)
                .setJobId(this.jobid)
                .setJobType(this.jobtype)
                .setOwnerId(this.ownerId)
                .setOutput(this.output)
                .setQueue(this.queue)
                .setStatus(this.status);
        return e;
    }

    public NetJob createNetJob() {
        NetJob nj = new NetJob()
            .setInput(this.input)
            .setJobDate(this.jobdate)
            .setJobId(this.jobid)
            .setJobType(this.jobtype)
            .setOutput(this.output)
            .setQueue(this.queue)
            .setStatus(this.status);
        if (this.ownerId != null) {
            nj.setOwnerName("ownerId=" + ownerId.toString());
        }
        return nj;
    }

    public byte[] getInput() {
        return this.input;
    }

    public Integer getJobId() {
        return this.jobid;
    }

    public Date getJobDate() {
        return jobdate;
    }

    public JobType getJobType() {
        return this.jobtype;
    }

    public byte[] getOutput() {
        return this.output;
    }

    public Integer getOwnerId() {
        return this.ownerId;
    }

    public String getQueue() {
        return this.queue;
    }

    public JobStatus getStatus() { 
        return this.status; 
    }

    public T setInput(byte[] input) { 
        this.input = input;
        return (T) this;
    }

    public T setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return (T) this;
    }

    public T setJobId(Integer jobid) {
        this.jobid = jobid;
        return (T) this;
    }

    public T setJobType(JobType jobtype) {
        this.jobtype = jobtype;
        return (T) this;
    }

    public T setOutput(byte[] output) { 
        this.output = output;
        return (T) this;
    }

    public T setOwnerId(Integer o) {
        this.ownerId = o;
        return (T) this;
    }

    public T setQueue(String queue) {
        this.queue = queue;
        return (T) this;
    }

    public T setStatus(JobStatus status) {
        this.status = status;
        return (T) this;
    }

    /**
     * update this job from a <code>NetJob</code> object. Update 
     * affects only the fields <code>status</code> and 
     * <code>output</code>. All other fields remain unaffected.
     * @param netjob the <code>NetJob</code> object to use for update
     */
    public void update(NetJob netjob) {
        this.output = netjob.getOutput();
        this.status = netjob.getStatus();
    }
}
