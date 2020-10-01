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
package de.ipb_halle.lbac.container.bean;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerSearchMaskBean implements Serializable {

    private String containerSearchName;
    private String containerSearchId;
    private String searchProject;
    private String searchLocation;
    private User currentUser;

    @Inject
    protected ContainerOverviewBean overviewBean;

    @Inject
    protected ContainerService containerService;

    @Inject
    protected ProjectService projectService;

    Logger logger = LogManager.getLogger(this.getClass().getName());

    public void actionClearSearchFilter() {
        containerSearchName = null;
        containerSearchId = null;
        searchProject = null;
        searchLocation = null;
        overviewBean.actionStartFilteredSearch();
    }

    public List<String> getSimilarContainerNames(String pattern) {
        Set<String> names = new HashSet<>();
        Set<Container> container = containerService.getSimilarContainerNames(pattern, currentUser);
        for (Container c : container) {
            names.add(c.getLabel());
        }
        return new ArrayList<>(names);
    }

    public List<Container> getSimilarContainers(String pattern) {

        Set<Container> container = containerService.getSimilarContainerNames(pattern, currentUser);
        return new ArrayList<>(container);

    }

    public List<String> getSimilarProjectNames(String pattern) {
        return projectService.getSimilarProjectNames(pattern, currentUser);
    }

    public String getContainerSearchName() {
        return containerSearchName;
    }

    public void setContainerSearchName(String containerSearchName) {
        this.containerSearchName = containerSearchName;
    }

    public String getContainerSearchId() {
        return containerSearchId;
    }

    public void setContainerSearchId(String containerSearchId) {
        this.containerSearchId = containerSearchId;
    }

    public String getSearchProject() {
        return searchProject;
    }

    public void setSearchProject(String searchProject) {
        this.searchProject = searchProject;
    }

    public String getSearchLocation() {
        return searchLocation;
    }

    public void setSearchLocation(String searchLocation) {
        this.searchLocation = searchLocation;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
    }

    public void handleSelect(SelectEvent event) {

    }

}
