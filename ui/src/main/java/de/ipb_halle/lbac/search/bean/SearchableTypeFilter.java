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
package de.ipb_halle.lbac.search.bean;

/**
 *
 * @author fmauz
 */
public class SearchableTypeFilter {

    private boolean materials;
    private boolean items;
    private boolean experiments;
    private boolean projects;
    private boolean documents;

    public boolean isMaterials() {
        return materials;
    }

    public void setMaterials(boolean materials) {
        this.materials = materials;
    }

    public boolean isItems() {
        return items;
    }

    public void setItems(boolean items) {
        this.items = items;
    }

    public boolean isExperiments() {
        return experiments;
    }

    public void setExperiments(boolean experiments) {
        this.experiments = experiments;
    }

    public boolean isProjects() {
        return projects;
    }

    public void setProjects(boolean projects) {
        this.projects = projects;
    }

    public boolean isDocuments() {
        return documents;
    }

    public void setDocuments(boolean documents) {
        this.documents = documents;
    }

}
