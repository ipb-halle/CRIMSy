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
package de.ipb_halle.pageobjects.util;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Utility class for DOM element selectors
 * 
 * @author flange
 */
public class Selectors {
    private Selectors() {
    }

    private static final String TEST_ATTRIBUTE_NAME = "data-test-id";

    /**
     * @param testId
     * @return CSS selector that selects {@code testId} in the DOM element attribute
     *         {@code data-test-id}
     */
    public static String testId(String testId) {
        return String.format("[%s='%s']", TEST_ATTRIBUTE_NAME, testId);
    }

    /**
     * @param htmlElement
     * @param testId
     * @return CSS selector that selects for {@code testId} in the attribute
     *         {@code data-test-id} in DOM elements of type {@code htmlElement}
     */
    public static String testId(String htmlElement, String testId) {
        return elementWithAttribute(htmlElement, TEST_ATTRIBUTE_NAME, testId);
    }

    /**
     * @param classes
     * @return CSS selector that selects for given CSS classes
     */
    public static String cssClasses(String... classes) {
        StringBuilder sb = new StringBuilder();
        for (String c : classes) {
            sb.append(".").append(c);
        }

        return sb.toString();
    }

    /**
     * @param htmlElement
     * @param classes
     * @return CSS selector that selects for the given CSS classes in DOM elements
     *         of type {@code htmlElement}
     */
    public static String elementWithCssClasses(String htmlElement, String... classes) {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlElement);
        for (String c : classes) {
            sb.append(".").append(c);
        }

        return sb.toString();
    }

    /**
     * @param htmlElement
     * @param attributeName
     * @param attributeValue
     * @return CSS selector that selects for the attribute in DOM elements of type
     *         {@code htmlElement}
     */
    public static String elementWithAttribute(String htmlElement, String attributeName, String attributeValue) {
        return String.format("%s[%s='%s']", htmlElement, attributeName, attributeValue);
    }

    /**
     * @param htmlElement
     * @param attributes
     * @return CSS selector that selects for the attributes in the DOM elements of
     *         type {@code htmlElement}
     */
    public static String elementWithAttributes(String htmlElement, Map<String, String> attributes) {
        StringJoiner sj = new StringJoiner("");
        sj.add(htmlElement);
        if (attributes != null) {
            attributes.forEach((key, value) -> sj.add(String.format("[%s='%s']", key, value)));
        }

        return sj.toString();
    }
}