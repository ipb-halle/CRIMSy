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
package de.ipb_halle.pageobjects.pages.settings.collectionmanagement;

/**
 * Model class for input data in {@link CollectionDialog}.
 * 
 * @author flange
 */
public class CollectionModel {
    private String name;
    private String description;

    /*
     * Fluent setters
     */
    public CollectionModel name(String name) {
        this.name = name;
        return this;
    }

    public CollectionModel description(String description) {
        this.description = description;
        return this;
    }

    /*
     * Getters
     */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}