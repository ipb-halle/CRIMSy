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

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * 
 * @author flange
 */
public class I18n {
    private I18n() {
    }

    public static final String REQUIRED_VALIDATION_ERROR_KEY = "javax.faces.component.UIInput.REQUIRED";

    public static String getJSFMessage(String key, Locale locale) {
        /*
         * requires: myfaces-api, same version we use in ui
         * read messages_xx.properties from there and extract the value for the given key
         */
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static boolean isJSFMessage(String test, String key, Locale locale) {
        String pattern = getJSFMessage(key, locale);
        return isParsableMessage(test, pattern);
    }

    public static String getUIMessage(String key, Locale locale) {
        // get messages_xx.properties from the resources of ui
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static boolean isUIMessage(String test, String key, Locale locale) {
        String pattern = getUIMessage(key, locale);
        return isParsableMessage(test, pattern);
    }

    private static boolean isParsableMessage(String test, String pattern) {
        MessageFormat format = new MessageFormat(pattern);
        try {
            format.parse(test);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}