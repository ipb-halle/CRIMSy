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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.ACObjectEntity;
import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author fbroda
 */
@Entity
@Table(name = "experiments")
public class ExperimentEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @AttributeTag(type = AttributeType.BARCODE)
    private Integer experimentid;

    @Column
    @AttributeTag(type = AttributeType.EXP_CODE)
    private String code;

    @Column
    @AttributeTag(type = AttributeType.TEXT)
    private String description;

    @Column
    @AttributeTag(type = AttributeType.TEMPLATE)
    private boolean template;

    @Column
    private Integer folderid;

    @Column
    private Date ctime;

    @Column
    private Integer projectid;

    public String getCode() {
        return this.code;
    }

    public Date getCtime() {
        return ctime;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getExperimentId() {
        return this.experimentid;
    }

    public Integer getFolderId() {
        return this.folderid;
    }

    public boolean getTemplate() {
        return this.template;
    }

    public ExperimentEntity setCode(String code) {
        this.code = code;
        return this;
    }

    public ExperimentEntity setCtime(Date ctime) {
        this.ctime = ctime;
        return this;
    }

    public ExperimentEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public ExperimentEntity setExperimentId(Integer experimentid) {
        this.experimentid = experimentid;
        return this;
    }

    public ExperimentEntity setFolderId(Integer folderid) {
        this.folderid = folderid;
        return this;
    }

    public ExperimentEntity setTemplate(boolean template) {
        this.template = template;
        return this;
    }

    public Integer getProjectid() {
        return projectid;
    }

    public void setProjectid(Integer projectid) {
        this.projectid = projectid;
    }

}
