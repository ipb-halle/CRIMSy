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
package de.ipb_halle.lbac.material.common.difference;

import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class MaterialComparatorTest {

    private MaterialComparator instance;

    @Before
    public void init() {
        instance = new MaterialComparator();
    }

    @Test(expected = Exception.class)
    public void test001_compareMaterialWithDifferentType() throws Exception {
        instance.compareMaterial(
                createEmptyStructure(UUID.randomUUID()),
                new BioMaterial(
                        34,
                        new ArrayList<>(),
                        1,
                        new HazardInformation(),
                        new StorageClassInformation(),
                        null, null)
        );
    }

    @Test
    public void test002_compareMaterialWithoutDifference() throws Exception {
        UUID ownerID = UUID.randomUUID();
        Assert.assertTrue(
                "test002-List of differences must be empty",
                instance.compareMaterial(
                        createEmptyStructure(ownerID),
                        createEmptyStructure(ownerID)).isEmpty());
    }

    @Test
    public void test003_compareMaterialWithNameDifference() throws Exception {
        UUID ownerID = UUID.randomUUID();
        Structure oldStruc = createEmptyStructure(ownerID);
        Structure newStruc = createEmptyStructure(ownerID);

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
        Structure oldStruc = createEmptyStructure(ownerID);
        Structure newStruc = createEmptyStructure(ownerID);

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
        UUID ownerID = UUID.randomUUID();
        Structure oldStruc = createEmptyStructure(ownerID);
        Structure newStruc = createEmptyStructure(ownerID);
        //Testcase 1 : remove information fro structure
        oldStruc.setMolecule(new Molecule("xx-xx", 0));
        oldStruc.setSumFormula("h2o");
        oldStruc.setMolarMass(12d);
        oldStruc.setExactMolarMass(11d);
        newStruc.setMolecule(null);
        newStruc.setSumFormula(null);
        newStruc.setMolarMass(0d);
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
        UUID ownerID = UUID.randomUUID();
        Structure oldStruc = createEmptyStructure(ownerID);
        Structure newStruc = createEmptyStructure(ownerID);

        oldStruc.getHazards().setHazardStatements("HazardBeforeEdit");
        oldStruc.getHazards().setPrecautionaryStatements(null);
        oldStruc.getHazards().setAttention(true);
        oldStruc.getHazards().setHighlyFlammable(true);
        oldStruc.getHazards().setPoisonous(true);

        newStruc.getHazards().setHazardStatements("HazardAfterEdit");
        newStruc.getHazards().setPrecautionaryStatements("PrecautionaryStatementAfterEdit");
        newStruc.getHazards().setHighlyFlammable(true);
        newStruc.getHazards().setIrritant(true);

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);

        MaterialHazardDifference hazardDiff = instance.getDifferenceOfType(diffs, MaterialHazardDifference.class);
        Assert.assertNotNull("Testcase 6 - Diff must be found", hazardDiff);
        Assert.assertEquals("Testcase 6 - 5 diffs must be found", 5, hazardDiff.getEntries());

        Assert.assertEquals("Testcase 6 - new HazardRemark must be 'HazardAfterEdit'", "HazardAfterEdit", hazardDiff.getRemarksNew().get(0));
        Assert.assertEquals("Testcase 6 - old HazardRemark must be 'HazardBeforeEdit'", "HazardBeforeEdit", hazardDiff.getRemarksOld().get(0));
        Assert.assertEquals("Testcase 6 - new PrecautionaryRemark must be 'Precautionary'", "PrecautionaryStatementAfterEdit", hazardDiff.getRemarksNew().get(1));
        Assert.assertNull("Testcase 6 - old PrecautionaryRemark must be null", hazardDiff.getRemarksOld().get(1));
        Integer indexOfAttention = hazardDiff.getTypeIdsOld().indexOf(Hazard.attention.getTypeId());
        Assert.assertNotNull("Testcase 6 - Attention wast not removed", indexOfAttention);
        Assert.assertNull("Testcase 6 - Attention was not removed", hazardDiff.getTypeIdsNew().get(indexOfAttention));

        Integer indexOfPoisonous = hazardDiff.getTypeIdsOld().indexOf(Hazard.poisonous.getTypeId());
        Assert.assertNotNull("Testcase 6 - Poisonous wast not removed", indexOfPoisonous);
        Assert.assertNull("Testcase 6 - Poisonous was not removed", hazardDiff.getTypeIdsNew().get(indexOfPoisonous));

        Integer indexOfIrritant = hazardDiff.getTypeIdsNew().indexOf(Hazard.irritant.getTypeId());
        Assert.assertNotNull("Testcase 6 - Irritant wast not added", indexOfIrritant);
        Assert.assertNull("Testcase 6 - Irritant was not added", hazardDiff.getTypeIdsOld().get(indexOfIrritant));
    }

    @Test
    public void test007_compareMaterialWithStorageDifference() throws Exception {
        UUID ownerID = UUID.randomUUID();
        Structure oldStruc = createEmptyStructure(ownerID);
        Structure newStruc = createEmptyStructure(ownerID);

        List<MaterialDifference> diffs = instance.compareMaterial(
                oldStruc, newStruc);
        MaterialStorageDifference storageDiff = instance.getDifferenceOfType(diffs, MaterialStorageDifference.class);
        Assert.assertNull(storageDiff);

        oldStruc.getStorageInformation().setAcidSensitive(true);
        oldStruc.getStorageInformation().setKeepMoist(true);
        oldStruc.getStorageInformation().setStorageClass(new StorageClass(1, "oldStorageClass"));

        newStruc.getStorageInformation().setAcidSensitive(false);
        newStruc.getStorageInformation().setKeepMoist(true);
        newStruc.getStorageInformation().setKeepTempBelowMinus40Celsius(true);
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

    protected Structure createEmptyStructure(UUID ownerID) {
        List<MaterialName> names = new ArrayList<>();
        int projectId = 4;
        HazardInformation hazards = new HazardInformation();
        StorageClassInformation storage = new StorageClassInformation();
        double molarMass = 0;
        double exactMolarMass = 0;
        int id = 0;
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
        s.setAcList(new ACList());
        s.setOwnerID(ownerID);
        s.getStorageInformation().setStorageClass(new StorageClass(0, "default class"));
        return s;
    }
}
