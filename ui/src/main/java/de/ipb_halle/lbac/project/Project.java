/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.component.MaterialDetailType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Project {

    protected int id;
    protected String name;
    protected Double budget;
    protected List<BudgetReservation> budgetReservation = new ArrayList<>();
    protected boolean budgetBlocked;
    protected ProjectType projectType;
    protected String description;
    protected Map<String, String> projectIndices = new HashMap<>();
    protected User owner;
    protected ACList userGroups;
    protected Map<MaterialDetailType, ACList> detailTemplates = new HashMap<>();
    private Logger logger = LogManager.getLogger(this.getClass().getName());

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

        this.owner = owner;
        this.userGroups = userGroups;
        this.detailTemplates = detailTemplates;
        this.budgetReservation = budgetReservation;

    }

    public Project(ProjectType projectType, String projectName) {
        this.projectType = projectType;
        this.name = projectName;
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

    public void setOwner(User u) {
        this.owner = u;
    }

    public String getOwnerName() {
        return owner.getName();
    }

    public ACList getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(ACList userGroups) {
        this.userGroups = userGroups;
    }

    public Map<MaterialDetailType, ACList> getDetailTemplates() {
        return detailTemplates;
    }

    public void setDetailTemplates(Map<MaterialDetailType, ACList> detailTemplates) {
        this.detailTemplates = detailTemplates;
    }

    public UUID getOwnerID() {
        return this.owner.getId();
    }

    @Override
    public String toString() {
        return id + " - " + getName();
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

}
