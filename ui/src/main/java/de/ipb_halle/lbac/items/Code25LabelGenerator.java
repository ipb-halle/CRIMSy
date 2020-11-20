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
package de.ipb_halle.lbac.items;

/**
 *
 * @author fmauz
 */
public class Code25LabelGenerator {

    public String generateLabel(int id) {
        int j = 3;
        int k = 0;
        String s = String.format("%09d", id);
        int l = s.length();

        /* 
         * Code 25 expects an even number of digits
         * --> prepend a zero as needed
         *
        if((l % 2) == 0) {
            s = "0" + s;
            l++;
        }
         */
        for (int i = 0; i < l; i++) {
            k = k + j * ((int) s.charAt(i) - 0x30);
            j = (j + 2) % 4;
        }
        k = (10 - (k % 10)) % 10;
        return s + Integer.toString(k);
    }
}
