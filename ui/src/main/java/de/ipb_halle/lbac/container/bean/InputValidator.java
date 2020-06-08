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
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class InputValidator implements Serializable {

    private Container containerToCheck;
    private String preferredProjectName;
    private String preferredLocationName;
    private Integer height;
    private Integer width;

    @Inject
    private ContainerService containerService;

    Logger logger = LogManager.getLogger(this.getClass().getName());
    private ErrorMessagePresenter errorMessagePresenter = new ErrorMessagePresenter();

    public boolean isInputValideForCreation(
            Container container,
            String preferredProjectName,
            String containerLocation,
            Integer height, Integer width) {
        this.height = height;
        this.width = width;
        this.containerToCheck = container;
        this.preferredProjectName = preferredProjectName;
        this.preferredLocationName = containerLocation;

        boolean valide = isLabelValide();
        if (!valide) {
            logger.info("Name validity failed");
            errorMessagePresenter.presentErrorMessage("container_input_name_invalide");
        }

        if (valide && !checkProjectValidity()) {
            logger.info("Project validity failed");
            errorMessagePresenter.presentErrorMessage("container_input_project_invalide");
        }
        valide = valide && checkProjectValidity();

        if (valide && !isLocationAvailable()) {
            logger.info("Location validity failed");
            errorMessagePresenter.presentErrorMessage("container_input_location_invalide");
        }
        valide = valide && isLocationAvailable();

        if (valide && !isLocationBiggerThan()) {
            logger.info("Size validity failed");
            errorMessagePresenter.presentErrorMessage("container_input_location_to_small");
        }
        valide = valide && isLocationBiggerThan();
        if (valide && !isDimensionsValide()) {
            logger.info("Dimesion validity failed");
            errorMessagePresenter.presentErrorMessage("container_input_dimensions");
        }
        valide = valide && isDimensionsValide();

        return valide;
    }

    private boolean isDimensionsValide() {
        boolean valide = true;
        boolean noWidthEntry = width == null || width == 0;
        boolean noHeightEntry = height == null || height == 0;
        if (!noHeightEntry) {
            valide = valide && height <= 1000 && height >= 0 && Math.floor(height) == height;
        }
        if (!noWidthEntry) {
            valide = valide && width <= 1000 && width >= 0 && Math.floor(width) == width;
        }
        return valide;
    }

    private boolean isLabelValide() {
        if (containerToCheck.getLabel() == null || containerToCheck.getLabel().trim().isEmpty()) {
            return false;
        }
        return containerService.loadContainerByName(containerToCheck.getLabel()) == null;
    }

    private boolean isLocationAvailable() {
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

    private boolean isLocationBiggerThan() {
        if (containerToCheck.getParentContainer() == null) {
            return true;
        }
        return containerToCheck.getType().getRank() < containerToCheck.getParentContainer().getType().getRank();

    }

    private boolean checkProjectValidity() {
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

    public void setErrorMessagePresenter(ErrorMessagePresenter errorMessagePresenter) {
        this.errorMessagePresenter = errorMessagePresenter;
    }

    public void setContainerService(ContainerService containerService) {
        this.containerService = containerService;
    }

}
