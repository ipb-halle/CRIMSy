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
package de.ipb_halle.lbac.project;

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
 *
 * @author fmauz
 */
@Entity
@Table(name = "projects")
public class ProjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    public ProjectEntity() {

    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;
    @Column
    private String name;
    @Column
    private Double budget;
    @Column
    private boolean budgetBlocked;

    @Column
    private int projectTypeId;

    @Column
    private Integer ownerId;

    @Column
    private Integer aclist_id;

    @Column
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date ctime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date mtime;

    public ProjectEntity(Project p) {
      
        name = p.getName();
        budget = p.getBudget();
        budgetBlocked = p.budgetBlocked;
        projectTypeId = p.getProjectType().getId();
        ownerId = p.getOwnerID();
        aclist_id = p.getUserGroups().getId();
        description = p.getDescription();
        ctime = new Date();
        mtime = new Date();

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public boolean isBudgetBlocked() {
        return budgetBlocked;
    }

    public void setBudgetBlocked(boolean budgetBlocked) {
        this.budgetBlocked = budgetBlocked;
    }

    public int getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(int projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getAclist_id() {
        return aclist_id;
    }

    public void setAclist_id(Integer aclist_id) {
        this.aclist_id = aclist_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

}
