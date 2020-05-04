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
package de.ipb_halle.lbac.device.print;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;

import java.io.Serializable;
import java.util.Date;

/**
 * Print Job 
 *
 * @author fbroda
 */
public class PrintJob implements DTO {


    private Integer jobid;
    private byte[] data;
    private String destination;
    private Date   jobdate;
    private User   owner;
    private PrintJobStatus status;

    public PrintJob() {
        this.jobdate = new Date();
        this.status = PrintJobStatus.PENDING;
    }

    public PrintJob(PrintJobEntity entity, User owner) {
        this.owner = owner;
        this.jobid = entity.getJobId();
        this.data = entity.getData();
        this.destination = entity.getDestination();
        this.jobdate = entity.getJobDate();
        this.status = entity.getStatus();
    }
    
    public PrintJobEntity createEntity() {
        return new PrintJobEntity()
                .setData(this.data)
                .setDestination(this.destination)
                .setJobDate(this.jobdate)
                .setJobId(this.jobid)
                .setOwnerId(this.owner.getId())
                .setStatus(this.status);
    }

    public byte[] getData() {
        return this.data;
    }

    public String getDestination() {
        return this.destination;
    }

    public Date getJobDate() {
        return this.jobdate;
    }

    public Integer getJobId() {
        return this.jobid;
    }

    public User getOwner() {
        return this.owner;
    }

    public PrintJobStatus getStatus() { 
        return this.status; 
    }

    public PrintJob setData(byte[] data) { 
        this.data = data;
        return this;
    }

    public PrintJob setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public PrintJob setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return this;
    }

    public PrintJob setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }

    public PrintJob setStatus(PrintJobStatus status) {
        this.status = status;
        return this;
    }

    public PrintJob setOwner(User owner) {
        this.owner = owner;
        return this;
    }
}
