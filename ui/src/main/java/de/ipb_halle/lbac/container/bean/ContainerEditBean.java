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
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.bean.ContainerOverviewBean.Mode;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Representation of the controller of the ContainerEdit dialog with the data
 * tier
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerEditBean implements Serializable {

    @Inject
    protected ContainerService containerService;

    @Inject
    protected ProjectService projectService;

    private Integer containerHeight;
    private Container containerLocation;
    private Container containerToCreate = new Container();

    private List<ContainerType> containerTypes = new ArrayList<>();
    private Integer containerWidth;
    private User currentUser;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Mode mode = Mode.CREATE;
    private Container originalContainer;
    protected final List<String> gmoSafetyLevels = new ArrayList<>();
    private String preferredProjectName;
    protected ContainerLocalizer localizer = new ContainerLocalizer();

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        clearEditBean();
        initGmoSafetyLevels();
    }

    /**
     * removes all information from temporary container attribures
     */
    public void clearEditBean() {
        originalContainer = null;
        containerToCreate = new Container();
        preferredProjectName = null;
        containerLocation = null;
        containerWidth = null;
        containerHeight = null;
    }

    public Integer getContainerHeight() {
        return containerHeight;
    }

    public String getContainerName() {
        return containerToCreate.getLabel();
    }

    public Container getContainerLocation() {
        return containerLocation;
    }

    public boolean getContainerSwapDimensions() {
        return containerToCreate.getSwapDimensions();
    }

    public Container getContainerToCreate() {
        return containerToCreate;
    }

    public ContainerType getContainerType() {
        return containerToCreate.getType();
    }

    public List<ContainerType> getContainerTypesWithRankGreaterZero() {
        List<ContainerType> filteredContainerTypes = new ArrayList<>();
        for (ContainerType t : containerTypes) {
            if (t.getRank() > 0) {
                filteredContainerTypes.add(t);
                t.setLocalizedName(localizer.localizeString("container_type_" + t.getName()));
            }
        }
        return filteredContainerTypes;
    }

    public Integer getContainerWidth() {
        return containerWidth;
    }

    public boolean getContainerZeroBased() {
        return containerToCreate.getZeroBased();
    }

    public String getDialogTitle() {
        if (mode == Mode.CREATE) {
            return localizer.localizeString("container_edit_titel_create");
        } else if (mode == Mode.EDIT) {
            return localizer.localizeString("container_edit_titel_edit", originalContainer.getLabel());
        } else {
            return "";
        }
    }

    public String getFireArea() {
        return containerToCreate.getFireArea();
    }

    public List<String> getGmoSafetyLevels() {
        return gmoSafetyLevels;
    }

    public String getPreferredProjectName() {
        return preferredProjectName;
    }

    public String getGmoSafetyLevel() {
        return containerToCreate.getGmoSafetyLevel();
    }

    private void initGmoSafetyLevels() {
        gmoSafetyLevels.clear();
        gmoSafetyLevels.add(localizer.localizeString("container_gmosafetylevel_none"));
        gmoSafetyLevels.add("S1");
        gmoSafetyLevels.add("S2");
        gmoSafetyLevels.add("S3");
        gmoSafetyLevels.add("S4");
    }

    public boolean isDimensionVisible() {
        return containerToCreate.getType().getRank() != ContainerType.HIGHEST_RANK;
    }

    public boolean isEditable() {
        return mode == Mode.CREATE;
    }

    public boolean isGmoSafetyLevelVisible() {
        return containerToCreate.getType().getRank() == ContainerType.HIGHEST_RANK;
    }

    public void setContainerHeight(Integer containerHeight) {
        this.containerHeight = containerHeight;
    }

    public void setContainerLocation(Container containerLocation) {
        this.containerLocation = containerLocation;
    }

    public void setContainerName(String containerName) {
        containerToCreate.setLabel(containerName);
    }

    public void setContainerSwapDimensions(boolean s) {
        containerToCreate.setSwapDimensions(s);
    }

    public void setContainerType(ContainerType t) {
        containerToCreate.setType(t);
    }

    public void setContainerWidth(Integer containerWidth) {
        this.containerWidth = containerWidth;
    }

    public void setContainerZeroBased(boolean z) {
        containerToCreate.setZeroBased(z);
    }

    public void setFireArea(String fireArea) {
        containerToCreate.setFireArea(fireArea);
    }

    public void setGmoSafetyLevel(String level) {
        containerToCreate.setGmoSafetyLevel(level);
    }

    public void setPreferredProjectName(String preferredProjectName) {
        this.preferredProjectName = preferredProjectName;
    }

    public void startContainerEdit(Container c) {
        clearEditBean();
        mode = Mode.EDIT;
        containerTypes = containerService.loadContainerTypes();
        originalContainer = c;
        containerToCreate = c.copy();
        if (c.getProject() != null) {
            preferredProjectName = c.getProject().getName();
        } else {
            preferredProjectName = null;
        }
        if (c.getParentContainer() != null) {
            containerLocation = c.getParentContainer();
        } else {
            containerLocation = null;
        }

        containerWidth = c.getColumns();
        containerHeight = c.getRows();
    }

    public void startNewContainerCreation() {
        clearEditBean();
        containerToCreate = new Container();
        mode = Mode.CREATE;
        containerTypes = containerService.loadContainerTypes();
        containerToCreate.setType(containerTypes.get(0));
    }

    public Container getOriginalContainer() {
        return originalContainer;
    }

}
