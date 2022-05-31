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
package de.ipb_halle.lbac.material.common.difference;

import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.composition.CompositionDifference;
import de.ipb_halle.lbac.material.composition.CompositionHistoryEntity;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class MaterialComparatorTest {

    private MaterialComparator instance;
    private User user;

    @BeforeEach
    public void init() {
        instance = new MaterialComparator();
        user = new User();
        user.setId(-1);
    }

    @Test
    public void test001_compareMaterialWithDifferentType() throws Exception {
        Assert.assertThrows(Exception.class, () ->
            instance.compareMaterial(createEmptyStructure(user),
                    new BioMaterial(
                            34,
                            new ArrayList<>(),
                            1,
                            new HazardInformation(),
                            new StorageInformation(),
                            null, null)
                    )
        );
    }

    @Test
    public void test002_compareMaterialWithoutDifference() throws Exception {
        Assert.assertTrue(
                "test002-List of differences must be empty",
                instance.compareMaterial(
                        createEmptyStructure(user),
                        createEmptyStructure(user)).isEmpty());
    }

    @Test
    public void test003_compareMaterialWithNameDifference() throws Exception {
        Structure oldStruc = createEmptyStructure(user);
        Structure newStruc = createEmptyStructure(user);

        //Testcase 1 - an old Name was deleted, everything else remains
        oldStruc.getNames().add(new MaterialName("water", "en", 0));
        oldStruc.getNames().add(new MaterialName("Wasser", "de", 1));
        oldStruc.getNames().add(new MaterialName("Diwasserstoffoxyd", "de", 2));

        newStruc.getNames().add(new MaterialName("water", "en", 0));
        newStruc.getNames().add(new MaterialName("Wasser", "de", 1));

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);

        MaterialIndexDifference indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertNotNull(indexDiff);
        Assert.assertEquals(1, indexDiff.getEntries());
        Assert.assertNull(indexDiff.getLanguageNew().get(0));
        Assert.assertNull(indexDiff.getRankNew().get(0));
        Assert.assertNull(indexDiff.getValuesNew().get(0));
        Assert.assertEquals("Diwasserstoffoxyd", indexDiff.getValuesOld().get(0));
        Assert.assertEquals("de", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals(2, indexDiff.getRankOld().get(0), 0);
        Assert.assertEquals(1, indexDiff.getTypeId().get(0), 0);

        //Testcase 2 - a new name was added and there was a change in the ranks
        oldStruc.getNames().clear();
        oldStruc.getNames().add(new MaterialName("water", "en", 0));
        oldStruc.getNames().add(new MaterialName("Wasser", "de", 1));

        newStruc.getNames().clear();
        newStruc.getNames().add(new MaterialName("Diwasserstoffoxyd", "de", 0));
        newStruc.getNames().add(new MaterialName("water", "en", 1));
        newStruc.getNames().add(new MaterialName("Wasser", "de", 2));
        diffs = instance.compareMaterial(
                oldStruc, newStruc);
        indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertNotNull(indexDiff);
        Assert.assertEquals(3, indexDiff.getEntries());

        Assert.assertEquals("de", indexDiff.getLanguageNew().get(0));
        Assert.assertEquals("en", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals(0, indexDiff.getRankOld().get(0), 0);
        Assert.assertEquals(0, indexDiff.getRankNew().get(0), 0);
        Assert.assertEquals("water", indexDiff.getValuesOld().get(0));
        Assert.assertEquals("Diwasserstoffoxyd", indexDiff.getValuesNew().get(0));
        Assert.assertEquals(1, indexDiff.getTypeId().get(0), 0);

        Assert.assertEquals("en", indexDiff.getLanguageNew().get(1));
        Assert.assertEquals("de", indexDiff.getLanguageOld().get(1));
        Assert.assertEquals(1, indexDiff.getRankOld().get(1), 0);
        Assert.assertEquals(1, indexDiff.getRankNew().get(1), 0);
        Assert.assertEquals("Wasser", indexDiff.getValuesOld().get(1));
        Assert.assertEquals("water", indexDiff.getValuesNew().get(1));
        Assert.assertEquals(1, indexDiff.getTypeId().get(1), 0);

        Assert.assertNull(indexDiff.getLanguageOld().get(2));
        Assert.assertNull(indexDiff.getValuesOld().get(2));
        Assert.assertNull(indexDiff.getRankOld().get(2));

        Assert.assertEquals("de", indexDiff.getLanguageNew().get(2));
        Assert.assertEquals(2, indexDiff.getRankNew().get(2), 0);
        Assert.assertEquals("Wasser", indexDiff.getValuesNew().get(2));
        Assert.assertEquals(1, indexDiff.getTypeId().get(2), 0);

        //Testcase 3 - name was removed and one name switched rank
        oldStruc.getNames().clear();
        oldStruc.getNames().add(new MaterialName("Wasser", "de", 0));
        oldStruc.getNames().add(new MaterialName("water", "en", 1));

        newStruc.getNames().clear();
        newStruc.getNames().add(new MaterialName("water", "en", 0));

        diffs = instance.compareMaterial(
                oldStruc, newStruc);
        indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertEquals("en", indexDiff.getLanguageNew().get(0));
        Assert.assertEquals("de", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals(0, indexDiff.getRankOld().get(0), 0);
        Assert.assertEquals(0, indexDiff.getRankNew().get(0), 0);
        Assert.assertEquals("Wasser", indexDiff.getValuesOld().get(0));
        Assert.assertEquals("water", indexDiff.getValuesNew().get(0));
        Assert.assertEquals(1, indexDiff.getTypeId().get(0), 0);

        Assert.assertNull(indexDiff.getLanguageNew().get(1));
        Assert.assertEquals("en", indexDiff.getLanguageOld().get(1));
        Assert.assertEquals(1, indexDiff.getRankOld().get(1), 0);
        Assert.assertNull(indexDiff.getRankNew().get(1));
        Assert.assertEquals("water", indexDiff.getValuesOld().get(1));
        Assert.assertNull(indexDiff.getValuesNew().get(1));
        Assert.assertEquals(1, indexDiff.getTypeId().get(1), 0);
    }

    @Test
    public void test004_compareMaterialWithIndexDifference() throws Exception {
        UUID ownerID = UUID.randomUUID();
        Structure oldStruc = createEmptyStructure(user);
        Structure newStruc = createEmptyStructure(user);

        //Testcase 1: change an value of an existing entry
        oldStruc.getIndices().add(new IndexEntry(2, "A38", null));
        newStruc.getIndices().add(new IndexEntry(2, "B38", null));
        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);
        MaterialIndexDifference indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertNotNull("No difference was found", indexDiff);
        Assert.assertEquals("Testcase 4: Only 1 diff must be present", 1, indexDiff.getEntries());
        Assert.assertEquals("Old Value must be A38", "A38", indexDiff.getValuesOld().get(0));
        Assert.assertEquals("New Value must be B38", "B38", indexDiff.getValuesNew().get(0));
        Assert.assertNull("Languages of new Value must be null", indexDiff.getLanguageNew().get(0));
        Assert.assertNull("Languages of old Value must be null", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals("Type must be 2", 2, (long) indexDiff.getTypeId().get(0));

        //Testcase 2: add a new index
        oldStruc.getIndices().clear();
        oldStruc.getIndices().add(new IndexEntry(2, "A38", null));
        newStruc.getIndices().clear();
        newStruc.getIndices().add(new IndexEntry(2, "A38", null));
        newStruc.getIndices().add(new IndexEntry(3, "B38", null));
        diffs = instance.compareMaterial(
                oldStruc, newStruc);
        indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertNotNull("No difference was found", indexDiff);
        Assert.assertEquals("Testcase 1: Only 1 diff must be present", 1, indexDiff.getEntries());
        Assert.assertNull("Old Value must be null", indexDiff.getValuesOld().get(0));
        Assert.assertEquals("New Value must be B38", "B38", indexDiff.getValuesNew().get(0));
        Assert.assertNull("Languages of new Value must be null", indexDiff.getLanguageNew().get(0));
        Assert.assertNull("Languages of old Value must be null", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals("Type must be 2", 3, (long) indexDiff.getTypeId().get(0));
        //Testcase 3: remove an index
        oldStruc.getIndices().clear();
        oldStruc.getIndices().add(new IndexEntry(2, "A38", null));
        oldStruc.getIndices().add(new IndexEntry(3, "B38", null));
        newStruc.getIndices().clear();
        newStruc.getIndices().add(new IndexEntry(2, "A38", null));

        diffs = instance.compareMaterial(
                oldStruc, newStruc);
        indexDiff = instance.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        Assert.assertNotNull("No difference was found", indexDiff);
        Assert.assertEquals("Testcase 1: Only 1 diff must be present", 1, indexDiff.getEntries());
        Assert.assertNull("New Value must be null", indexDiff.getValuesNew().get(0));
        Assert.assertEquals("Old Value must be B38", "B38", indexDiff.getValuesOld().get(0));
        Assert.assertNull("Languages of new Value must be null", indexDiff.getLanguageNew().get(0));
        Assert.assertNull("Languages of old Value must be null", indexDiff.getLanguageOld().get(0));
        Assert.assertEquals("Type must be 2", 3, (long) indexDiff.getTypeId().get(0));
    }

    @Test
    public void test005_compareMaterialWithStructureDifference() throws Exception {
        Structure oldStruc = createEmptyStructure(user);
        Structure newStruc = createEmptyStructure(user);
        //Testcase 1 : remove information fro structure
        oldStruc.setMolecule(new Molecule("xx-xx", 0));
        oldStruc.setSumFormula("h2o");
        oldStruc.setAverageMolarMass(12d);
        oldStruc.setExactMolarMass(11d);
        newStruc.setMolecule(null);
        newStruc.setSumFormula(null);
        newStruc.setAverageMolarMass(0d);
        newStruc.setExactMolarMass(0d);

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);

        MaterialStructureDifference strucDiff = instance.getDifferenceOfType(diffs, MaterialStructureDifference.class);
        Assert.assertNotNull("Testcase 1 - Diff must be found", strucDiff);
        Assert.assertNull("Testcase 1 - new molecule must be null", strucDiff.getMoleculeId_new());
        Assert.assertNull("Testcase 1 - new sumFormula must be null", strucDiff.getSumFormula_new());
        Assert.assertEquals("Testcase 1 - new molar mass mustbe zero", 0, strucDiff.getMolarMass_new(), 0);
        Assert.assertEquals("Testcase 1 - new exact molar mass must be zero", 0, strucDiff.getExactMolarMass_new(), 0);

        Assert.assertEquals("Testcase 1 - old moleculestructure must be xx-xx ", "xx-xx", strucDiff.getMoleculeId_old().getStructureModel());
        Assert.assertEquals("Testcase 1 - old Moleculid must be 0", 0, strucDiff.getMoleculeId_old().getId(), 0);
        Assert.assertEquals("Testcase 1 - old sumformula must be h2o", "h2o", strucDiff.getSumFormula_old());
        Assert.assertEquals("Testcase 1 - old molar mass must be 12", 12, strucDiff.getMolarMass_old(), 0);
        Assert.assertEquals("Testcase 1 - old exact molar mass must be 11", 11, strucDiff.getExactMolarMass_old(), 0);
    }

    @Test
    public void test006_compareMaterialWithHazardDifference() throws Exception {
        Structure oldStruc = createEmptyStructure(user);
        Structure newStruc = createEmptyStructure(user);
        oldStruc.getHazards().getHazards().put(new HazardType(10, true, "hStatement", 2), "HazardBeforeEdit");
        oldStruc.getHazards().getHazards().put(new HazardType(11, true, "pStatement", 2), null);
        oldStruc.getHazards().getHazards().put(new HazardType(2, false, "GHS02", 1), null);
        oldStruc.getHazards().getHazards().put(new HazardType(6, false, "GHS06", 1), null);

        newStruc.getHazards().getHazards().put(new HazardType(10, true, "hStatement", 2), "HazardAfterEdit");
        newStruc.getHazards().getHazards().put(new HazardType(11, true, "hStatement", 2), "PrecautionaryStatementAfterEdit");
        newStruc.getHazards().getHazards().put(new HazardType(2, false, "GHS02", 1), null);
        newStruc.getHazards().getHazards().put(new HazardType(7, false, "GHS07", 1), null);

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);

        MaterialHazardDifference hazardDiff = instance.getDifferenceOfType(diffs, MaterialHazardDifference.class);
        Assert.assertNotNull("Testcase 6 - Diff must be found", hazardDiff);
        Assert.assertEquals("Testcase 6 - 5 diffs must be found", 4, hazardDiff.getEntries());
        Assert.assertEquals(7, hazardDiff.getTypeIdsNew().get(0), 0);
        Assert.assertNull(hazardDiff.getTypeIdsOld().get(0));
        Assert.assertNull(hazardDiff.getRemarksNew().get(0));
        Assert.assertNull(hazardDiff.getRemarksOld().get(0));
        Assert.assertNull(hazardDiff.getTypeIdsNew().get(1));
        Assert.assertEquals(6, hazardDiff.getTypeIdsOld().get(1), 0);
        Assert.assertNull(hazardDiff.getRemarksNew().get(1));
        Assert.assertNull(hazardDiff.getRemarksOld().get(1));
        int indexOfHStatement = 3;
        int indexOfPStatement = 2;
        if (hazardDiff.getTypeIdsNew().get(2) == 10) {
            indexOfHStatement = 2;
            indexOfPStatement = 3;
        }
        Assert.assertEquals(10, hazardDiff.getTypeIdsNew().get(indexOfHStatement), 0);
        Assert.assertEquals(10, hazardDiff.getTypeIdsOld().get(indexOfHStatement), 0);
        Assert.assertEquals("HazardAfterEdit", hazardDiff.getRemarksNew().get(indexOfHStatement));
        Assert.assertEquals("HazardBeforeEdit", hazardDiff.getRemarksOld().get(indexOfHStatement));
        Assert.assertEquals(11, hazardDiff.getTypeIdsNew().get(indexOfPStatement), 0);
        Assert.assertEquals(11, hazardDiff.getTypeIdsOld().get(indexOfPStatement), 0);
        Assert.assertNull(hazardDiff.getRemarksOld().get(indexOfPStatement));
        Assert.assertEquals("PrecautionaryStatementAfterEdit", hazardDiff.getRemarksNew().get(indexOfPStatement));

    }

    @Test
    public void test007_compareMaterialWithStorageDifference() throws Exception {
        Structure oldStruc = createEmptyStructure(user);
        Structure newStruc = createEmptyStructure(user);

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);
        MaterialStorageDifference storageDiff = instance.getDifferenceOfType(diffs, MaterialStorageDifference.class);
        Assert.assertNull(storageDiff);

        oldStruc.getStorageInformation().getStorageConditions().add(StorageCondition.lightSensitive);

        oldStruc.getStorageInformation().getStorageConditions().add(StorageCondition.keepMoist);
        oldStruc.getStorageInformation().setStorageClass(new StorageClass(1, "oldStorageClass"));

        newStruc.getStorageInformation().getStorageConditions().remove(StorageCondition.acidSensitive);
        newStruc.getStorageInformation().getStorageConditions().add(StorageCondition.keepMoist);
        newStruc.getStorageInformation().getStorageConditions().add(StorageCondition.keepTempBelowMinus40Celsius);
        newStruc.getStorageInformation().setStorageClass(new StorageClass(2, "newStorageClass"));

        diffs = instance.compareMaterial(
                oldStruc, newStruc);

        storageDiff = instance.getDifferenceOfType(diffs, MaterialStorageDifference.class);

        Assert.assertNotNull("Testcase 7 - Diff must be found ", storageDiff);

        Assert.assertEquals("Testcase 7 - old storageclass must have id 2", 1L, (long) storageDiff.getStorageclassOld());
        Assert.assertEquals("Testcase 7 - new storageclass must have id 2", 2L, (long) storageDiff.getStorageclassNew());

        Assert.assertEquals("Testcase 7 - 3 differences in old storage conditions must be found", 2, storageDiff.getStorageConditionsOld().size());
        Assert.assertEquals("Testcase 7 - 3 differences in new storage conditions must be found", 2, storageDiff.getStorageConditionsNew().size());
    }

    @Test
    public void test008_compareBioMaterialWithHazardDiffs() throws Exception {

        BioMaterial original = new BioMaterial(0, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), null, null);
        original.setACList(new ACList());
        original.setOwner(user);
        original.getHazards().getHazards().put(new HazardType(12, false, "S1", 2), null);
        BioMaterial copy = original.copyMaterial();
        List<MaterialDifference> diffs = instance.compareMaterial(original, copy);
        Assert.assertEquals(0, diffs.size());

        copy.getHazards().getHazards().clear();
        copy.getHazards().getHazards().put(new HazardType(13, false, "S2", 2), null);
        diffs = instance.compareMaterial(original, copy);
        Assert.assertEquals(1, diffs.size());
    }

    @Test
    public void test009_compareCompositions() throws Exception {
        //Prepare materials
        Structure structure1 = createEmptyStructure(user, "struc1", 1);
        Structure structure2 = createEmptyStructure(user, "struc2", 2);
        MaterialComposition composition1 = createEmptyComposition();
        MaterialComposition composition2 = createEmptyComposition();

        //UC1 -> add a new component
        composition1.addComponent(structure1, 1d, null);

        List<MaterialDifference> diffs = instance.compareMaterial(composition2, composition1);
        checkDifference(diffs, 1);
        checkDiffEntity(diffs, 0, null, structure1.getId(), null, 1d);

        //UC2 -> remove a component 
        diffs = instance.compareMaterial(composition1, composition2);
        checkDifference(diffs, 1);
        checkDiffEntity(diffs, 0, structure1.getId(), null, 1d, null);

        //UC3 -> swap components
        composition2.addComponent(structure2, null, null);
        diffs = instance.compareMaterial(composition1, composition2);
        checkDifference(diffs, 2);
        checkDiffEntity(diffs, 0, null, structure2.getId(), null, null);
        checkDiffEntity(diffs, 1, structure1.getId(), null, 1d, null);

        //UC4 -> change concentrations
        composition1.getComponents().clear();
        composition2.getComponents().clear();
        composition1.addComponent(structure1, .2d, null);
        composition2.addComponent(structure1, .3d, null);
        diffs = instance.compareMaterial(composition1, composition2);
        checkDifference(diffs, 1);
        checkDiffEntity(diffs, 0, structure1.getId(), structure1.getId(), .2d, .3d);
        //UC5 -> add 2 components
        composition1.getComponents().clear();
        composition2.getComponents().clear();
        composition2.addComponent(structure1, .2d, null);
        composition2.addComponent(structure2, .3d, null);
        diffs = instance.compareMaterial(composition1, composition2);
        checkDifference(diffs, 2);
        checkDiffEntity(diffs, 0, null, structure1.getId(), null, .2d);
        checkDiffEntity(diffs, 1, null, structure2.getId(), null, .3d);

        //UC6 -> change unit
        composition1.getComponents().clear();
        composition2.getComponents().clear();
        composition1.addComponent(structure1, .2d, Unit.getUnit("%"));
        composition2.addComponent(structure1, .2d, null);
        diffs = instance.compareMaterial(composition1, composition2);
        Assert.assertEquals(1, diffs.size());
        CompositionDifference diff = instance.getDifferenceOfType(diffs, CompositionDifference.class);
        Assert.assertNull(diff.getUnits_new().get(0));
        Assert.assertEquals("%", diff.getUnits_old().get(0));

        diffs = instance.compareMaterial(composition2, composition1);
        Assert.assertEquals(1, diffs.size());
        diff = instance.getDifferenceOfType(diffs, CompositionDifference.class);
        Assert.assertNull(diff.getUnits_old().get(0));
        Assert.assertEquals("%", diff.getUnits_new().get(0));

        composition1.getComponents().clear();
        composition2.getComponents().clear();
        composition1.addComponent(structure1, .2d, Unit.getUnit("%"));
        composition2.addComponent(structure1, .2d, Unit.getUnit("ml"));
        diffs = instance.compareMaterial(composition1, composition2);
        Assert.assertEquals(1, diffs.size());
        diff = instance.getDifferenceOfType(diffs, CompositionDifference.class);
        Assert.assertEquals("ml", diff.getUnits_new().get(0));
        Assert.assertEquals("%", diff.getUnits_old().get(0));

    }

    private void checkDifference(List<MaterialDifference> diffs, int expectedDifferences) {
        CompositionDifference diff = instance.getDifferenceOfType(diffs, CompositionDifference.class);
        List<CompositionHistoryEntity> entities = diff.createEntity();
        Assert.assertEquals(expectedDifferences, entities.size());
    }

    private void checkDiffEntity(
            List<MaterialDifference> diffs,
            int position,
            Integer materialIdOld,
            Integer materialIdNew,
            Double concentrationOld,
            Double concentrationNew) {
        CompositionDifference diff = instance.getDifferenceOfType(diffs, CompositionDifference.class);
        List<CompositionHistoryEntity> entities = diff.createEntity();
        CompositionHistoryEntity entity = entities.get(position);
        Assert.assertEquals(materialIdOld, entity.getMaterialid_old());
        Assert.assertEquals(materialIdNew, entity.getMaterialid_new());
        Assert.assertEquals(concentrationOld, entity.getConcentration_old());
        Assert.assertEquals(concentrationNew, entity.getConcentration_new());

    }

    private MaterialComposition createEmptyComposition() {
        MaterialComposition composition = new MaterialComposition(3, 1, CompositionType.MIXTURE);
        composition.setACList(new ACList());
        composition.setOwner(user);
        return composition;
    }

    protected Structure createEmptyStructure(User user) {
        return createEmptyStructure(user, "", 0);
    }

    protected Structure createEmptyStructure(User user, String name, int id) {
        List<MaterialName> names = new ArrayList<>();
        if (!name.equals("")) {
            names.add(new MaterialName(name, "en", 0));
        }
        int projectId = 4;
        HazardInformation hazards = new HazardInformation();
        StorageInformation storage = new StorageInformation();
        double molarMass = 0;
        double exactMolarMass = 0;
        Structure s = new Structure(
                "",
                molarMass,
                exactMolarMass,
                id,
                names,
                projectId,
                hazards,
                storage,
                new Molecule("", 1));
        s.setACList(new ACList());
        s.setOwner(user);
        s.getStorageInformation().setStorageClass(new StorageClass(0, "default class"));
        return s;
    }
}
