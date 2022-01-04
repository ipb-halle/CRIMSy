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

/**
 * Utility class for DOM element selectors
 * 
 * @author flange
 */
public class Selectors {
    private Selectors() {
    }

    private static final String TEST_ATTRIBUTE_NAME = "data-test-id";

    public static String css(String testId) {
        return String.format("[%s=\"%s\"]", TEST_ATTRIBUTE_NAME, testId);
    }

    public static String css(String htmlElement, String testId) {
        return String.format("%s[%s=\"%s\"]", htmlElement, TEST_ATTRIBUTE_NAME,
                testId);
    }
}