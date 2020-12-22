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
package de.ipb_halle.lbac.items.bean;

import java.io.Serializable; 

/**
 *
 * @author fmauz
 */
public class SearchMaskValues implements Serializable {

    private final static long serialVersionUID = 1L;

    private String description;
    private String label;
    private String location;
    private String projectName;
    private String materialName;
    private String userName;

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public String getLocation() {
        return location;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getUserName() {
        return userName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
