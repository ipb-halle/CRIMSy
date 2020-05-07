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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.entity.DTO;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fbroda
 */
public class Printer extends ACObject implements DTO {

    private String name;
    private String config;
    private String contact;
    private String driver;
    private String model;
    private String place;
    private PrinterStatus status;

    /**
     * constructor
     */
    public Printer(PrinterEntity entity, ACList aclist, User owner) {
        setACList(aclist);
        setOwner(owner);
        this.name = entity.getName();
        this.config = entity.getConfig();
        this.contact = entity.getContact();
        this.driver = entity.getDriver();
        this.model = entity.getModel();
        this.place = entity.getPlace();
        this.status = entity.getStatus();
    }

    public PrinterEntity createEntity() {
        return new PrinterEntity()
            .setName(this.name)
            .setACListId(this.getACList().getId())
            .setConfig(this.config)
            .setContact(this.contact)
            .setDriver(this.driver)
            .setModel(this.model)
            .setOwnerId(getOwner().getId())
            .setPlace(this.place)
            .setStatus(this.status);
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

    public String getModel() {
        return this.model;
    }

    public String getName() {
        return this.name;
    }

    public String getPlace() {
        return this.place;
    }

    public PrinterStatus getStatus() {
        return this.status;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setDriver(String driver) { 
        this.driver = driver; 
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setStatus(PrinterStatus status) {
        this.status = status;
    }

}
