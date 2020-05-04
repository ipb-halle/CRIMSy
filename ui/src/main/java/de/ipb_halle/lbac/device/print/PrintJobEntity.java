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
 * Print Job entity
 * A print job entity object collects information necessary 
 * to execute a print job outside the DMZ.
 * <ul>
 * <li>actual bytes to be printed</li>
 * <li>name of the destination printer</li>
 * </ul>
 *
 * @author fbroda
 */
@Entity
@Table(name = "printjobs")
public class PrintJobEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer jobid;

    /**
     * The document 
     */
    @Column
    private byte[] data;

    /**
     * the name of the destination printer
     */
    @Column
    private String destination;

    @Column
    private UUID ownerid;

    @Column
    private Date jobdate;

    @Column
    private PrintJobStatus status;

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

    public UUID getOwnerId() {
        return this.ownerid;
    }

    public PrintJobStatus getStatus() {
        return this.status;
    }

    public PrintJobEntity setData(byte[] data) { 
        this.data = data;
        return this;
    }

    public PrintJobEntity setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public PrintJobEntity setJobDate(Date jobdate) {
        this.jobdate = jobdate;
        return this;
    }

    public PrintJobEntity setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }

    PrintJobEntity setOwnerId(UUID ownerid) {
        this.ownerid = ownerid;
        return this;
    }

    public  PrintJobEntity setStatus(PrintJobStatus status) {
        this.status = status;
        return this;
    }
}
