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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Named
@FacesConverter(value = "containerConverter")
public class ContainerConverter implements Converter {

    @Inject
    private ContainerService containerService;
    Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        Container c = null;
        try {
            int id = Integer.parseInt(value.split("-")[0]);
            c = containerService.loadContainerById(id);
            c.setAutoCompleteString(c.getId() + "-" + c.getLabel());
        } catch (Exception e) {
            logger.warn("Could not set Container from: "+value);
        }
        return c;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null && object instanceof Container) {
            Container c = (Container) object;
            return c.getAutoCompleteString();
        } else {
            return null;
        }

    }
}
