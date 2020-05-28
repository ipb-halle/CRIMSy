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
package de.ipb_halle.lbac.container.bean;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerEditBean implements Serializable {

    private String containerName;
    private String containerSearchProject;
    private String containerSearchLocation;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerSearchProject() {
        return containerSearchProject;
    }

    public void setContainerSearchProject(String containerSearchProject) {
        this.containerSearchProject = containerSearchProject;
    }

    public List<String> getSimilarProjectNames(String input) {
        return Arrays.asList("Funktionen von IQD Proteinen", "Struktur-Funktionsanalysen", "Entwicklung von bioinformatischen Tools");
    }

    public List<String> getSimilarLocationNames(String s) {
        return Arrays.asList("Chemikalienlager NWC", "R302", "R301");
    }

    public String getContainerSearchLocation() {
        return containerSearchLocation;
    }

    public void setContainerSearchLocation(String containerSearchLocation) {
        this.containerSearchLocation = containerSearchLocation;
    }

}
