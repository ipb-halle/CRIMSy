/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.difference;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import de.ipb_halle.lbac.material.bean.ModificationType;
import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.subtype.structure.Molecule;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.subtype.structure.Structure;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialComparator {

    Logger logger = LogManager.getLogger(this.getClass().getName());

    private void addTaxonomyDifference(
            List<MaterialDifference> differences,
            Taxonomy originalMat,
            Taxonomy editedMat) {

        TaxonomyDifference td = new TaxonomyDifference();
        if (originalMat.getLevel().getId() != editedMat.getLevel().getId()) {
            td.setOldLevelId(originalMat.getLevel().getId());
            td.setNewLevelId(editedMat.getLevel().getId());
        }
        if (originalMat.getTaxHierachy().get(0).getId() != editedMat.getTaxHierachy().get(0).getId()) {
            for (Taxonomy t : editedMat.getTaxHierachy()) {
                td.getNewHierarchy().add(t.getId());
            }
            for (Taxonomy t : originalMat.getTaxHierachy()) {
                td.getOldHierarchy().add(t.getId());
            }
        }
        differences.add(td);

    }

    /**
     * Compares two materials and calculates the differences.
     *
     * @param originalMat
     * @param editedMat
     * @return List with all differences.
     * @throws Exception
     */
    public List<MaterialDifference> compareMaterial(
            Material originalMat,
            Material editedMat) throws Exception {

        List<MaterialDifference> differences = new ArrayList<>();
        if (originalMat.getType() != editedMat.getType()) {
            throw new Exception("Materials not comparable: ORIG - " + originalMat.getType() + " EDIT - " + editedMat.getType());
        }
        addOverviewDifference(differences, originalMat, editedMat);
        addMaterialNameDifference(differences, originalMat, editedMat);
        addIndexDifference(differences, originalMat, editedMat);
        addHazardDifference(differences, originalMat, editedMat);
        addStorageDifference(differences, originalMat, editedMat);

        if (originalMat.getType() == MaterialType.STRUCTURE) {
            addStructureDifferences(differences,
                    (Structure) originalMat,
                    (Structure) editedMat);
        }
        if (originalMat.getType() == MaterialType.TAXONOMY) {
            addTaxonomyDifference(differences,
                    (Taxonomy) originalMat,
                    (Taxonomy) editedMat);
        }

        return differences;

    }

    /**
     * Helpermethod to fetch a concrete type of the MaterialDifference interface
     * from the list of differences.
     *
     * @param <T>
     * @param diffs
     * @param T
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getDifferenceOfType(List<MaterialDifference> diffs, Class T) {
        for (MaterialDifference sd : diffs) {
            if (sd.getClass() == T) {
                return (T) sd;
            }
        }
        return null;
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
        if (originalMat.getAcList() == null || editedMat.getAcList() == null) {
            throw new Exception("Acl-list of material not set !");
        }
        boolean bothAclsEqual = originalMat.getAcList().permEquals(editedMat.getAcList());
        if (!bothAclsEqual) {
            overviewDiff.setAcListNew(editedMat.getAcList());
            overviewDiff.setAcListOld(originalMat.getAcList());
        }
        if (originalMat.getOwnerID() == null || editedMat.getOwnerID() == null) {
            throw new Exception("Owner of material not set !");
        }
        if (!originalMat.getOwnerID().toString().equals(editedMat.getOwnerID().toString())) {
            overviewDiff.setOwnerIdNew(editedMat.getOwnerID());
            overviewDiff.setOwnerIdOld(originalMat.getOwnerID());
        }
        if (originalMat.getProjectId() != editedMat.getProjectId()) {
            overviewDiff.setProjectIdNew(editedMat.getProjectId());
            overviewDiff.setProjectIdOld(originalMat.getProjectId());
        }
        if (overviewDiff.differenceFound()) {
            differences.add(overviewDiff);
        }
    }

    protected void addStorageDifference(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) throws Exception {
        MaterialStorageDifference diff = new MaterialStorageDifference();
        Integer newStorageClassId = editedMat.getStorageInformation().getStorageClass().getId();
        Integer oldStorageClassId = originalMat.getStorageInformation().getStorageClass().getId();
        String newDescription = editedMat.getStorageInformation().getRemarks();
        String oldDescription = originalMat.getStorageInformation().getRemarks();
        Set<StorageCondition> oldConditions = originalMat.getStorageInformation().getStorageConditions();
        Set<StorageCondition> newConditions = editedMat.getStorageInformation().getStorageConditions();
        if (!Objects.equals(newStorageClassId, oldStorageClassId)) {
            diff.changeStorageClass(oldStorageClassId, newStorageClassId);
        }
        if (!Objects.equals(newStorageClassId, oldStorageClassId)) {
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

        addSingleHazardDiff(
                hazardDiff,
                originalMat.getHazards().getHazardStatements(),
                editedMat.getHazards().getHazardStatements(),
                HazardInformation.HAZARD_STATEMENT);

        addSingleHazardDiff(
                hazardDiff,
                originalMat.getHazards().getPrecautionaryStatements(),
                editedMat.getHazards().getPrecautionaryStatements(),
                HazardInformation.PRECAUTIONARY_STATEMENT);
        Set<Hazard> removedHazards = getNotMatchingHazards(originalMat.getHazards().getHazards(), editedMat.getHazards().getHazards());
        Set<Hazard> newHazards = getNotMatchingHazards(editedMat.getHazards().getHazards(), originalMat.getHazards().getHazards());
        for (Hazard h : removedHazards) {
            hazardDiff.addHazardRemovement(h.getTypeId());
        }
        for (Hazard h : newHazards) {
            hazardDiff.addHazardExpansion(h.getTypeId());
        }
        if (hazardDiff.getEntries() > 0) {
            differences.add(hazardDiff);
        }

    }

    /**
     * Adds the differences of structure information to the list of differences
     *
     * @param differences
     * @param originalStruc
     * @param editedStruc
     */
    protected void addStructureDifferences(
            List<MaterialDifference> differences,
            Structure originalStruc,
            Structure editedStruc) {

        MaterialStructureDifference diff = new MaterialStructureDifference();
        if (!originalStruc.getSumFormula().equals(editedStruc.getSumFormula())) {
            diff.setSumFormula_new(editedStruc.getSumFormula());
            diff.setSumFormula_old(originalStruc.getSumFormula());
        }
        if (Math.abs(originalStruc.getExactMolarMass() - editedStruc.getExactMolarMass()) > Double.MIN_NORMAL) {
            diff.setExactMolarMass_new(editedStruc.getExactMolarMass());
            diff.setExactMolarMass_old(originalStruc.getExactMolarMass());
        }
        if (Math.abs(originalStruc.getMolarMass() - editedStruc.getMolarMass()) > Double.MIN_NORMAL) {
            diff.setMolarMass_new(editedStruc.getMolarMass());
            diff.setMolarMass_old(originalStruc.getMolarMass());
        }
        Molecule origMol = originalStruc.getMolecule();
        Molecule newMol = editedStruc.getMolecule();
        boolean newMoleculeSet = origMol == null && newMol != null;
        boolean newMoleculeRemoved = origMol != null && newMol == null;
        boolean modelEdited = newMol != null
                && origMol != null
                && !newMol.getStructureModel().equals(origMol.getStructureModel())
                && newMol.getModelType() == origMol.getModelType();

        if (newMoleculeSet || newMoleculeRemoved || modelEdited) {
            diff.setMoleculeId_new(editedStruc.getMolecule());
            diff.setMoleculeId_old(originalStruc.getMolecule());
        }

        diff.setAction(ModificationType.EDIT);
        if (diff.differenceFound()) {
            differences.add(diff);
        }
    }

    protected Set<Hazard> getNotMatchingHazards(
            Set<Hazard> valuestoCheck,
            Set<Hazard> valuestoCheckAgainst) {
        Set<Hazard> x = new HashSet<>();
        for (Hazard y : valuestoCheck) {
            if (!valuestoCheckAgainst.contains(y)) {
                x.add(y);
            }
        }
        return x;

    }

    protected void addSingleHazardDiff(
            MaterialHazardDifference hazardDiff,
            String oldValue,
            String newValue,
            Integer typId) {
        if (!Objects.equals(oldValue, newValue)) {
            if (oldValue == null) {
                hazardDiff.addDifference(null, typId, null, newValue);
            } else if (newValue == null) {
                hazardDiff.addDifference(typId, null, oldValue, null);
            } else {
                hazardDiff.addDifference(typId, typId, oldValue, newValue);
            }
        }
    }

}
