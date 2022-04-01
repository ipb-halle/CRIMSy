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
package de.ipb_halle.pageobjects.pages.materials.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Model class for checkbox activation in {@link GHSData}.
 * 
 * @author flange
 */
public class GHSModel {
    private static final int FIRST = 1;
    private static final int LAST = 11;
    private Set<Integer> activated = new HashSet<>();

    /**
     * Activate the GHS number(s).
     * 
     * @param ghsNumbers GHS number(s) - valid range in the UI is 1 to 11 (GHS01 to GHS11).
     * @return this
     */
    public GHSModel activate(int... ghsNumbers) {
        for (int num : ghsNumbers) {
            activated.add(num);
        }

        return this;
    }

    public Set<Integer> getActivated() {
        return activated;
    }

    public Set<Integer> getDeactivated() {
        Set<Integer> deactivated = new HashSet<>();
        for (int i = FIRST; i <= LAST; i++) {
            if (!activated.contains(i)) {
                deactivated.add(i);
            }
        }
        return deactivated;
    }
}