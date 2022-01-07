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
package de.ipb_halle.pageobjects.pages.materials;

/**
 * Model class for input data in {@link MaterialSearchMaskPage}.
 * 
 * @author flange
 */
public class MaterialSearchMaskModel {
    private String name;
    private String id;
    private String userName;
    private String projectName;
    private String index;
    private String materialType;
    private String molfile;

    public MaterialSearchMaskModel name(String name) {
        this.name = name;
        return this;
    }

    public MaterialSearchMaskModel id(String id) {
        this.id = id;
        return this;
    }

    public MaterialSearchMaskModel userName(String userName) {
        this.userName = userName;
        return this;
    }

    public MaterialSearchMaskModel projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public MaterialSearchMaskModel index(String index) {
        this.index = index;
        return this;
    }

    public MaterialSearchMaskModel materialType(String materialType) {
        this.materialType = materialType;
        return this;
    }

    public MaterialSearchMaskModel molfile(String molfile) {
        this.molfile = molfile;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getIndex() {
        return index;
    }

    public String getMaterialType() {
        return materialType;
    }

    public String getMolfile() {
        return molfile;
    }
}