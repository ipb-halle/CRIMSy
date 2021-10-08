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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.biomaterial.BioMaterialDifference;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.bean.StorageInformationBuilder;
import de.ipb_halle.lbac.material.composition.CompositionDifference;
import de.ipb_halle.lbac.material.composition.CompositionHistoryController;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.project.ProjectBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class provides methods to apply one ore more differences bewetween two
 * states of a material.
 *
 * @author fmauz
 */
public class HistoryOperation implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected ProjectBean projectBean;
    protected MaterialEditState materialEditState;
    protected MaterialNameBean materialNameBean;
    protected MaterialIndexBean indexBean;
    protected StructureInformation structureInfos;
    protected MaterialCompositionBean compositionBean;
    protected StorageInformationBuilder storageInformationBuilder;
    protected TaxonomySelectionController taxonomySelectionController;
    protected List<HazardType> possibleHazards = new ArrayList<>();
    protected MaterialBean materialBean;

    /**
     * Initialises the functionality by neccessary services and the history
     * (collection of all differences at some timepoints) in the
     * materialEditState variable.
     *
     * @param materialEditState
     * @param projectBean
     * @param nameBean
     * @param indexBean
     * @param structureInfos
     * @param storageInformationBuilder
     * @param taxonomySelectionController
     * @param possibleHazards
     * @param compositionBean
     */
    public HistoryOperation(
            MaterialBean materialBean,
            MaterialEditState materialEditState,
            ProjectBean projectBean,
            MaterialNameBean nameBean,
            MaterialIndexBean indexBean,
            StructureInformation structureInfos,
            StorageInformationBuilder storageInformationBuilder,
            TaxonomySelectionController taxonomySelectionController,
            List<HazardType> possibleHazards,
            MaterialCompositionBean compositionBean) {
        this.projectBean = projectBean;
        this.materialEditState = materialEditState;
        this.materialNameBean = nameBean;
        this.indexBean = indexBean;
        this.structureInfos = structureInfos;
        this.storageInformationBuilder = storageInformationBuilder;
        this.taxonomySelectionController = taxonomySelectionController;
        this.possibleHazards = possibleHazards;
        this.compositionBean = compositionBean;
        this.materialBean = materialBean;
    }

    /**
     * Based on the current timepoint applies all differences of the next
     * timepoint and set t as the current timepoint.
     *
     */
    public void applyNextPositiveDifference() {
        materialEditState.changeVersionDateToNext(materialEditState.getCurrentVersiondate());
        applyPositiveOverview();
        applyPositiveStructure();
        applyPositiveIndices();
        applyPositiveHazards();
        applyPositiveStorage();
        applyPositiveTaxonomy();
        applyPositiveComposition();
    }

    /**
     * Based on the current timepoint applies all differences(negative) of the
     * last timepoint and set t as the current timepoint.
     *
     */
    public void applyNextNegativeDifference() {
        applyNegativeOverview();
        applyNegativeStructure();
        applyNegativeIndices();
        applyNegativeHazards();
        applyNegativeStorage();
        applyNegativeTaxonomy();
        applyNegativeComposition();
        materialEditState.changeVersionDateToPrevious(materialEditState.getCurrentVersiondate());
    }

    private void applyNegativeComposition() {
        CompositionDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(CompositionDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {

            CompositionHistoryController controller = new CompositionHistoryController(materialBean);
            controller.applyNegativeDifference(diff);
        }
    }

    private void applyPositiveComposition() {
        CompositionDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(CompositionDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            CompositionHistoryController controller = new CompositionHistoryController(materialBean);
            controller.applyPositiveDifference(diff);
        }
    }

    public void applyPositiveStorage() {
        MaterialStorageDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStorageDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
                storageInformationBuilder.setRemarks(diff.getDescriptionNew());
            }
            if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
                if (diff.getStorageclassNew() != null) {
                    storageInformationBuilder.setChoosenStorageClass(storageInformationBuilder.getStorageClassById(diff.getStorageclassNew()));
                    storageInformationBuilder.setStorageClassActivated(true);

                } else {
                    storageInformationBuilder.setStorageClassActivated(false);
                }
            }
            for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
                if (diff.getStorageConditionsOld().get(i) == null) {
                    storageInformationBuilder.addStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));

                } else {
                    storageInformationBuilder.removeStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));
                }
            }
        }
    }

    public void applyNegativeStorage() {
        MaterialStorageDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStorageDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
                storageInformationBuilder.setRemarks(diff.getDescriptionOld());
            }
            if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
                if (diff.getStorageclassOld() != null) {
                    storageInformationBuilder.setChoosenStorageClass(storageInformationBuilder.getStorageClassById(diff.getStorageclassOld()));
                    storageInformationBuilder.setStorageClassActivated(true);
                } else {
                    storageInformationBuilder.setStorageClassActivated(false);
                }
            }
            for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
                if (diff.getStorageConditionsNew().get(i) == null) {
                    storageInformationBuilder.addStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));

                } else {
                    storageInformationBuilder.removeStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));
                }
            }
        }
    }

    /**
     * @return true if the currently shown material state is the original
     * version
     */
    public boolean isOriginalMaterial() {
        return materialEditState.getCurrentVersiondate() == null;
    }

    protected void applyPositiveHazards() {
        MaterialHazardDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialHazardDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            for (int i = 0; i < diff.getEntries(); i++) {
                Integer newTypeId = diff.getTypeIdsNew().get(i);
                Integer oldTypeId = diff.getTypeIdsOld().get(i);
                String newValue = diff.getRemarksNew().get(i);
                if (newTypeId != null) {
                    materialEditState.getHazardController().addHazardType(getHazardById(newTypeId), newValue);
                } else {
                    materialEditState.getHazardController().removeHazard(getHazardById(oldTypeId));
                }
            }
        }
    }

    protected void applyNegativeHazards() {
        MaterialHazardDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialHazardDifference.class, materialEditState.getCurrentVersiondate());

        if (diff != null) {
            materialEditState.getHazardController().setEditable(false);
            for (int i = 0; i < diff.getEntries(); i++) {
                Integer newTypeId = diff.getTypeIdsNew().get(i);
                Integer oldTypeId = diff.getTypeIdsOld().get(i);
                String oldValue = diff.getRemarksOld().get(i);
                if (oldTypeId != null) {
                    materialEditState.getHazardController().addHazardType(getHazardById(oldTypeId), oldValue);
                } else {
                    materialEditState.getHazardController().removeHazard(getHazardById(newTypeId));
                }
            }
        }
    }

    protected void applyNegativeOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (diff.getProjectIdOld() != null) {
                materialEditState.setCurrentProject(projectBean.getProjectService().loadProjectById(diff.getProjectIdOld()));
            }
        }
    }

    protected void applyPositiveOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (diff.getProjectIdOld() != null) {
                materialEditState.setCurrentProject(projectBean.getProjectService().loadProjectById(diff.getProjectIdNew()));
            }
        }
    }

    protected void applyNegativeStructure() {
        MaterialStructureDifference structureDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStructureDifference.class, materialEditState.getCurrentVersiondate());
        if (structureDiff != null) {
            if (!Objects.equals(structureDiff.getMoleculeId_old(), structureDiff.getMoleculeId_new())) {
                if (structureDiff.getMoleculeId_old() == null && structureDiff.getMoleculeId_new() != null) {
                    structureInfos.setStructureModel(null);
                } else {
                    structureInfos.setStructureModel(structureDiff.getMoleculeId_old().getStructureModel());
                }
            }
            if (!Objects.equals(structureDiff.getMolarMass_old(), structureDiff.getMolarMass_new())) {
                structureInfos.setAverageMolarMass(structureDiff.getMolarMass_old());
            }
            if (!Objects.equals(structureDiff.getExactMolarMass_old(), structureDiff.getExactMolarMass_new())) {
                structureInfos.setExactMolarMass(structureDiff.getExactMolarMass_old());
            }
            if (!Objects.equals(structureDiff.getSumFormula_old(), structureDiff.getSumFormula_new())) {
                structureInfos.setSumFormula(structureDiff.getSumFormula_old());
            }
        }
    }

    protected void applyPositiveStructure() {
        MaterialStructureDifference structureDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStructureDifference.class, materialEditState.getCurrentVersiondate());
        if (structureDiff != null) {
            if (!Objects.equals(structureDiff.getMoleculeId_old(), structureDiff.getMoleculeId_new())) {
                if (structureDiff.getMoleculeId_old() != null && structureDiff.getMoleculeId_new() == null) {
                    structureInfos.setStructureModel(null);
                } else {
                    structureInfos.setStructureModel(structureDiff.getMoleculeId_new().getStructureModel());
                }
            }
            if (!Objects.equals(structureDiff.getMolarMass_old(), structureDiff.getMolarMass_new())) {
                structureInfos.setAverageMolarMass(structureDiff.getMolarMass_new());
            }
            if (!Objects.equals(structureDiff.getExactMolarMass_old(), structureDiff.getExactMolarMass_new())) {
                structureInfos.setExactMolarMass(structureDiff.getExactMolarMass_new());
            }
            if (!Objects.equals(structureDiff.getSumFormula_old(), structureDiff.getSumFormula_new())) {
                structureInfos.setSumFormula(structureDiff.getSumFormula_new());
            }
        }
    }

    protected void applyPositiveIndices() {
        MaterialIndexDifference indexDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, materialEditState.getCurrentVersiondate());
        if (indexDiff != null) {
            for (int i = 0; i < indexDiff.getEntries(); i++) {
                if (indexDiff.getTypeId().get(i) == 1) {
                    int currentSize = materialNameBean.getNames().size();
                    for (int j = currentSize - 1; j >= 0; j--) {
                        MaterialName mn = materialNameBean.getNames().get(j);
                        String newLan = indexDiff.getLanguageNew().get(i);
                        String newValue = indexDiff.getValuesNew().get(i);
                        Integer newRank = indexDiff.getRankNew().get(i);
                        boolean rankEqual = mn.getRank().equals(indexDiff.getRankOld().get(i));
                        boolean valueEqual = mn.getValue().equals(indexDiff.getValuesOld().get(i));
                        boolean lanEqual = mn.getLanguage().equals(indexDiff.getLanguageOld().get(i));
                        if (rankEqual && valueEqual && lanEqual) {
                            if (newLan == null) {
                                materialNameBean.getNames().remove(j);
                            } else {
                                mn.setLanguage(newLan);
                                mn.setValue(newValue);
                                mn.setRank(newRank);
                            }
                        }
                        if (indexDiff.getRankOld().get(i) == null && indexDiff.getValuesOld().get(i) == null && indexDiff.getLanguageOld().get(i) == null) {
                            materialNameBean.getNames().add(new MaterialName(newValue, newLan, newRank));
                            break;
                        }
                    }
                } else {
                    if (indexDiff.getValuesNew().get(i) == null) {
                        IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), indexBean.getIndices());
                        if (index != null) {
                            indexBean.getIndices().remove(index);
                        }
                    } else if (indexDiff.getValuesOld().get(i) == null) {
                        indexBean.getIndices().add(
                                new IndexEntry(
                                        indexDiff.getTypeId().get(i),
                                        indexDiff.getValuesNew().get(i),
                                        null));
                    } else {
                        IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), indexBean.getIndices());
                        if (index != null) {
                            index.setValue(indexDiff.getValuesNew().get(i));
                        }
                    }
                }
            }
            materialNameBean.reorderNamesByRank();
        }
    }

    protected void applyNegativeIndices() {
        MaterialIndexDifference indexDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, materialEditState.getCurrentVersiondate());
        if (indexDiff != null) {
            int originalNameSize = materialNameBean.getNames().size();
            for (int i = 0; i < indexDiff.getEntries(); i++) {
                if (indexDiff.getTypeId().get(i) == 1) {

                    for (int j = materialNameBean.getNames().size() - 1; j >= 0; j--) {
                        MaterialName mn = materialNameBean.getNames().get(j);
                        String oldLan = indexDiff.getLanguageOld().get(i);
                        String oldValue = indexDiff.getValuesOld().get(i);
                        Integer oldRank = indexDiff.getRankOld().get(i);
                        String newLan = indexDiff.getLanguageNew().get(i);
                        String newValue = indexDiff.getValuesNew().get(i);
                        Integer newRank = indexDiff.getRankNew().get(i);

                        boolean rankEqual = mn.getRank().equals(newRank);
                        boolean valueEqual = mn.getValue().equals(newValue);
                        boolean lanEqual = mn.getLanguage().equals(newLan);
                        if (rankEqual && valueEqual && lanEqual) {
                            if (oldLan == null) {
                                materialNameBean.getNames().remove(j);
                            } else {
                                mn.setLanguage(oldLan);
                                mn.setValue(oldValue);
                                mn.setRank(oldRank);
                            }
                        }
                        if (newLan == null && newValue == null && newRank == null) {
                            materialNameBean.getNames().add(new MaterialName(oldValue, oldLan, oldRank));
                            break;
                        }
                    }
                } else {
                    if (indexDiff.getValuesOld().get(i) == null) {
                        IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), indexBean.getIndices());
                        if (index != null) {
                            indexBean.getIndices().remove(index);
                        }
                    } else if (indexDiff.getValuesNew().get(i) == null) {
                        indexBean.getIndices().add(
                                new IndexEntry(
                                        indexDiff.getTypeId().get(i),
                                        indexDiff.getValuesOld().get(i),
                                        null));
                    } else {
                        IndexEntry index = getIndexByTypeId(indexDiff.getTypeId().get(i), indexBean.getIndices());
                        if (index != null) {
                            index.setValue(indexDiff.getValuesOld().get(i));
                        }
                    }
                }
            }
            materialNameBean.reorderNamesByRank();
        }
    }

    protected IndexEntry getIndexByTypeId(int typeId, List<IndexEntry> indices) {
        if (indices == null) {
            return null;
        }
        for (IndexEntry ie : indices) {
            if (ie.getTypeId() == typeId) {
                return ie;
            }
        }
        return null;
    }

    public void applyPositiveTaxonomy() {
        BioMaterialDifference bioMatDiff = materialEditState
                .getMaterialBeforeEdit()
                .getHistory()
                .getDifferenceOfTypeAtDate(
                        BioMaterialDifference.class,
                        materialEditState.getCurrentVersiondate());

        if (bioMatDiff != null && bioMatDiff.getTaxonomyid_new() != null) {
            taxonomySelectionController.setSelectedTaxonomyById(bioMatDiff.getTaxonomyid_new());
        }
        if (taxonomySelectionController != null) {
            if (!materialEditState.isMostRecentVersion()) {
                taxonomySelectionController.deactivateTree();
            } else {
                taxonomySelectionController.activateTree();
            }
        }

    }

    public void applyNegativeTaxonomy() {
        BioMaterialDifference bioMatDiff = materialEditState
                .getMaterialBeforeEdit()
                .getHistory()
                .getDifferenceOfTypeAtDate(
                        BioMaterialDifference.class,
                        materialEditState.getCurrentVersiondate());

        if (bioMatDiff != null && bioMatDiff.getTaxonomyid_old() != null) {
            taxonomySelectionController.setSelectedTaxonomyById(bioMatDiff.getTaxonomyid_old());
        }
        if (taxonomySelectionController != null) {
            taxonomySelectionController.deactivateTree();
        }
    }

    private HazardType getHazardById(int id) {
        for (HazardType hazard : possibleHazards) {
            if (hazard.getId() == id) {
                return hazard;
            }
        }
        return null;
    }

}
