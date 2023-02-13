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

import de.ipb_halle.lbac.admission.ACObjectEntity;
import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "projects")
public class ProjectEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    @AttributeTag(type = AttributeType.PROJECT_NAME)
    private String name;

    @Column
    private Double budget;
    @Column
    private boolean budgetBlocked;

    @Column
    private int projectTypeId;

    @Column
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date ctime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date mtime;
    
    @AttributeTag(type = AttributeType.DEACTIVATED)
    @Column
    private boolean deactivated;

    /**
     * default constructor
     */
    public ProjectEntity() {
    }

    public ProjectEntity(Project p) {

        name = p.getName();
        budget = p.getBudget();
        budgetBlocked = p.budgetBlocked;
        projectTypeId = p.getProjectType().getId();
        super.setOwner(p.getOwnerID());
        super.setACList(p.getUserGroups().getId());
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

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

}
