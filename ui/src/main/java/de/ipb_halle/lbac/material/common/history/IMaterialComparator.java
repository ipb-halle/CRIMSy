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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageCondition;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public abstract class IMaterialComparator {

    public abstract void compareDifferences(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) throws Exception;

    /**
     * Adds the differences of the names of the material to the list of
     * differences
     *
     * @param differences
     * @param originalMat
     * @param editedMat
     */
    protected void addMaterialNameDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) {
        MaterialIndexDifference diffs = new MaterialIndexDifference();

        List<MaterialName> oldNames = originalMat.getCopiedNames();
        List<MaterialName> newNames = editedMat.getCopiedNames();

        for (MaterialName newName : newNames) {
            diffs.getValuesNew().add(newName.getValue());
            diffs.getValuesOld().add(null);
            diffs.getRankNew().add(newName.getRank());
            diffs.getRankOld().add(null);
            diffs.getTypeId().add(1);
            diffs.getLanguageNew().add(newName.getLanguage());
            diffs.getLanguageOld().add(null);
        }
        for (int i = oldNames.size() - 1; i >= 0; i--) {
            MaterialName oldName = oldNames.get(i);
            if (i < diffs.getEntries()) {
                diffs.getLanguageOld().set(i, oldName.getLanguage());
                diffs.getRankOld().set(i, oldName.getRank());
                diffs.getValuesOld().set(i, oldName.getValue());
                oldNames.remove(i);
            }
        }
        for (MaterialName mn : oldNames) {
            diffs.getValuesOld().add(mn.getValue());
            diffs.getValuesNew().add(null);
            diffs.getRankOld().add(mn.getRank());
            diffs.getRankNew().add(null);
            diffs.getTypeId().add(1);
            diffs.getLanguageOld().add(mn.getLanguage());
            diffs.getLanguageNew().add(null);
        }
        diffs.clearRedundantEntries();
        if (diffs.differenceFound()) {
            differences.add(diffs);
        }
    }

    /**
     * Adds the differences of common informations
     *
     * @param differences
     * @param originalMat
     * @param editedMat
     * @throws Exception
     */
    protected void addOverviewDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) throws Exception {
        MaterialOverviewDifference overviewDiff = new MaterialOverviewDifference();
        //Check material overview differences
        if (originalMat.getACList() == null || editedMat.getACList() == null) {
            throw new Exception("Acl-list of material not set !");
        }
        boolean bothAclsEqual = originalMat.getACList().permEquals(editedMat.getACList());
        if (!bothAclsEqual) {
            overviewDiff.setAcListNew(editedMat.getACList());
            overviewDiff.setAcListOld(originalMat.getACList());
        }
        if (originalMat.getOwner() == null || editedMat.getOwner() == null) {
            throw new Exception("Owner of material not set !");
        }
        if (!originalMat.getOwner().getId().equals(editedMat.getOwner().getId())) {
            overviewDiff.setOwnerIdNew(editedMat.getOwner().getId());
            overviewDiff.setOwnerIdOld(originalMat.getOwner().getId());
        }
        if (!Objects.equals(originalMat.getProjectId(), editedMat.getProjectId())) {
            overviewDiff.setProjectIdNew(editedMat.getProjectId());
            overviewDiff.setProjectIdOld(originalMat.getProjectId());
        }
        if (overviewDiff.differenceFound()) {
            differences.add(overviewDiff);
        }
    }

    /**
     * Adds the differences of indices to the list of differences.
     *
     * @param differences
     * @param originalMat
     * @param editedMat
     */
    protected void addIndexDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) {
        MaterialIndexDifference diffs = new MaterialIndexDifference();

        List<IndexEntry> origIndices = originalMat.getCopiedIndices();
        List<IndexEntry> editIndices = editedMat.getCopiedIndices();

        for (int i = origIndices.size() - 1; i >= 0; i--) {
            IndexEntry orig = origIndices.get(i);
            for (int j = editIndices.size() - 1; j >= 0; j--) {
                IndexEntry edit = editIndices.get(j);
                if (edit.getTypeId() == orig.getTypeId()) {
                    diffs.getLanguageNew().add(null);
                    diffs.getLanguageOld().add(null);
                    diffs.getRankNew().add(0);
                    diffs.getRankOld().add(0);
                    diffs.getValuesNew().add(edit.getValue());
                    diffs.getValuesOld().add(orig.getValue());
                    diffs.getTypeId().add(edit.getTypeId());
                    editIndices.remove(j);
                    origIndices.remove(i);
                    break;
                }
            }
        }

        for (IndexEntry ie : origIndices) {
            diffs.getLanguageNew().add(null);
            diffs.getLanguageOld().add(null);
            diffs.getRankNew().add(0);
            diffs.getRankOld().add(0);
            diffs.getValuesNew().add(null);
            diffs.getValuesOld().add(ie.getValue());
            diffs.getTypeId().add(ie.getTypeId());
        }
        for (IndexEntry ie : editIndices) {
            diffs.getLanguageNew().add(null);
            diffs.getLanguageOld().add(null);
            diffs.getRankNew().add(0);
            diffs.getRankOld().add(0);
            diffs.getValuesNew().add(ie.getValue());
            diffs.getValuesOld().add(null);
            diffs.getTypeId().add(ie.getTypeId());
        }
        diffs.clearRedundantEntries();
        if (diffs.differenceFound()) {
            differences.add(diffs);
        }
    }

    /**
     * Adds the hazard differences to the list of differences.
     *
     * @param differences
     * @param originalMat
     * @param editedMat
     */
    protected void addHazardDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) {
        MaterialHazardDifference hazardDiff = new MaterialHazardDifference();
        Map<HazardType, String> newHazards = editedMat.getHazards().getHazards();
        Map<HazardType, String> oldHazards = originalMat.getHazards().getHazards();
        //Find all hazards in edited material, but not in original one
        Set<HazardType> hazardsInEditedButNotInOld
                = AWithoutB(newHazards.keySet(), oldHazards.keySet());
        for (HazardType hazard : hazardsInEditedButNotInOld) {
            hazardDiff.addHazardExpansion(hazard.getId(), newHazards.get(hazard));
        }
        //Find all hazards in original material, but not in edited one
        Set<HazardType> hazardsInOldButNotInNew
                = AWithoutB(oldHazards.keySet(), newHazards.keySet());
        for (HazardType hazard : hazardsInOldButNotInNew) {
            hazardDiff.addHazardRemovement(hazard.getId(), oldHazards.get(hazard));
        }
        //Find all hazards which are in old and new material, but gor a different remark string
        for (HazardType oldHazard : oldHazards.keySet()) {
            for (HazardType newHazard : newHazards.keySet()) {
                if (newHazard.equals(oldHazard)) {
                    String newRemark = newHazards.get(newHazard);
                    String oldRemark = oldHazards.get(oldHazard);
                    if (!Objects.equals(newRemark, oldRemark)) {
                        hazardDiff.addDifference(
                                oldHazard.getId(),
                                newHazard.getId(),
                                oldRemark,
                                newRemark);
                    }
                }
            }
        }

        if (hazardDiff.getEntries() > 0) {
            differences.add(hazardDiff);
        }
    }

    /**
     * Return the result of the set operation "A without B" without altering the
     * original sets A and B
     *
     * @param A
     * @param B
     * @return
     */
    protected Set<HazardType> AWithoutB(
            Set<HazardType> A,
            Set<HazardType> B) {
        Set<HazardType> diff = new HashSet<>();
        for (HazardType y : A) {
            if (!B.contains(y)) {
                diff.add(y);
            }
        }
        return diff;

    }

    protected void addStorageDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) throws Exception {
        MaterialStorageDifference diff = new MaterialStorageDifference();
        Integer newStorageClassId = editedMat.getStorageInformation().getStorageClass() == null ? null : editedMat.getStorageInformation().getStorageClass().getId();
        Integer oldStorageClassId = originalMat.getStorageInformation().getStorageClass() == null ? null : originalMat.getStorageInformation().getStorageClass().getId();
        String newDescription = editedMat.getStorageInformation().getRemarks();
        String oldDescription = originalMat.getStorageInformation().getRemarks();
        Set<StorageCondition> oldConditions = originalMat.getStorageInformation().getStorageConditions();
        Set<StorageCondition> newConditions = editedMat.getStorageInformation().getStorageConditions();
        if (!Objects.equals(newStorageClassId, oldStorageClassId)) {
            diff.changeStorageClass(oldStorageClassId, newStorageClassId);
        }
        if (!Objects.equals(oldDescription, newDescription)) {
            diff.changeStorageDescription(oldDescription, newDescription);
        }

        for (StorageCondition c : oldConditions) {
            if (!newConditions.contains(c)) {
                diff.removeCondition(c);
            }
        }
        for (StorageCondition c : newConditions) {
            if (!oldConditions.contains(c)) {
                diff.addCondition(c);
            }
        }
        if (diff.diffFound()) {
            differences.add(diff);
        }
    }

}
