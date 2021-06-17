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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javax.validation.constraints.NotNull;

import org.apache.johnzon.mapper.JohnzonConverter;

/**
 * This entity describes a printer:
 * <ul>
 * <li>name</li>
 * <li>config</li>
 * <li>contact</li>
 * <li>model</li>
 * <li>place</li>
 * <li>driver class</li>
 * <li>status (READY, FAILED, DISABLED, ???)</li>
 * <li>ACL / project</li>
 * </ul>
 *
 * @author fbroda
 */
@Entity
@Table(name = "printers")
public class PrinterEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private String queue;

    @Column
    private Integer aclistid;

    @Column
    @NotNull
    private String config;

    @Column
    @NotNull
    private String contact;

    @Column
    @NotNull
    private String driver;

    @Column
    @NotNull
    private String model;

    @Column
    @NotNull
    private String name;

    @Column
    private Integer ownerid;

    @Column
    @NotNull
    private String place;

    @Column
    @NotNull
    private PrinterStatus status;

    /*
     * default constructor
     */
    public PrinterEntity() {
        this.config = "";
        this.contact = "";
        this.driver = "";
        this.model = "";
        this.place = "";
        this.status = PrinterStatus.DISABLED;
    }

    public Integer getACListId() {
        return this.aclistid;
    }

    public String getConfig() {
        return this.config;
    }

    public String getContact() {
        return this.contact;
    }

    public String getDriver() { 
        return this.driver;
    }

    public String getName() {
        return this.name;
    }

    public String getModel() {
        return this.model;
    }

    public Integer getOwnerId() {
        return this.ownerid;
    }

    public String getQueue() {
        return this.queue;
    }

    public String getPlace() {
        return this.place;
    }

    public PrinterStatus getStatus() {
        return this.status;
    }

    public PrinterEntity setACListId(Integer aclistid) {
        this.aclistid = aclistid;
        return this;
    }

    public PrinterEntity setConfig(String config) {
        this.config = config;
        return this;
    }

    public PrinterEntity setContact(String contact) {
        this.contact = contact;
        return this;
    }

    public PrinterEntity setDriver(String driver) { 
        this.driver = driver; 
        return this; 
    }

    public PrinterEntity setName(String name) {
        this.name = name;
        return this;
    }

    public PrinterEntity setModel(String model) {
        this.model = model;
        return this;
    }

    public PrinterEntity setOwnerId(Integer ownerid) {
        this.ownerid = ownerid;
        return this;
    }

    public PrinterEntity setQueue(String queue) {
        this.queue = queue;
        return this;
    }

    public PrinterEntity setPlace(String place) {
        this.place = place;
        return this;
    }

    public PrinterEntity setStatus(PrinterStatus status) {
        this.status = status;
        return this;
    }

}
