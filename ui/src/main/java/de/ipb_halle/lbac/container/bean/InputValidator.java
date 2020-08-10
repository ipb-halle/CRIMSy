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

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Checks input values of a container for validity
 *
 * @author fmauz
 */
public class InputValidator implements Serializable {

    protected ErrorMessagePresenter errorMessagePresenter = new ErrorMessagePresenter();
    protected Integer height;
    protected final int MAX_WIDTH = 1000;
    protected final int MAX_HEIGHT = 1000;
    protected String preferredProjectName;
    protected String preferredLocationName;
    protected ContainerService containerService;
    protected Container containerToCheck;
    protected Integer width;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    protected final String ERROR_MESSAGE_DIMENSION_INVALIDE = "container_input_dimensions";
    protected final String ERROR_MESSAGE_LOCATION_INVALIDE = "container_input_location_invalide";
    protected final String ERROR_MESSAGE_LOCATION_TO_SMALL = "container_input_location_to_small";
    protected final String ERROR_MESSAGE_NAME_INVALIDE = "container_input_name_invalide";
    protected final String ERROR_MESSAGE_PROJECT_INVALIDE = "container_input_project_invalide";

    public InputValidator(ContainerService containerService) {
        this.containerService = containerService;
    }

    /**
     * Checks the name, project,dimension and location of the container for
     * validity.If a contraint is violated a FacesMessage will be presented to
     * the user.
     *
     * @param container
     * @param preferredProjectName
     * @param containerLocation
     * @param height
     * @param width
     * @return
     */
    public boolean isInputValideForCreation(
            Container container,
            String preferredProjectName,
            String containerLocation,
            Integer height,
            Integer width) {
        this.height = height;
        this.width = width;
        this.containerToCheck = container;
        this.preferredProjectName = preferredProjectName;
        this.preferredLocationName = containerLocation;
        boolean valide = isLabelValide();
        if (!valide) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_NAME_INVALIDE);
        }

        if (!isProjectValide()) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_PROJECT_INVALIDE);
        }
        valide = valide && isProjectValide();

        if (!isLocationAvailable()) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_LOCATION_INVALIDE);
        }
        valide = valide && isLocationAvailable();

        if (!isLocationBiggerThan()) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_LOCATION_TO_SMALL);
        }
        valide = valide && isLocationBiggerThan();
        if (!isDimensionsValide()) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_DIMENSION_INVALIDE);
        }
        valide = valide && isDimensionsValide();

        return valide;
    }

    /**
     * Width and height must be positive, integer and lower than the max values
     *
     * @return
     */
    protected boolean isDimensionsValide() {
        boolean valide = true;
        boolean noWidthEntry = width == null || width == 0;
        boolean noHeightEntry = height == null || height == 0;
        if (!noHeightEntry) {
            valide = valide && height <= MAX_HEIGHT && height >= 0 && Math.floor(height) == height;
        }
        if (!noWidthEntry) {
            valide = valide && width <= MAX_WIDTH && width >= 0 && Math.floor(width) == width;
        }
        return valide;
    }

    /**
     * Checks the containerlabel for existance and if only unique names for that
     * type are allowed, checks for uniqness.
     *
     * @return
     */
    protected boolean isLabelValide() {
        if (containerToCheck.getLabel() == null || containerToCheck.getLabel().trim().isEmpty()) {
            return false;
        }
        return !containerToCheck.getType().isUnique_name() || containerService.loadContainerByName(containerToCheck.getLabel()) == null;
    }

    /**
     * Checks if the location of the container is valide and matching the
     * expected name.
     *
     * @return
     */
    protected boolean isLocationAvailable() {
        boolean isLocationSet = preferredLocationName != null && !preferredLocationName.trim().isEmpty();
        if (isLocationSet) {
            if (containerToCheck.getParentContainer() == null) {
                return false;
            } else {
                return true;
            }
        } else {
            if (containerToCheck.getParentContainer() == null) {
                return true;
            } else {
                return false;
            }
        }

    }

    /**
     * Checks if the container fits into its location by comparing the rank of
     * the containertype (must be lower for fitting)
     *
     * @return
     */
    protected boolean isLocationBiggerThan() {
        if (containerToCheck.getParentContainer() == null) {
            return true;
        }
        return containerToCheck.getType().getRank() < containerToCheck.getParentContainer().getType().getRank();

    }

    /**
     * Checks if the project of the container is valide and matching the
     * expected one by comparing its name
     *
     * @return
     */
    protected boolean isProjectValide() {
        boolean valide;
        boolean preferedProjectSet = preferredProjectName != null
                && !preferredProjectName.trim().isEmpty();
        if (preferedProjectSet) {
            if (containerToCheck.getProject() == null) {
                valide = false;
            } else {
                valide = containerToCheck.getProject().getName().equals(preferredProjectName);
            }
        } else {
            valide = containerToCheck.getProject() == null;
        }
        return valide;
    }

    public ErrorMessagePresenter getErrorMessagePresenter() {
        return errorMessagePresenter;
    }

    public void setContainerService(ContainerService containerService) {
        this.containerService = containerService;
    }

    public void setErrorMessagePresenter(ErrorMessagePresenter errorMessagePresenter) {
        this.errorMessagePresenter = errorMessagePresenter;
    }

}
