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
package de.ipb_halle.pageobjects.pages.materials.models;

/**
 * Model class for input data in {@link MaterialEditPage}.
 * 
 * @author flange
 */
public class MaterialModel {
    private String project;
    private String materialType;
    private MaterialNamesModel materialNamesModel;
    private IndicesModel indicesModel;
    private StructureInfosModel structureInfosModel;
    private HazardsModel hazardsModel;
    private StorageModel storageModel;

    /*
     * Fluent setters
     */
    public MaterialModel project(String project) {
        this.project = project;
        return this;
    }

    public MaterialModel materialType(String materialType) {
        this.materialType = materialType;
        return this;
    }

    public MaterialModel materialNamesModel(MaterialNamesModel materialNamesModel) {
        this.materialNamesModel = materialNamesModel;
        return this;
    }

    public MaterialModel indicesModel(IndicesModel indicesModel) {
        this.indicesModel = indicesModel;
        return this;
    }

    public MaterialModel structureInfosModel(StructureInfosModel structureInfosModel) {
        this.structureInfosModel = structureInfosModel;
        return this;
    }

    public MaterialModel hazardsModel(HazardsModel hazardsModel) {
        this.hazardsModel = hazardsModel;
        return this;
    }

    public MaterialModel storageModel(StorageModel storageModel) {
        this.storageModel = storageModel;
        return this;
    }

    /*
     * Getters
     */
    public String getProject() {
        return project;
    }

    public String getMaterialType() {
        return materialType;
    }

    public MaterialNamesModel getMaterialNamesModel() {
        return materialNamesModel;
    }

    public IndicesModel getIndicesModel() {
        return indicesModel;
    }

    public StructureInfosModel getStructureInfosModel() {
        return structureInfosModel;
    }

    public HazardsModel getHazardsModel() {
        return hazardsModel;
    }

    public StorageModel getStorageModel() {
        return storageModel;
    }
}