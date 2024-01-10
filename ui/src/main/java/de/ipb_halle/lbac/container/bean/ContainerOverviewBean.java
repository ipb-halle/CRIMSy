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
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Triggers save and search actions from UI.and initialises the readable
 * containers for the logged in user.
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerOverviewBean implements Serializable {

    @Inject
    private transient MessagePresenter messagePresenter;

    @Inject
    protected ContainerService containerService;
    @Inject
    protected ContainerEditBean editBean;
    @Inject
    protected ProjectService projectService;
    @Inject
    protected ContainerSearchMaskBean searchMaskBean;

    @Inject
    protected LoggingProfiler loggingProfiler;

    private User currentUser;
    private Mode mode;
    private List<Container> readableContainer = new ArrayList<>();
    protected InputValidator validator;

    protected ContainerLocalizer localizer;
    protected CallBackController callBackController = new CallBackController();
    protected ValidatorFactory validatorFactory;

    private transient Logger logger = LogManager.getLogger(this.getClass().getName());

    public enum Mode {
        SHOW, EDIT, CREATE
    }

    @PostConstruct
    private void init() {
        loggingProfiler.profilerStart("ContainerOverviewBean");

        localizer = new ContainerLocalizer(messagePresenter);
        validatorFactory = new ValidatorFactory(containerService);

        loggingProfiler.profilerStop("ContainerOverviewBean");
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("ContainerOverviewBean.setCurrentAccount");

        currentUser = evt.getCurrentAccount();
        setReadableContainer(containerService.loadContainersWithoutItems(currentUser));
        mode = Mode.SHOW;
        loggingProfiler.profilerStop("ContainerOverviewBean.setCurrentAccount");

    }

    public void actionCancel() {
        mode = Mode.SHOW;
    }

    public void actionContainerDeactivate(Container c) {
        containerService.deactivateContainer(c);
        actionStartFilteredSearch();
    }

    public void actionContainerEdit(Container c) {
        editBean.startContainerEdit(c);
        mode = Mode.EDIT;
    }

    public void actionSecondButtonClick() {
        editBean.startNewContainerCreation();
        mode = Mode.CREATE;
    }

    public void actionStartFilteredSearch() {
        Map<String, Object> cmap = new HashMap<>();
        if (searchMaskBean.getContainerSearchName() != null && !searchMaskBean.getContainerSearchName().trim().isEmpty()) {
            cmap.put("label", searchMaskBean.getContainerSearchName());
        }
        if (searchMaskBean.getContainerSearchId() != null && !searchMaskBean.getContainerSearchId().trim().isEmpty()) {
            cmap.put("id", Integer.valueOf(searchMaskBean.getContainerSearchId()));
        }
        if (searchMaskBean.getSearchProject() != null && !searchMaskBean.getSearchProject().trim().isEmpty()) {
            cmap.put("project", searchMaskBean.getSearchProject());
        }
        if (searchMaskBean.getSearchLocation() != null && !searchMaskBean.getSearchLocation().trim().isEmpty()) {
            cmap.put("location", searchMaskBean.getSearchLocation());
        }
        setReadableContainer(containerService.loadContainersWithoutItems(currentUser, cmap));
    }

    public void actionTriggerContainerSave() {
        boolean success;
        editBean.getContainerToCreate().setParentContainer(editBean.getContainerLocation());
        if (mode == Mode.CREATE) {
            success = saveNewContainer();
        } else {
            success = saveEditedContainer();
        }
        callBackController.addCallBackParameter("success", success);
        if (success) {
            mode = Mode.SHOW;
            editBean.clearEditBean();
        }
    }

    public Mode getMode() {
        return mode;
    }

    public String getProjectName(Container c) {
        if (c.getProject() == null) {
            return "";
        } else {
            return c.getProject().getName();
        }
    }

    public List<Container> getReadableContainer() {
        return readableContainer;
    }

    public boolean isFirstButtonVisible() {
        return mode != Mode.SHOW;
    }

    public boolean isSecondButtonVisible() {
        return mode == Mode.SHOW;
    }

    public boolean isTopLevelButtonVisible() {
        return mode == Mode.SHOW;
    }

    private boolean saveEditedContainer() {
        validator = new EditInputValidator(
                containerService,
                editBean.getOriginalContainer().getLabel());

        if (editBean.getPreferredProjectName() != null
                && !editBean.getPreferredProjectName().trim().isEmpty()) {
            editBean.getContainerToCreate()
                    .setProject(loadProjectByName());
        }
        if (editBean.getPreferredProjectName() == null || editBean.getPreferredProjectName().trim().isEmpty()) {
            editBean.getContainerToCreate()
                    .setProject(null);
        }

        if (editBean.getContainerLocation() != null) {
            editBean.getContainerToCreate()
                    .setParentContainer(editBean.getContainerLocation());
        }

        boolean valide = validator.isInputValideForCreation(
                editBean.getContainerToCreate(),
                editBean.getPreferredProjectName(),
                editBean.getContainerLocation(),
                editBean.getContainerHeight(),
                editBean.getContainerWidth());

        if (valide) {
            mode = Mode.SHOW;
            editBean.getContainerToCreate().setParentContainer(editBean.getContainerLocation());
            containerService.saveEditedContainer(editBean.getContainerToCreate());
        }

        actionStartFilteredSearch();
        return valide;
    }

    public boolean saveNewContainer() {
        validator = new InputValidator(containerService);
        validator.setErrorMessagePresenter(messagePresenter);
        if (editBean.getPreferredProjectName() != null
                && !editBean.getPreferredProjectName().trim().isEmpty()) {
            editBean.getContainerToCreate()
                    .setProject(loadProjectByName());
        }
        boolean valide = validator.isInputValideForCreation(
                editBean.getContainerToCreate(),
                editBean.getPreferredProjectName(),
                editBean.getContainerLocation(),
                editBean.getContainerHeight(),
                editBean.getContainerWidth()
        );
        editBean.getContainerToCreate().setParentContainer(editBean.getContainerLocation());

        if (valide) {
            setDimensionIfPossible();
            containerService.saveContainer(editBean.getContainerToCreate());
            mode = Mode.SHOW;
            actionStartFilteredSearch();
        }
        return valide;
    }

    private void setDimensionIfPossible() {
        if (editBean.isDimensionVisible()) {
            Integer height = editBean.getContainerHeight() == null ? null : Math.max(1, editBean.getContainerHeight());
            Integer width = editBean.getContainerWidth() == null ? null : Math.max(1, editBean.getContainerWidth());
            editBean.getContainerToCreate().setRows(height);
            editBean.getContainerToCreate().setColumns(width);
        }
    }

    public void setReadableContainer(List<Container> readableContainer) {
        this.readableContainer = readableContainer;
        for (Container c : readableContainer) {
            c.getType().setLocalizedName(
                    localizer.localizeString("container_type_" + c.getType().getName()));
        }
    }

    private Project loadProjectByName() {
        ProjectSearchRequestBuilder builder = new ProjectSearchRequestBuilder(currentUser, 0, 1);
        builder.setProjectName(editBean.getPreferredProjectName().trim());
        SearchResult result = projectService.loadProjects(builder.build());
        Node n = result.getNode();
        return (Project) result.getAllFoundObjects(Project.class, n).get(0);

    }

}
