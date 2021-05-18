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

import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
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

    private MessagePresenter messagePresenter = JsfMessagePresenter.getInstance();
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    private HazardService hazardService;
    private MaterialType materialType;
    private String imageString = "/resources/img/hazards/%s.png";
    private HazardType[] selectedHazards;
    private String hStatements;
    private String pStatements;
    private String customText;
    private boolean radioctive;
    private boolean editable;
    private String bioSavetyLevel;
    private List<String> possibleBioSavetyLevels = new ArrayList<>();

    private final int H_STATEMENT_ID = 10;
    private final int P_STATEMENT_ID = 11;
    private final int[] BSL_IDS = new int[]{12, 13, 14, 15};
    private final int RADIOACTIVE_STATEMENT_ID = 16;
    private final int CUSTOM_STATEMENT_ID = 17;

    public MaterialHazardController(
            HazardService hazardService,
            MaterialType materialType,
            boolean isEditable,
            Map<HazardType, String> hazards) {
        this(hazardService, materialType, isEditable, hazards, JsfMessagePresenter.getInstance());

    }

    public MaterialHazardController(
            HazardService hazardService,
            MaterialType materialType,
            boolean isEditable,
            Map<HazardType, String> hazards,
            MessagePresenter presenter) {
        this.messagePresenter = presenter;
        this.hazardService = hazardService;
        this.materialType = materialType;
        selectedHazards = new HazardType[hazards.keySet().size()];
        this.selectedHazards = new ArrayList<>(hazards.keySet()).toArray(selectedHazards);
        this.editable = isEditable;
        this.radioctive = hazards.keySet().contains(hazardService.getHazardById(RADIOACTIVE_STATEMENT_ID));
        this.customText = hazards.get(hazardService.getHazardById(CUSTOM_STATEMENT_ID));
        this.hStatements = hazards.get(hazardService.getHazardById(H_STATEMENT_ID));
        this.pStatements = hazards.get(hazardService.getHazardById(P_STATEMENT_ID));
        for (int i = 0; i < 5; i++) {
            possibleBioSavetyLevels.add(getLocalizedBioSavetyLabel(i));
        }
        this.bioSavetyLevel = possibleBioSavetyLevels.get(0);

        for (int i = 0; i < BSL_IDS.length; i++) {
            if (hazards.keySet().contains(hazardService.getHazardById(BSL_IDS[i]))) {
                this.bioSavetyLevel = possibleBioSavetyLevels.get(i + 1);
            }
        }

    }

    /**
     * Checks if the given category can be choosen at the current material
     *
     * @param category
     * @return
     */
    public boolean isHazardCategoryRendered(String category) {
        boolean rendered = hazardService.getAllowedCatsOf(materialType).contains(HazardType.Category.valueOf(category));
        return rendered;
    }

    public boolean isHazardEditable() {
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

    /**
     * Create the Map which is used in the material model.
     *
     * @return
     */
    public Map<HazardType, String> createHazardMap() {
        Map<HazardType, String> hazards = new HashMap<>();
        for (int i = 0; i < selectedHazards.length; i++) {
            hazards.put(selectedHazards[i], null);
        }
        processStatement(hazards, hStatements, H_STATEMENT_ID);
        processStatement(hazards, pStatements, P_STATEMENT_ID);
        processStatement(hazards, customText, CUSTOM_STATEMENT_ID);
        processBioSavetyLevels(hazards);

        if (radioctive) {
            hazards.put(hazardService.getHazardById(RADIOACTIVE_STATEMENT_ID), null);
        } else {
            hazards.remove(hazardService.getHazardById(RADIOACTIVE_STATEMENT_ID));
        }

        return hazards;
    }

    public String getLocalizedName(HazardType h) {
        return messagePresenter.presentMessage("hazard_" + h.getName());
    }

    public String getLocalizedRadioactiveLabel() {
        return getLocalizedName(hazardService.getHazardById(16));
    }

    public String getLocalizedCustomLabel() {
        return getLocalizedName(hazardService.getHazardById(17));
    }

    public String getLocalizedStatements() {
        return messagePresenter.presentMessage("hazard_Statements");
    }

    public String getLocalizedBioSavetyLabel(int level) {
        return messagePresenter.presentMessage("hazard_S" + level);
    }

    /**
     * An array is necessary, list is not working
     *
     * @return
     */
    public HazardType[] getSelectedHazards() {
        return selectedHazards;
    }

    public void setSelectedHazards(HazardType[] selectedHazards) {
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

    /**
     * Checks if a statement is not blank and puts it in the hazard list. If not
     * blank, removes it if its in.
     *
     * @param hazards
     * @param statement
     * @param id
     */
    private void processStatement(Map<HazardType, String> hazards, String statement, int id) {
        if (statement != null && !statement.trim().isEmpty()) {
            hazards.put(hazardService.getHazardById(id), statement);
        } else {
            hazards.remove(hazardService.getHazardById(id));
        }
    }

    private void processBioSavetyLevels(Map<HazardType, String> hazards) {
        //Put selected savety level in hazards
        for (int i = 0; i < BSL_IDS.length; i++) {
            if (bioSavetyLevel.equals(possibleBioSavetyLevels.get(i+1))) {
                hazards.put(hazardService.getHazardById(BSL_IDS[i]), null);
            }
        }
    }

    public String getBioSavetyLevel() {
        return bioSavetyLevel;
    }

    public void setBioSavetyLevel(String bioSavetyLevel) {
        this.bioSavetyLevel = bioSavetyLevel;
    }

    public List<String> getPossibleBioSavetyLevels() {
        return possibleBioSavetyLevels;
    }

    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

}
