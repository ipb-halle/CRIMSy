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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.forum.HTMLInputFilter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author fmauz
 */
@FacesConverter("InputConverter")
public class InputConverter implements Converter {

    private HTMLInputFilter filter = new HTMLInputFilter(false, true);

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        return filter.filter(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return filter.filter(o.toString());
    }

}
