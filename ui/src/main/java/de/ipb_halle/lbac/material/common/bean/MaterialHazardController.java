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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialHazardController {

    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    private HazardService hazardService;
    private Material material;
    private String imageString = "/resources/img/hazards/%s.png";
    private List<HazardType> selectedHazards;
    private String hStatements;
    private String pStatements;
    private String customText;
    private boolean radioctive;
    private boolean editable;

    public MaterialHazardController(
            HazardService hazardService,
            Material material,
            boolean isEditable) {
        this.hazardService = hazardService;
        this.material = material;
        this.selectedHazards = new ArrayList<>(material.getHazards().getHazards().keySet());
        this.editable=isEditable;
    }

    /**
     * Checks if the given category can be choosen at the current material
     *
     * @param category
     * @return
     */
    public boolean isHazardCategoryRendered(String category) {
        return hazardService.getAllowedCatsOf(material.getType()).contains(HazardType.Category.valueOf(category));
    }
    
    public boolean isHazardEditable(){
        return editable;
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

    /**
     * Returns the possbible hazards of a category sorted by the id asc
     *
     * @param category
     * @return
     */
    public List<HazardType> getHazardsOfType(String category) {
        List<HazardType> types = new ArrayList<>(hazardService.getHazardOf(HazardType.Category.valueOf(category)));
        types.sort((c1, c2) -> c1.getId() > c2.getId() ? 1 : -1);
        return types;
    }

    public Map<HazardType, String> createHazardMap() {
        Map<HazardType, String> hazards = new HashMap<>();
        for (HazardType hazard : selectedHazards) {
            hazards.put(hazard, null);
        }
        if (hStatements != null && !hStatements.trim().isEmpty()) {
            hazards.put(hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(0), hStatements);
        }
        if (pStatements != null && !pStatements.trim().isEmpty()) {
            hazards.put(hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(1), pStatements);
        }
        if (radioctive) {
            hazards.put(hazardService.getHazardOf(HazardType.Category.RADIOACTIVITY).get(0), null);
        }
        if (customText != null && !customText.trim().isEmpty()) {
            hazards.put(hazardService.getHazardOf(HazardType.Category.CUSTOM).get(0), customText);
        }
        return hazards;
    }

    public String getLocalizedName(HazardType h) {
        return h.getName();
    }

    public List<HazardType> getSelectedHazards() {
        return selectedHazards;
    }

    public void setSelectedHazards(List<HazardType> selectedHazards) {
        this.selectedHazards = selectedHazards;
    }

    public String gethStatements() {
        return hStatements;
    }

    public void sethStatements(String hStatements) {
        this.hStatements = hStatements;
    }

    public String getpStatements() {
        return pStatements;
    }

    public void setpStatements(String pStatements) {
        this.pStatements = pStatements;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    public boolean isRadioctive() {
        return radioctive;
    }

    public void setRadioctive(boolean radioctive) {
        this.radioctive = radioctive;
    }

}
