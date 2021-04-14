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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;

import java.util.Date;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <code>Experiment</code>s are containers for the actions which are carried out
 * during experimentation and the information that is generated in this process.
 * They are characterized by a short code (which might be generated by some
 * strategy later on) and a description (a short abstract of the purpose of the
 * experiment).
 *
 * Currently all owner, ACL, and revision stuff has been postponed, it will be
 * definitely added in later releases.
 *
 * @author fbroda
 */
public class Experiment extends ACObject implements DTO, Searchable {
    
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    
    private Integer experimentid;

    /**
     * A code (experiment number) by which this experiment can be identified.
     * ToDo: Later versions may / will / should implement one or more strategies
     * to assign UNIQUE experiment numbers.
     */
    private String code;

    /*
     * short "abstract" for this experiment. 
     */
    private String description;

    /**
     * template flag templates hold no experiment data but can be cloned
     * (copied) to save users from repetitions
     */
    private boolean template;
    
    protected int projectId;
    protected ACList acList;
    protected User owner;
    protected Date creationTime;
    protected Project project;

//    protected ExperimenHistory history = new ExperimentHistory();
    /**
     *
     * @param experimentid
     * @param code
     * @param description
     * @param template
     * @param acList
     * @param owner
     * @param creationTime
     */
    public Experiment(
            Integer experimentid,
            String code,
            String description,
            boolean template,
            ACList acList,
            User owner,
            Date creationTime) {
        this.experimentid = experimentid;
        this.code = code;
        this.description = description;
        this.template = template;
        this.acList = acList;
        this.owner = owner;
        this.creationTime = creationTime;
    }

    /**
     * constructor
     *
     * @param e ExperimentEntity to construct from
     * @param acList
     * @param owner
     * @param project
     */
    public Experiment(ExperimentEntity e,
            ACList acList,
            User owner,
            Project project) {
        this.experimentid = e.getExperimentId();
        this.code = e.getCode();
        this.description = e.getDescription();
        this.template = e.getTemplate();
        this.acList = acList;
        this.owner = owner;
        this.creationTime = e.getCtime();
        this.project = project;
    }
    
    @Override
    public ExperimentEntity createEntity() {
        ExperimentEntity entity = new ExperimentEntity()
                .setExperimentId(this.experimentid)
                .setCode(this.code)
                .setDescription(this.description)
                .setTemplate(this.template)
                .setFolderId(1);
        if (project != null) {
            entity.setProjectid(project.getId());
        }
        entity.setACList(this.acList.getId());
        entity.setCtime(creationTime);
        entity.setOwner(this.owner.getId());
        return entity;
    }
    
    @Override
    public ACList getACList() {
        return acList;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public Date getCreationTime() {
        return creationTime;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public boolean getTemplate() {
        return this.template;
    }
    
    public int getProjectId() {
        return projectId;
    }
    
    public Integer getExperimentId() {
        return this.experimentid;
    }
    
    @Override
    public User getOwner() {
        return owner;
    }
    
    @Override
    public ACObject setACList(ACList acList) {
        this.acList = acList;
        return this;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
    
    public void setExperimentId(Integer experimentid) {
        this.experimentid = experimentid;
    }
    
    @Override
    public ACObject setOwner(User owner) {
        this.owner = owner;
        return this;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTemplate(boolean template) {
        this.template = template;
    }
    
    public Integer getId() {
        return experimentid;
    }
    
    @Override
    public String getNameToDisplay() {
        return code;
    }
    
    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Experiment)) {
            return false;
        }
        Experiment otherUser = (Experiment) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }
    
    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.EXPERIMENT);
    }
    
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    
    public void updateCode(String shortcut,String number){
        code=shortcut+number+"_"+code;
    }
    
}
