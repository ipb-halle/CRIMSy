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
 * This entity stores the data of a label:
 * <ul>
 * <li>id</li>
 * <li>name of the label</li>
 * <li>description (for display in tooltips)</li>
 * <li>configuration of the label</li>
 * <li>applicable printer model</li>
 * <li>applicable class (Item, Container, Material, ...)</li>
 * </ul>
 *
 * During runtime the available labels are selected on the basis of the chosen 
 * printer and the java class for which the label should be printed.
 *
 * @author fbroda
 */
@Entity
@Table(name = "labels")
public class LabelEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private Integer id;

    /**
     * JSON config of the label 
     */
    @Column
    private String config;

    /**
     * a short description of the label for display in tool tips 
     */
    @Column
    private String description;


    /**
     * the type of object, this label is applicable, e.g. Item, Collection, etc.
     * Space separated list.
     */
    @Column
    private String labeltype;

    /**
     * the name of the label to be displayed in a select box
     */
    @Column
    private String name;

    /**
     * the printer model which can print this label
     */
    @Column
    private String printermodel;

    /*
     * default constructor
     */
    public LabelEntity() {
        this.config = "";
        this.description = "";
        this.labeltype = "";
        this.printermodel = "";
        this.name = "";
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
        return this.labeltype;
    }

    public String getName() { 
        return this.name;
    }

    public String getPrinterModel() {
        return this.printermodel;
    }

    public LabelEntity setConfig(String config) {
        this.config = config;
        return this;
    }

    public LabelEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public LabelEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public LabelEntity setLabelType(String labeltype) {
        this.labeltype = labeltype;
        return this;
    }

    public LabelEntity setName(String name) {
        this.name = name;
        return this;
    }

    public LabelEntity setPrinterModel(String printermodel) {
        this.printermodel = printermodel;
        return this;
    }
}
