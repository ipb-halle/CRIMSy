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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * 
 * @author flange
 */
@Named
@RequestScoped
public class ScrollBean {
    /**
     * Returns a JavaScript snippet that scrolls to the first DOM element that
     * is marked with the given CSS class and adds a scrolling offset. Requires
     * jQuery.
     * 
     * @param cssClass name of the CSS class
     * @param offset   offset in pixels
     * @return JS snippet
     */
    public String scrollToCSS(String cssClass, int offset) {
        StringBuilder sb = new StringBuilder(256);

        sb.append("$('.").append(cssClass)
                .append("').get()[0]?.scrollIntoView(true);");
        if (offset != 0) {
            sb.append("window.scrollBy(0,").append(offset).append(");");
        }

        return sb.toString();
    }

    /**
     * Returns a JavaScript snippet that scrolls to the first DOM element that
     * is marked with the given CSS class. Requires jQuery.
     * 
     * @param cssClass name of the CSS class
     * @return JS snippet
     */
    public String scrollToCSS(String cssClass) {
        return scrollToCSS(cssClass, 0);
    }
}
