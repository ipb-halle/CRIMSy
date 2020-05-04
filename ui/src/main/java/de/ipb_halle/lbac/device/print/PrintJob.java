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

    
    public PrintJobEntity createEntity() {
        return new PrintJobEntity()
                .setData(this.data)
                .setDestination(this.destination)
                .setJobId(this.jobid);
    }

    public byte[] getData() {
        return this.data;
    }

    public String getDestination() {
        return this.destination;
    }

    public Integer getJobId() {
        return this.jobid;
    }

    public PrintJob setData(byte[] data) { 
        this.data = data;
        return this;
    }

    public PrintJob setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public PrintJob setJobId(Integer jobid) {
        this.jobid = jobid;
        return this;
    }
}
