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

/**
 * Checks input values of a container for validity. The difference to the InputValidator
 * validator is, that a string is used to check if the label was changed.
 *
 * @author fmauz
 */
public class EditInputValidator extends InputValidator {

    private final String originalLabel;

    public EditInputValidator(
            ContainerService containerService,
            String originalLabel) {
        super(containerService);
        this.originalLabel = originalLabel;

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
    @Override
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

        boolean valide = originalLabel.equals(container.getLabel()) || isLabelValide();

        if (!valide) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_NAME_INVALIDE);
        }

        valide = valide && isProjectValide();
        if (!isProjectValide()) {
            errorMessagePresenter.presentErrorMessage(ERROR_MESSAGE_PROJECT_INVALIDE);
        }

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

}
