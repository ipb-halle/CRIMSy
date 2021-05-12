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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class MaterialHazardController {

    private HazardService hazardService;
    private Material material;
    private String imageString = "/resources/img/hazards/%s.png";

    public MaterialHazardController(HazardService hazardService, Material material) {
        this.hazardService = hazardService;
        this.material = material;
    }

    public boolean isHazardCategoryRendered(String category) {
        return hazardService.getAllowedCatsOf(material.getType()).contains(HazardType.Category.valueOf(category));
    }

    /**
     * Returns the location of the associated image of the hazard
     *
     * @param h
     * @return
     */
    public String getImageLocation(HazardType h) {
        return String.format(imageString, h.getName());
    }

}
