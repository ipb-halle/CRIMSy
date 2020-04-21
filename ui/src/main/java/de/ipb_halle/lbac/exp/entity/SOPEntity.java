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
package de.ipb_halle.lbac.exp.entity;

import de.ipb_halle.lbac.message.LocalUUIDConverter;
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
 * First draft of an experiment. Access and permission 
 * management as well as history have been postponed so far. 
 * An experiment is a container for a collection of 
 * experiment records and can be characterized by a
 * unique code.
 *
 * @author fbroda
 */
@Entity
@Table(name = "exp_sops")
public class SOPEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer sopid;

    /**
     * The SOP document (FileObjectEntity; usually a PDF)
     */
    @Column
    private UUID documentid;

    /**
     * a short description for this SOP. 
     */
    @Column
    private String description;


    public String getDescription() {
        return this.description;
    }

    public UUID getDocumentId() { 
        return this.documentid; 
    }

    public Integer getSopId() {
        return this.sopid;
    }

    public SOPEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public SOPEntity setDocumentId(UUID documentid) { 
        this.documentid = documentid; 
        return this; 
    }

    public SOPEntity setSopId(Integer sopid) {
        this.sopid = sopid;
        return this;
    }
}
