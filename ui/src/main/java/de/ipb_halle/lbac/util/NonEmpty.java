/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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

/**
 * This class provides a utility methods to map empty 
 * strings to null
 */
public class NonEmpty {

    /**
     * @param str the String
     * @return null if String is null or empty
     */
    public static String nullOrNonEmpty(String str) {
        if ((str == null) || str.isEmpty()) {
            return null;
        }
        return str;
    }

    /**
     * @param str the String
     * @return null if String is null or empty or '0'
     */
    public static Integer nullOrNonZero(String str) {
        if ((str == null) || str.isEmpty()) {
            return null;
        }
        int i = Integer.parseInt(str);
        if (i == 0) {
            return null;
        }
        return i;
    }

}
