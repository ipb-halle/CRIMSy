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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages the interactions with the frontend and provides a method for building
 * the "backend" ready datastructure for further actions (like saving etc).
 *
 * @author fmauz
 */
public class MaterialHazardBuilder {

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
    private boolean gmo;

    private final int H_STATEMENT_ID = 10;
    private final int P_STATEMENT_ID = 11;
    private final int[] BSL_IDS = new int[]{12, 13, 14, 15};
    private final int RADIOACTIVE_STATEMENT_ID = 16;
    private final int CUSTOM_STATEMENT_ID = 17;
    private final int GMO_STATEMENT_ID = 20;

    public MaterialHazardBuilder(
            HazardService hazardService,
            MaterialType materialType,
            boolean isEditable,
            Map<HazardType, String> hazards) {
        this(hazardService, materialType, isEditable, hazards, JsfMessagePresenter.getInstance());

    }

    /**
     *
     * @param hazardService
     * @param materialType
     * @param isEditable
     * @param hazards
     * @param presenter
     */
    public MaterialHazardBuilder(
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
        this.gmo = hazards.keySet().contains(hazardService.getHazardById(GMO_STATEMENT_ID));
        this.customText = hazards.get(hazardService.getHazardById(CUSTOM_STATEMENT_ID));
        this.hStatements = hazards.get(hazardService.getHazardById(H_STATEMENT_ID));
        this.pStatements = hazards.get(hazardService.getHazardById(P_STATEMENT_ID));
        for (int i = 1; i < 5; i++) {
            possibleBioSavetyLevels.add(getLocalizedBioSavetyLabel(i));
        }
        possibleBioSavetyLevels.add(getLocalizedBioSavetyLabel(0));

        this.bioSavetyLevel = possibleBioSavetyLevels.get(possibleBioSavetyLevels.size()-1);

        for (int i = 0; i < BSL_IDS.length; i++) {
            if (hazards.keySet().contains(hazardService.getHazardById(BSL_IDS[i]))) {
                this.bioSavetyLevel = possibleBioSavetyLevels.get(i + 1);
            }
        }

    }

    /**
     * Removes a Hazard. If the hazard is a h- or p-statement, radioactivity,
     * gmo or a custom statement the corresponding varable will be changed.
     *
     * @param hazard
     */
    public void removeHazard(HazardType hazard) {
        for (int i = 0; i < BSL_IDS.length; i++) {
            if (hazard.getId() == BSL_IDS[i]) {
                bioSavetyLevel = possibleBioSavetyLevels.get(0);
            }
        }

        hStatements = hazard.getId() == H_STATEMENT_ID ? null : hStatements;
        pStatements = hazard.getId() == P_STATEMENT_ID ? null : pStatements;
        customText = hazard.getId() == CUSTOM_STATEMENT_ID ? null : customText;
        radioctive = hazard.getId() == RADIOACTIVE_STATEMENT_ID ? false : radioctive;
        gmo = hazard.getId() == GMO_STATEMENT_ID ? false : gmo;

        ArrayList<HazardType> tmpArray = new ArrayList<>(Arrays.asList(selectedHazards));
        tmpArray.remove(hazard);
        selectedHazards = tmpArray.stream().toArray(HazardType[]::new);
    }

    /**
     * Adds a Hazard. If the hazard is a h- or p-statement, radioactivity, gmo
     * or a custom statement the corresponding varable will be changed.
     *
     * @param hazard
     */
    public void addHazardType(HazardType hazard, String remark) {
        for (int i = 0; i < BSL_IDS.length; i++) {
            if (hazard.getId() == BSL_IDS[i]) {
                bioSavetyLevel = possibleBioSavetyLevels.get(i + 1);
            }
        }
        if (hazard.getId() == H_STATEMENT_ID) {
            hStatements = remark;
        } else if (hazard.getId() == P_STATEMENT_ID) {
            pStatements = remark;
        } else if (hazard.getId() == CUSTOM_STATEMENT_ID) {
            customText = remark;
        } else if (hazard.getId() == RADIOACTIVE_STATEMENT_ID) {
            radioctive = true;
        } else if (hazard.getId() == GMO_STATEMENT_ID) {
            gmo = true;
        } else {
            ArrayList<HazardType> newHazards = Arrays.stream(selectedHazards).collect(Collectors
                    .toCollection(ArrayList::new));
            newHazards.add(hazard);
            selectedHazards = newHazards.stream().toArray(HazardType[]::new);
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

    public void setEditable(boolean editable) {
        this.editable = editable;
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
    public Map<HazardType, String> buildHazardsMap() {
        Map<HazardType, String> hazards = new HashMap<>();
        for (int i = 0; i < selectedHazards.length; i++) {
            hazards.put(selectedHazards[i], null);
        }
        processStatement(hazards, hStatements, H_STATEMENT_ID);
        processStatement(hazards, pStatements, P_STATEMENT_ID);
        processStatement(hazards, customText, CUSTOM_STATEMENT_ID);
        processBioSavetyLevels(hazards);

        if (gmo) {
            hazards.put(hazardService.getHazardById(GMO_STATEMENT_ID), null);
        } else {
            hazards.remove(hazardService.getHazardById(GMO_STATEMENT_ID), null);
        }
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
        //Remove previous bio savety levels to avoid multiple entries
        for (int i = 0; i < BSL_IDS.length; i++) {
            hazards.remove(hazardService.getHazardById(BSL_IDS[i]));
        }
        //Put selected savety level in hazards
        for (int i = 0; i < BSL_IDS.length; i++) {
            if (bioSavetyLevel.equals(possibleBioSavetyLevels.get(i))) {
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

    /**
     * Returns a biohazard image if riskgroup 2,3 or 4 is choosen. Returns a
     * empty image else
     *
     * @param index
     * @return
     */
    public String getImageLocationOfBls(Integer index) {
        if (index == 1 || index == 2 || index == 3) {
            return String.format(imageString, "BIOHAZARD");
        }

        return String.format(imageString, "Empty");
    }

    public boolean isGmo() {
        return gmo;
    }

    public void setGmo(boolean gmo) {
        this.gmo = gmo;
    }

}
