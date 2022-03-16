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
package de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem;

import java.io.Serializable;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.bean.ContainerController;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * Controls the sixth step of the create solution wizard: The user defines a
 * location.
 * 
 * @author flange
 */
public class ConsumePartOfItemStep6Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ContainerController containerController; // r

    public ConsumePartOfItemStep6Controller(ContainerService containerService, UserBean userBean,
            MessagePresenter messagePresenter) {
        containerController = new ContainerController(null, containerService, userBean, messagePresenter);
    }

    /*
     * Getters/setters
     */
    public ContainerController getContainerController() {
        return containerController;
    }
}