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
 * Utility class for coping with internationalization via resource bundles both
 * from JSF and from the CRIMSy UI project.
 * 
 * @author flange
 */
public class I18n {
    private I18n() {
    }

    /*
     * Resource bundle keys for the JSF messages. Add keys if necessary, the bundle
     * can be found in the myfaces-api package.
     */
    public static final String JSF_REQUIRED_VALIDATION_ERROR_KEY = "javax.faces.component.UIInput.REQUIRED";

    /*
     * resource bundle names
     */
    private static final String JSF_BUNDLE_BASENAME = "javax.faces.Messages";
    private static final String UI_BUNDLE_BASENAME = "de.ipb_halle.lbac.i18n.messages";

    /**
     * @param key    JSF resource bundle key
     * @param locale
     * @return the entry from the JSF resource bundle corresponding to the
     *         {@code key}
     */
    public static String getJSFMessage(String key, Locale locale) {
        return getMessageFromBundle(JSF_BUNDLE_BASENAME, key, locale);
    }

    /**
     * Checks a given test string against the entry for the given {@code key} in the
     * JSF resource bundle. It also accounts for format strings in the entry.
     * 
     * @param test   test string
     * @param key    JSF resource bundle key
     * @param locale
     * @return {@code false} if the test string does not match the entry
     */
    public static boolean isJSFMessage(String test, String key, Locale locale) {
        String pattern = getJSFMessage(key, locale);
        if (pattern != null) {
            return isParsableMessage(test, pattern);
        } else {
            return false;
        }
    }

    /**
     * @param key    CRIMSy UI resource bundle key
     * @param locale
     * @return the entry from the CRIMSy UI resource bundle corresponding to the
     *         {@code key}
     */
    public static String getUIMessage(String key, Locale locale) {
        return getMessageFromBundle(UI_BUNDLE_BASENAME, key, locale);
    }

    /**
     * Checks a given test string against the entry for the given {@code key} in the
     * CRIMSy UI resource bundle. It also accounts for format strings in the entry.
     * 
     * @param test   test string
     * @param key    CRIMSy UI resource bundle key
     * @param locale
     * @return {@code false} if the test string does not match the entry
     */
    public static boolean isUIMessage(String test, String key, Locale locale) {
        String pattern = getUIMessage(key, locale);
        if (pattern != null) {
            return isParsableMessage(test, pattern);
        } else {
            return false;
        }
    }

    private static String getMessageFromBundle(String baseName, String key, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        if ((key != null) && (bundle.containsKey(key))) {
            return bundle.getString(key);
        } else {
            String error = String.format("Could not locate message key '%s' in bundle '%s' for locale %s.", key,
                    baseName, locale.toString());
            throw new RuntimeException(error);
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