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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.DTO;

import java.io.Serializable;
import java.util.Date;

/**
 * @author fbroda
 */
public class Label implements DTO {

    private Integer id;
    private String config;
    private String description;
    private String labelType;
    private String name;
    private String printerModel;

    /**
     * constructor
     */
    public Label(LabelEntity entity) {
        this.id = entity.getId();
        this.config = entity.getConfig();
        this.description = entity.getDescription();
        this.labelType = entity.getLabelType();
        this.name = entity.getName();
        this.printerModel = entity.getPrinterModel();
    }

    public LabelEntity createEntity() {
        return new LabelEntity()
            .setId(this.id)
            .setConfig(this.config)
            .setDescription(this.description)
            .setLabelType(this.labelType)
            .setName(this.name)
            .setPrinterModel(this.printerModel);
    }

    @Override
    public boolean equals(Object o) { 
        if ((o != null) && o.getClass().equals(this.getClass())) {
            Label l = (Label) o;
            if ((getId() != null) && getId().equals(l.getId())) {
                return true;
            }
        }
        return false;
    }

    public String getConfig() {
        return this.config;
    }

    public String getDescription() { 
        return this.description;
    }

    public Integer getId() {
        return this.id;
    }

    public String getLabelType() {
        return this.labelType;
    }

    public String getName() {
        return this.name;
    }

    public String getPrinterModel() {
        return this.printerModel;
    }

    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLabelType(String labelType) { 
        this.labelType = labelType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrinterModel(String printerModel) {
        this.printerModel = printerModel;
    }

}
