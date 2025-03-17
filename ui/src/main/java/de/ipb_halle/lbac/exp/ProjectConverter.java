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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author fmauz
 */
@FacesConverter(value = "ProjectConverter")
public class ProjectConverter implements Converter {

    @Inject
    private ACListService acListService;

    @Inject
    ProjectService service;

    @Inject
    private UserBean userBean;

    @Override
    public Project getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        ExpProjectController projectController = new ExpProjectController(service, acListService, userBean.getCurrentAccount());
        Project p = projectController.getProjectByName(string);
        return p;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) throws ConverterException {
        Project p = (Project) o;
        return p.getName();
    }

}
