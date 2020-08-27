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
package de.ipb_halle.lbac.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
@FacesConverter("RichTextConverter")
public class RichTextConverter implements Converter {

    private HTMLInputFilter filter = new HTMLInputFilter(false, true);
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        this.logger.info("getAsObject(): {}", string);
        return filter.filter(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        this.logger.info("getAsString() {}", o.toString());
        return o == null ? "" : filter.filter(o.toString());
    }

}
