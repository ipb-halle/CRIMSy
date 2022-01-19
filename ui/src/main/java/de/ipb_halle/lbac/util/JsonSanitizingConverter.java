/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import com.google.json.JsonSanitizer;

/**
 * FacesConverter that sanitizes incoming and outgoing JSON data.
 * 
 * @author flange
 */
@FacesConverter("jsonSanitizingConverter")
public class JsonSanitizingConverter implements Converter {
    /**
     * @return the sanitized JSON as String or {@code null} if {@code value} is
     *         {@code null}
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        if (value != null) {
            return JsonSanitizer.sanitize(value);
        }
        return null;
    }

    /**
     * @return the sanitized JSON as String or empty String if {@code value} is
     *         {@code null} or empty or not a String
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object value) {
        if ((value != null) && (value instanceof String)
                && !((String) value).isEmpty()) {
            return JsonSanitizer.sanitize((String) value);
        }
        return "";
    }
}