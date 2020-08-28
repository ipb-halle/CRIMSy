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
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * FacesConverter which allows only a limited set of HTML tags 
 * to be used (b, em, i, strike, strong, sub, sup, u).
 * This is of utility for substance or organism names as 
 * "<i>tert</i>-Butanol" or "<i>Arabidopsis thaliana</i>".
 *
 * @author fmauz
 */
@FacesConverter("InputConverter")
public class InputConverter implements Converter {

    private final static String[] blockElements = new String[] {
            "b", "em", "i", "strike", "strong", "sub", "sup", "u" }; 

    private static PolicyFactory policy = new HtmlPolicyBuilder()
                                        .allowElements(blockElements)
                                        .allowTextIn(blockElements)
                                        .toFactory();

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Method to facilitate "mis-using" this class to sanitize data 
     * coming over the network
     * @param string 
     * @return sanitized string
     */
    public String filter(String string) {
        return policy.sanitize(string);
    }

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        return policy.sanitize(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        return o == null ? "" : policy.sanitize(o.toString());
    }

}
