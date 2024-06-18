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

import java.util.List;
import java.util.Date;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The NetJob class is for transmission of queries, job 
 * lists and single job objects from and to the "agency" 
 * over the local network.
 *
 * @author fbroda
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NetJob {

    private Integer         jobid;
    private byte[]          input;
    private Date            jobdate;
    private JobType         jobtype;
    private byte[]          output;
    private String          ownername;
    private String          queue;
    private RequestType     requesttype;
    private JobStatus       status;
    private String          token;

    @XmlElementWrapper(name="joblist")
    @XmlElements( 
    @XmlElement(name = "job", type = NetJob.class) )
    private List<NetJob>    joblist;


    public byte[] getInput() {
        return this.input;
    }

    public Date getJobDate() {
        return this.jobdate;
    }

    public Integer getJobId() {
        return this.jobid;
    }

    public List<NetJob> getJobList() {
        return this.joblist;
    }

    public JobType getJobType() {
        return this.jobtype;
    }

    public byte[] getOutput() {
        return this.output;
    }

    public String getOwnerName() {
        return this.ownername;
    }

    public String getQueue() {
        return this.queue;
    }

    public RequestType getRequestType() {
        return this.requesttype;
    }

    public JobStatus getStatus() { 
        return this.status; 
    }

    public String getToken() {
        return this.token;
    }

    public NetJob setInput(byte[] input) { 
        this.input = input;
        return this;
    }

    public NetJob setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return this;
    }

    public NetJob setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }

    public NetJob setJobList(List<NetJob> joblist) {
        this.joblist = joblist;
        return this;
    }

    public NetJob setJobType(JobType jobtype) {
        this.jobtype = jobtype;
        return this;
    }

    public NetJob setOutput(byte[] output) { 
        this.output = output;
        return this;
    }

    public NetJob setOwnerName(String ownername) {
        this.ownername = ownername;
        return this;
    }

    public NetJob setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public NetJob setRequestType(RequestType requesttype) {
        this.requesttype = requesttype;
        return this;
    }

    public NetJob setStatus(JobStatus status) {
        this.status = status;
        return this;
    }

    public NetJob setToken(String token) {
        this.token = token;
        return this;
    }
}
