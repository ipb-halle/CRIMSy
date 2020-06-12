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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;

import java.util.Date;
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
public class Experiment implements DTO {

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
     * template flag
     * templates hold no experiment data but can be cloned (copied)
     * to save users from repetitions
     */
    private boolean template;

    protected int projectId;
    protected ACList acList;
    protected User owner;
    protected Date creationTime;

//    protected ExperimenHistory history = new ExperimentHistory();
    /**
     *
     * @param experimentid
     * @param code
     * @param description
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
     */
    public Experiment(ExperimentEntity e,
            ACList acList,
            User owner) {
        this.experimentid = e.getExperimentId();
        this.code = e.getCode();
        this.description = e.getDescription();
        this.template = e.getTemplate();
        this.acList = acList;
        this.owner = owner;

    }

    @Override
    public ExperimentEntity createEntity() {
        return new ExperimentEntity()
                .setExperimentId(this.experimentid)
                .setCode(this.code)
                .setDescription(this.description)
                .setTemplate(this.template)
                .setACListId(this.acList.getId())
                .setOwnerId(this.owner.getId());
    }

    public ACList getACList() {
        return acList;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean getTemplate() {
        return this.template;
    }

    public Integer getExperimentId() {
        return this.experimentid;
    }

    public User getOwner() {
        return owner;
    }

    public void setACList(ACList acList) { 
        this.acList = acList; 
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setExperimentId(Integer experimentid) { 
        this.experimentid = experimentid; 
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }
}
