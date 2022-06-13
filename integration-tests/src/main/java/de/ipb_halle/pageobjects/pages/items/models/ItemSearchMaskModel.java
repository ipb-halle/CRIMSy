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
package de.ipb_halle.pageobjects.pages.items.models;

/**
 * Model class for input data in {@link ItemSearchMaskPage}.
 * 
 * @author flange
 */
public class ItemSearchMaskModel {
    private String materialName;
    private String label;
    private String userName;
    private String projectName;
    private String location;
    private String description;

    /*
     * Fluent setters
     */
    public ItemSearchMaskModel materialName(String materialName) {
        this.materialName = materialName;
        return this;
    }

    public ItemSearchMaskModel label(String label) {
        this.label = label;
        return this;
    }

    public ItemSearchMaskModel userName(String userName) {
        this.userName = userName;
        return this;
    }

    public ItemSearchMaskModel projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public ItemSearchMaskModel location(String location) {
        this.location = location;
        return this;
    }

    public ItemSearchMaskModel description(String description) {
        this.description = description;
        return this;
    }

    /*
     * Getters
     */
    public String getMaterialName() {
        return materialName;
    }

    public String getLabel() {
        return label;
    }

    public String getUserName() {
        return userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }
}