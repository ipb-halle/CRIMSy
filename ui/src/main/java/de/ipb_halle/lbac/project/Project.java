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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.crimsy_api.DTO;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Project extends ACObject implements DTO, Serializable, Searchable {

    protected int id;
    protected String name;
    protected Double budget;
    protected List<BudgetReservation> budgetReservation = new ArrayList<>();
    protected boolean budgetBlocked;
    protected ProjectType projectType;
    protected String description;
    protected Map<String, String> projectIndices = new HashMap<>();
    protected Map<MaterialDetailType, ACList> detailTemplates = new HashMap<>();
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Date ctime;
    private Date mTime;
    private boolean deactivated;

    public Project() {
    }

    public Project(
            ProjectEntity pE,
            User owner,
            ACList userGroups,
            Map<MaterialDetailType, ACList> detailTemplates,
            List<BudgetReservation> budgetReservation) {
        this.id = pE.getId();
        this.name = pE.getName();
        this.budget = pE.getBudget();
        this.projectType = ProjectType.getProjectTypeById(pE.getProjectTypeId());
        this.budgetBlocked = pE.isBudgetBlocked();
        this.description = pE.getDescription();
        ctime = pE.getCtime();
        mTime = pE.getMtime();
        this.deactivated = pE.isDeactivated();

        this.setOwner(owner);
        this.setACList(userGroups);
        this.detailTemplates = detailTemplates;
        this.budgetReservation = budgetReservation;

    }

    public Project(ProjectType projectType, String projectName) {
        this.projectType = projectType;
        this.name = projectName;
        this.ctime = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public List<BudgetReservation> getBudgetReservation() {
        return budgetReservation;
    }

    public void setBudgetReservation(List<BudgetReservation> budgetReservation) {
        this.budgetReservation = budgetReservation;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public Map<String, String> getProjectIndices() {
        return projectIndices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String s) {
        this.description = s;
    }

    public String getOwnerName() {
        return getOwner().getName();
    }

    public ACList getUserGroups() {
        return getACList();
    }

    public Map<MaterialDetailType, ACList> getDetailTemplates() {
        return detailTemplates;
    }

    public void setDetailTemplates(Map<MaterialDetailType, ACList> detailTemplates) {
        this.detailTemplates = detailTemplates;
    }

    public Integer getOwnerID() {
        return this.getOwner().getId();
    }

    @Override
    public String toString() {
        // used to return "id - name", which is harder to test
        return getName();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Project other = (Project) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public ProjectEntity createEntity() {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(id);
        entity.setACList(getACList().getId());
        entity.setBudget(budget);
        entity.setBudgetBlocked(budgetBlocked);
        entity.setCtime(ctime);
        entity.setDescription(description);
        entity.setMtime(mTime);
        entity.setName(name);
        entity.setOwner(getOwnerID());
        entity.setProjectTypeId(projectType.getId());
        entity.setDeactivated(deactivated);
        return entity;
    }

    @Override
    public String getNameToDisplay() {
        return name;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Project)) {
            return false;
        }
        Project otherUser = (Project) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.PROJECT);
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

}
