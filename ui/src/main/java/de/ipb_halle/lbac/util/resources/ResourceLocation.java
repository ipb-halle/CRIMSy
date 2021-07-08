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
package de.ipb_halle.lbac.util.resources;

import de.ipb_halle.lbac.material.common.HazardType;

/**
 * 
 * @author flange
 */
public class ResourceLocation {
    private ResourceLocation() {
    }

    private static String hazardImageLocationTemplate = "img/hazards/%s.png";

    /**
     * Returns the image location in the resources hazards sub-folder for the
     * given hazard.
     * 
     * @param hazard
     * @return
     */
    public static String getHazardImageLocation(String hazard) {
        return String.format(hazardImageLocationTemplate, hazard);
    }

    /**
     * Returns the image location in the resources hazards sub-folder for the
     * given hazard type.
     * 
     * @param hazard
     * @return
     */
    public static String getHazardImageLocation(HazardType hazard) {
        return getHazardImageLocation(hazard.getName());
    }
}