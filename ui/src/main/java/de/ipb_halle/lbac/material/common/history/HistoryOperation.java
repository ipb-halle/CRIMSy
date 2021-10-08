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
     * @param materialBean
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
        applyPositiveOverview(); //Migrated
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
        applyNegativeOverview();  //Migrated
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
            diff.createHistoryController(materialBean).applyNegativeDifference(diff);
        }
    }

    private void applyPositiveComposition() {
        CompositionDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(CompositionDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            diff.createHistoryController(materialBean).applyPositiveDifference(diff);
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
            diff.createHistoryController(materialBean).applyPositiveDifference(diff);
        }
    }

    protected void applyNegativeHazards() {
        MaterialHazardDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialHazardDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            diff.createHistoryController(materialBean).applyNegativeDifference(diff);
        }
    }

    protected void applyNegativeOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            diff.createHistoryController(materialBean).applyNegativeDifference(diff);
        }
    }

    protected void applyPositiveOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            diff.createHistoryController(materialBean).applyPositiveDifference(diff);
        }
    }

    protected void applyNegativeStructure() {
        MaterialStructureDifference structureDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStructureDifference.class, materialEditState.getCurrentVersiondate());
        if (structureDiff != null) {
            structureDiff.createHistoryController(materialBean).applyNegativeDifference(structureDiff);
        }
    }

    protected void applyPositiveStructure() {
        MaterialStructureDifference structureDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStructureDifference.class, materialEditState.getCurrentVersiondate());
        if (structureDiff != null) {
            structureDiff.createHistoryController(materialBean).applyPositiveDifference(structureDiff);
        }
    }

    protected void applyPositiveIndices() {
        MaterialIndexDifference indexDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, materialEditState.getCurrentVersiondate());
        if (indexDiff != null) {
            indexDiff.createHistoryController(materialBean).applyPositiveDifference(indexDiff);
        }
    }

    protected void applyNegativeIndices() {
        MaterialIndexDifference indexDiff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialIndexDifference.class, materialEditState.getCurrentVersiondate());
        if (indexDiff != null) {
            indexDiff.createHistoryController(materialBean).applyNegativeDifference(indexDiff);
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

}
