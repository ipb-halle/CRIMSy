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
package de.ipb_halle.lbac.util.jsf;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * EL functions for numbers.
 * 
 * @author flange
 */
public final class NumberFunctions {
    private NumberFunctions() {
    }

    /**
     * Formats a given number according to the format pattern in the English
     * locale.
     * <p>
     * This implementation is derived from OmniFaces' Numbers class, which is
     * licensed under the Apache License, Version 2.0, see
     * https://github.com/omnifaces/omnifaces/blob/3.11/src/main/java/org/omnifaces/el/functions/Numbers.java
     * 
     * @param number  number to be formatted
     * @param pattern format pattern, see the documentation of the
     *                {@link DecimalFormat}
     * @return the formatted number
     */
    public static String formatNumberInEnglish(Number number, String pattern) {
        if (number == null) {
            return null;
        }

        DecimalFormat formatter = (DecimalFormat) NumberFormat
                .getNumberInstance(Locale.ENGLISH);
        formatter.applyPattern(pattern);
        return formatter.format(number);
    }
}