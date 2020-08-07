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

import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.project.ProjectBean;
import java.io.Serializable;
import java.text.SimpleDateFormat;
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
public class HistoryOperation  implements Serializable{

    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected ProjectBean projectBean;
    protected MaterialEditState materialEditState;
    protected MaterialNameBean materialNameBean;
    protected MaterialIndexBean indexBean;
    protected StructureInformation structureInfos;
    protected StorageClassInformation storageInformation;

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
     * @param storageClassInformation
     */
    public HistoryOperation(
            MaterialEditState materialEditState,
            ProjectBean projectBean,
            MaterialNameBean nameBean,
            MaterialIndexBean indexBean,
            StructureInformation structureInfos,
            StorageClassInformation storageClassInformation) {
        this.projectBean = projectBean;
        this.materialEditState = materialEditState;
        this.materialNameBean = nameBean;
        this.indexBean = indexBean;
        this.structureInfos = structureInfos;
        this.storageInformation = storageClassInformation;
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
        materialEditState.changeVersionDateToPrevious(materialEditState.getCurrentVersiondate());

    }

    public void applyPositiveStorage() {
        MaterialStorageDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStorageDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
                storageInformation.setRemarks(diff.getDescriptionNew());
            }
            if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
                storageInformation.setStorageClass(new StorageClass(diff.getStorageclassNew(), ""));
            }
            for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
                if (diff.getStorageConditionsOld().get(i) == null) {
                    storageInformation.getStorageConditions().add(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));
                } else {
                    storageInformation.getStorageConditions().remove(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));
                }
            }
        }
    }

    public void applyNegativeStorage() {
        MaterialStorageDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialStorageDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
                storageInformation.setRemarks(diff.getDescriptionOld());
            }
            if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
                storageInformation.setStorageClass(new StorageClass(diff.getStorageclassOld(), ""));
            }
            for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
                if (diff.getStorageConditionsNew().get(i) == null) {
                    storageInformation.getStorageConditions().add(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));
                } else {
                    storageInformation.getStorageConditions().remove(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));
                }
            }
        }
    }

    /**
     * Returns detailinformation about the current timepoint and its edit
     * actions.
     *
     * @return
     */
    public String getVersionComments() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (materialEditState.getCurrentVersiondate() != null) {
            return sdf.format(materialEditState.getCurrentVersiondate());
        } else {
            return "original material";
        }
    }

    protected void applyPositiveHazards() {
        MaterialHazardDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialHazardDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            for (int i = 0; i < diff.getEntries(); i++) {
                Integer newTypeId = diff.getTypeIdsNew().get(i);
                Integer oldTypeId = diff.getTypeIdsOld().get(i);
                String newValue = diff.getRemarksNew().get(i);
                boolean isHazardStatementEntry
                        = Objects.equals(HazardInformation.HAZARD_STATEMENT, oldTypeId)
                        || Objects.equals(HazardInformation.HAZARD_STATEMENT, newTypeId);
                if (isHazardStatementEntry) {
                    materialEditState.getHazards().setHazardStatements(newValue);
                    continue;
                }
                boolean isPreStatementEntry
                        = Objects.equals(HazardInformation.PRECAUTIONARY_STATEMENT, oldTypeId)
                        || Objects.equals(HazardInformation.PRECAUTIONARY_STATEMENT, newTypeId);
                if (isPreStatementEntry) {
                    materialEditState.getHazards().setPrecautionaryStatements(newValue);
                    continue;
                }
                if (newTypeId != null) {
                    materialEditState.getHazards().getHazards().add(Hazard.getHazardById(newTypeId));
                } else {
                    materialEditState.getHazards().getHazards().remove(Hazard.getHazardById(oldTypeId));
                }
            }
        }
    }

    protected void applyNegativeHazards() {
        MaterialHazardDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialHazardDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            for (int i = 0; i < diff.getEntries(); i++) {
                Integer newTypeId = diff.getTypeIdsNew().get(i);
                Integer oldTypeId = diff.getTypeIdsOld().get(i);
                String oldValue = diff.getRemarksOld().get(i);
                boolean isHazardStatementEntry
                        = Objects.equals(HazardInformation.HAZARD_STATEMENT, oldTypeId)
                        || Objects.equals(HazardInformation.HAZARD_STATEMENT, newTypeId);
                if (isHazardStatementEntry) {
                    materialEditState.getHazards().setHazardStatements(oldValue);
                    continue;
                }
                boolean isPreStatementEntry
                        = Objects.equals(HazardInformation.PRECAUTIONARY_STATEMENT, oldTypeId)
                        || Objects.equals(HazardInformation.PRECAUTIONARY_STATEMENT, newTypeId);
                if (isPreStatementEntry) {
                    materialEditState.getHazards().setPrecautionaryStatements(oldValue);
                    continue;
                }
                if (oldTypeId != null) {
                    materialEditState.getHazards().getHazards().add(Hazard.getHazardById(oldTypeId));
                } else {
                    materialEditState.getHazards().getHazards().remove(Hazard.getHazardById(newTypeId));
                }
            }
        }
    }

    protected void applyNegativeOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (diff.getProjectIdOld() != null) {
                materialEditState.setCurrentProject(projectBean.getReadableProjectById(diff.getProjectIdOld()));
            }
        }
    }

    protected void applyPositiveOverview() {
        MaterialOverviewDifference diff = materialEditState.getMaterialBeforeEdit().getHistory().getDifferenceOfTypeAtDate(MaterialOverviewDifference.class, materialEditState.getCurrentVersiondate());
        if (diff != null) {
            if (diff.getProjectIdOld() != null) {
                materialEditState.setCurrentProject(projectBean.getReadableProjectById(diff.getProjectIdNew()));
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
                structureInfos.setMolarMass(structureDiff.getMolarMass_old());
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
                structureInfos.setMolarMass(structureDiff.getMolarMass_new());
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
                    for (int j = materialNameBean.getNames().size() - 1; j >= 0; j--) {
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

                    for (int j = originalNameSize - 1; j >= 0; j--) {
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

}
