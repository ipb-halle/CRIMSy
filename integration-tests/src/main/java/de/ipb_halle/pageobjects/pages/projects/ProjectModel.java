/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.pageobjects.pages.projects;

/**
 * Model class for input data in {@link ProjectEditPage}.
 * 
 * @author flange
 */
public class ProjectModel {
    private String name;
    private String projectType;
    private String owner;
    private String description;
    // TODO: groups

    /*
     * Fluent setters
     */
    public ProjectModel name(String name) {
        this.name = name;
        return this;
    }

    public ProjectModel projectType(String projectType) {
        this.projectType = projectType;
        return this;
    }

    public ProjectModel owner(String owner) {
        this.owner = owner;
        return this;
    }

    public ProjectModel description(String description) {
        this.description = description;
        return this;
    }

    /*
     * Getters
     */
    public String getName() {
        return name;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }
}