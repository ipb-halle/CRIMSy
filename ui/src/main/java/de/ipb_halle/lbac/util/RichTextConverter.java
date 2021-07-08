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

import java.util.regex.Pattern;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * FacesConverter which sanitizes HTML generated from a Text Editor. Compared to
 * <code>InputConverter</code>, it allows a broader range of HTML tags and
 * attributes.
 *
 * @author fmauz
 */
@FacesConverter("RichTextConverter")
public class RichTextConverter implements Converter {

    private final static String[] blockElements = new String[]{
        "div", "b", "em", "h1", "h2", "h3", "h4", "h5", "h6", "i", "li",
        "ol", "p", "s", "span", "strong", "sub", "sup", "u", "ul"};

    private final static String[] singleElements = new String[]{
        "br", "hr"};

    private static PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements(blockElements)
            .allowTextIn(blockElements)
            .allowAttributes("class")
            .matching(Pattern.compile("((ql-(indent-[0-9]|font-(serif|monospace)|size-(small|large|huge)))|\\s)*"))
            .onElements(blockElements)
            .allowAttributes("style")
            .matching(Pattern.compile("color:( )?rgb\\(\\d{1,3}, \\d{1,3}, \\d{1,3}\\);"))
            .onElements(blockElements)
            .allowElements(singleElements)
            .toFactory();

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Method to facilitate "mis-using" this class to sanitize data coming over
     * the network
     *
     * @param string
     * @return sanitized string
     */
    public String filter(String string) {
        return policy.sanitize(string);
    }

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
        // this.logger.info("getAsObject(): {}", string);
        return string == null ? null : policy.sanitize(string).trim();
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        // this.logger.info("getAsString() {}", o.toString());
        return o == null ? "" : policy.sanitize(o.toString());
    }

}
