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
import java.util.ResourceBundle;

/**
 * 
 * @author flange
 */
public class I18n {
    private I18n() {
    }

    public static final String REQUIRED_VALIDATION_ERROR_KEY = "javax.faces.component.UIInput.REQUIRED";

    private static final String JSF_BUNDLE_BASENAME = "javax.faces.Messages";
    private static final String UI_BUNDLE_BASENAME = "de.ipb_halle.lbac.i18n.messages";

    public static String getJSFMessage(String key, Locale locale) {
        return getMessageFromBundle(JSF_BUNDLE_BASENAME, key, locale);
    }

    public static boolean isJSFMessage(String test, String key, Locale locale) {
        String pattern = getJSFMessage(key, locale);
        return isParsableMessage(test, pattern);
    }

    public static String getUIMessage(String key, Locale locale) {
        return getMessageFromBundle(UI_BUNDLE_BASENAME, key, locale);
    }

    public static boolean isUIMessage(String test, String key, Locale locale) {
        String pattern = getUIMessage(key, locale);
        return isParsableMessage(test, pattern);
    }

    private static String getMessageFromBundle(String baseName, String key,
            Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return null;
        }
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