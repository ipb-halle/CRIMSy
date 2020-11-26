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
package de.ipb_halle.lbac.items.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.bean.ContainerLocalizer;

/**
 *
 * @author fmauz
 */
public class ContainerInfoPresenter {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private final Container container;
    private ContainerLocalizer containerLocalizer;

    public ContainerInfoPresenter(Container container) {
        this.container = container;
        this.containerLocalizer = new ContainerLocalizer();
    }

    public String getContainerName() {
        if (container == null || container.getLabel() == null) {
            return "";
        } else {
            return container.getLabel();
        }
    }

    public String getContainerType() {
        if (container == null) {
            return "";
        } else {
            container.getType().setLocalizedName(
                    containerLocalizer.localizeString(
                            "container_type_" + container.getType().getName()));
            return container.getType().getLocalizedName();
        }
    }

    public String getContainerProject() {
        if (container == null) {
            return "";
        } else {
            if (container.getProject() == null) {
                return "";
            } else {
                return container.getProject().getName();
            }
        }
    }

    public String getContainerLocation() {
        if (container == null) {
            return "";
        } else {
            if (container.getLocation(true, true) == null) {
                return "unknown";
            } else {
                return container.getLocation(true, true);
            }
        }
    }

    public void setContainerName(String containerName) {

    }

    public void setContainerLocalizer(ContainerLocalizer containerLocalizer) {
        this.containerLocalizer = containerLocalizer;
    }

}
