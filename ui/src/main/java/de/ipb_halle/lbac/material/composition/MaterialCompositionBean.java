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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.bean.MaterialBean.Mode;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.search.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.search.bean.SearchMode;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchMaskValuesHolder;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.units.Quality;
import de.ipb_halle.lbac.util.units.Unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toCollection;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.TabChangeEvent;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialCompositionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    private List<Material> foundMaterials = new ArrayList<>();
    private final List<Concentration> concentrationsInComposition = new ArrayList<>();
    private MaterialType choosenMaterialType;
    private int MAX_RESULTS = 25;
    private String materialName = "";
    private CompositionType choosenCompositionType = CompositionType.EXTRACT;
    private String searchMolecule = "";
    private Mode mode;
    private CompositionHistoryController historyController;
    private int activeIndex = 0;
    private SequenceSearchMaskValuesHolder sequenceSearchMaskValuesHolder;

    @Inject
    private MaterialService materialService;

    @Inject
    private SequenceSearchService sequenceSearchService;

    @Inject
    private transient MessagePresenter presenter;

    @Inject
    private UserBean userBean;

    public MaterialCompositionBean() {
    }

    public MaterialCompositionBean(MaterialService materialService, MessagePresenter presenter, UserBean userBean) {
        this.materialService = materialService;
        this.presenter = presenter;
        this.userBean = userBean;

        init();
    }

    @PostConstruct
    public void init() {
        sequenceSearchMaskValuesHolder = new SequenceSearchMaskValuesHolder(presenter);
    }

    public void startCompositionEdit(MaterialComposition comp) {
        clearBean();
        mode = Mode.EDIT;
        this.concentrationsInComposition.addAll(comp.getComponents());
        this.choosenCompositionType = comp.getCompositionType();
    }

    public void clearBean() {
        this.mode = Mode.CREATE;
        this.materialName = "";
        this.choosenCompositionType = CompositionType.EXTRACT;
        this.choosenMaterialType = MaterialType.STRUCTURE;
        this.concentrationsInComposition.clear();
        this.foundMaterials.clear();
        this.searchMolecule = "";
    }

    public String getLocalizedCompositionType(CompositionType type) {
        return presenter.presentMessage("materialComposition_" + type);
    }

    public List<CompositionType> getCompositionTypes() {
        return Arrays.asList(CompositionType.values());
    }

    public CompositionType getChoosenType() {
        return choosenCompositionType;
    }

    public String getSearchMolecule() {
        return searchMolecule;
    }

    public void setSearchMolecule(String searchMolecule) {
        this.searchMolecule = searchMolecule;
    }

    public void setChoosenType(CompositionType choosenType) {
        if (choosenType != choosenCompositionType) {
            concentrationsInComposition.clear();
            foundMaterials.clear();
        }
        this.choosenCompositionType = choosenType;
        if (!choosenType.getAllowedTypes().contains(choosenMaterialType)) {
            choosenMaterialType = choosenType.getAllowedTypes().get(0);
            activeIndex = calculateIndexOfMaterialType(choosenMaterialType);
            changeSelectedMaterialType(getLocalizedTabTitle(choosenMaterialType.toString()));
        }
    }

    private int calculateIndexOfMaterialType(MaterialType type) {
        if (type == MaterialType.STRUCTURE) {
            return 0;
        }
        if (type == MaterialType.SEQUENCE) {
            return 1;
        }
        if (type == MaterialType.BIOMATERIAL) {
            return 2;
        }
        return 0;
    }

    private void changeSelectedMaterialType(String typeName) {
        for (MaterialType t : MaterialType.values()) {
            String localizedMateralName = getLocalizedTabTitle(t.toString());
            if (typeName.equals(localizedMateralName)) {
                choosenMaterialType = t;
            }
        }
    }

    public void onTabChange(TabChangeEvent event) {

        changeSelectedMaterialType(event.getTab().getTitle());
    }

    public String getLocalizedTabTitle(String materialType) {
        return presenter.presentMessage("search_category_" + materialType);
    }

    public void actionStartSearch() {
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(userBean.getCurrentAccount(), 0, MAX_RESULTS);
        requestBuilder.addMaterialType(choosenMaterialType);
        if (materialName != null && !materialName.trim().isEmpty()) {
            requestBuilder.setMaterialName(materialName);
        }
        if (choosenMaterialType == MaterialType.STRUCTURE) {
            Molecule mol = new Molecule(searchMolecule, -1);
            if (!mol.isEmptyMolecule()) {
                requestBuilder.setStructure(searchMolecule);
            }
        }

        if (choosenMaterialType == MaterialType.SEQUENCE) {
            sequenceSearch(requestBuilder);
        } else {
            SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());
            foundMaterials = result.getAllFoundObjects(choosenMaterialType.getClassOfDto(), result.getNode());
        }
    }

    private void sequenceSearch(MaterialSearchRequestBuilder requestBuilder) {
        SearchMode searchMode = sequenceSearchMaskValuesHolder.getSearchMode();
        requestBuilder.setSequenceInformation(sequenceSearchMaskValuesHolder.getQuery(),
                searchMode.getLibrarySequenceType(), searchMode.getQuerySequenceType(),
                sequenceSearchMaskValuesHolder.getTranslationTable().getId());
        SearchResult searchResultFromService = sequenceSearchService.searchSequences(requestBuilder.build());

        boolean hasErrors = !searchResultFromService.getErrorMessages().isEmpty();
        if (hasErrors) {
            presenter.error("sequenceSearch_error");
            return;
        }

        List<SequenceAlignment> searchResults = searchResultFromService
                .getAllFoundObjectsAsSearchable(SequenceAlignment.class);
        searchResults.sort(
                Comparator.comparing(seqAlignment -> seqAlignment.getAlignmentInformation().getExpectationValue()));

        foundMaterials.clear();
        addDistinctSequencesToFoundMaterials(searchResults);
    }

    /*
     * Problem: searchResults can contain the same Sequence material more than once.
     * One the other hand, we would like to have only distinct materials in
     * foundMaterials.
     * 
     * Solution: Use the Material ID to check if the Sequence was already added.
     */
    private void addDistinctSequencesToFoundMaterials(List<SequenceAlignment> searchResults) {
        Set<Integer> alreadyAddedMaterialIds = new HashSet<>();
        for (SequenceAlignment alignment : searchResults) {
            Sequence foundSequence = alignment.getFoundSequence();
            int id = foundSequence.getId();
            if (!alreadyAddedMaterialIds.contains(id)) {
                alreadyAddedMaterialIds.add(id);
                foundMaterials.add(foundSequence);
            }
        }
    }

    public List<Material> getMaterialsThatCanBeAdded() {
        return foundMaterials.stream()
                .filter(m -> choosenCompositionType.getAllowedTypes().contains(m.getType()))
                .filter((m -> !isMaterialAlreadyInComposition(m.getId())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isMaterialAlreadyInComposition(int materialIdToLookFor) {
        for (Concentration concentrationAlreadyIn : concentrationsInComposition) {
            if (concentrationAlreadyIn.isSameMaterial(materialIdToLookFor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a material to the composition if the material is not already in it
     * and its type is allowed by the compositionType
     *
     * @param materialToAdd
     */
    public void actionAddMaterialToComposition(Material materialToAdd) {
        if (!isMaterialAlreadyInComposition(materialToAdd.getId())
                && choosenCompositionType.getAllowedTypes().contains(materialToAdd.getType())) {
            concentrationsInComposition.add(new Concentration(materialToAdd));
        }
    }

    public void actionRemoveConcentrationFromComposition(Concentration concentrationToRemove) {
        concentrationsInComposition.remove(concentrationToRemove);
        ArrayList<Integer> ids = foundMaterials.stream().map(mat -> mat.getId()).collect(toCollection(ArrayList::new));
        if (!ids.contains(concentrationToRemove.getMaterialId())) {
            foundMaterials.add(concentrationToRemove.getMaterial());
        }
    }

    /**
     * Switches the current material type. If null , an invalid or not allowed
     * type is passed, no change is made.
     *
     * @param type String value of @see MaterialType
     */
    public void actionSwitchMaterialType(String type) {
        concentrationsInComposition.clear();
        foundMaterials.clear();
        MaterialType t;
        if (type == null) {
            logger.error("No null value allowed as argument");
            return;
        }
        try {
            t = MaterialType.valueOf(type);
        } catch (IllegalArgumentException e) {
            logger.error(String.format("%s does not match a enum value", type));
            return;
        }
        if (choosenCompositionType.getAllowedTypes().contains(t)) {
            this.choosenMaterialType = t;
        } else {
            logger.error(String.format("CompositionType %s does not allow material type %s", choosenMaterialType, type));
        }
    }

    public MaterialType getChoosenMaterialType() {
        return choosenMaterialType;
    }

    public List<Material> getFoundMaterials() {
        return foundMaterials;
    }

    public boolean isMaterialTypePanelDisabled(String materialTypeString) {
        MaterialType type;
        try {
            type = MaterialType.valueOf(materialTypeString);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return !choosenCompositionType.getAllowedTypes().contains(type);
    }

    public List<Concentration> getConcentrationsInComposition() {
        return concentrationsInComposition;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getLocalizedMaterialType(Concentration conc) {
        return presenter.
                presentMessage("search_category_" + conc.getMaterialType().toString());

    }

    /**
     * Returns an empty String if the id is -1 (Inaccessible material).
     * Otherwise return the id of the material
     *
     * @param c
     * @return
     */
    public String getFormatedIdOfConcentration(Concentration c) {
        return c.getMaterialId() == -1 ? "" : String.format("%d", c.getMaterialId());
    }

    public boolean isCompositionTypeEditable() {
        return mode == Mode.CREATE;
    }

    protected Concentration getConcentrationWithMaterial(int materialid) {
        for (Concentration conc : concentrationsInComposition) {
            if (conc.isSameMaterial(materialid)) {
                return conc;
            }
        }
        return null;
    }

    public CompositionHistoryController getHistoryController() {
        return historyController;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public List<Unit> getAvailableAmountUnits() {
        return Unit.getVisibleUnitsOfQuality(
                Quality.AMOUNT_OF_SUBSTANCE,
                Quality.PERCENT_CONCENTRATION,
                Quality.MOLAR_MASS,
                Quality.VOLUME);
    }

    public SequenceSearchMaskValuesHolder getSequenceSearchMaskValuesHolder() {
        return sequenceSearchMaskValuesHolder;
    }
}
