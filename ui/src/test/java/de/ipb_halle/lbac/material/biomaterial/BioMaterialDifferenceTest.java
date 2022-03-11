/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.history.MaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class BioMaterialDifferenceTest {

    User user = new User();

    public BioMaterialDifferenceTest() {
        user.setId(1);
        user.setName("testUser");
    }

    @Test
    public void test001_compareBioMaterials() throws Exception {
        MaterialComparator comparator = new MaterialComparator();

        Taxonomy taxo1 = createTaxonomy(1);
        Tissue tissue1 = createTissue(10, taxo1);
        BioMaterial biomaterial_original = createBioMaterial(100, taxo1, tissue1);

        List<MaterialDifference> diffs = comparator.compareMaterial(biomaterial_original, biomaterial_original);

        //Check self identity
        BioMaterialDifference bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNull(bioDiff);

        //check remove of taxonomy and tissue
        BioMaterial biomaterial_edited = biomaterial_original.copyMaterial();
        biomaterial_edited.setTaxonomy(null);
        biomaterial_edited.setTissue(null);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertEquals(1, (int) bioDiff.getTaxonomyid_old());
        Assert.assertNull(bioDiff.getTaxonomyid_new());
        Assert.assertEquals(10, (int) bioDiff.getTissueid_old());
        Assert.assertNull(bioDiff.getTissueid_new());

        //Check change of taxonomy and remove of tissue
        Taxonomy taxo2 = createTaxonomy(2);
        biomaterial_edited = createBioMaterial(200, taxo2, null);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertEquals(1, (int) bioDiff.getTaxonomyid_old());
        Assert.assertEquals(2, (int) bioDiff.getTaxonomyid_new());
        Assert.assertEquals(10, (int) bioDiff.getTissueid_old());
        Assert.assertNull(bioDiff.getTissueid_new());

        //Check change of tissue and remove of taxonomy
        Tissue tissue2 = createTissue(20, taxo2);
        biomaterial_edited = createBioMaterial(200, null, tissue2);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertEquals(1, (int) bioDiff.getTaxonomyid_old());
        Assert.assertNull(bioDiff.getTaxonomyid_new());
        Assert.assertEquals(10, (int) bioDiff.getTissueid_old());
        Assert.assertEquals(20, (int) bioDiff.getTissueid_new());

        //Check change of tissue and taxonomy
        biomaterial_edited = createBioMaterial(200, taxo2, tissue2);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertEquals(1, (int) bioDiff.getTaxonomyid_old());
        Assert.assertEquals(2, (int) bioDiff.getTaxonomyid_new());
        Assert.assertEquals(10, (int) bioDiff.getTissueid_old());
        Assert.assertEquals(20, (int) bioDiff.getTissueid_new());

        //Check change of tissue but taxonomy remains
        biomaterial_edited = createBioMaterial(200, taxo1, tissue2);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertFalse(bioDiff.hasTaxonomyDiff());
        Assert.assertEquals(10, (int) bioDiff.getTissueid_old());
        Assert.assertEquals(20, (int) bioDiff.getTissueid_new());

        //Check change of taxonomy but tissue remains
        biomaterial_edited = createBioMaterial(200, null, tissue1);

        diffs = comparator.compareMaterial(biomaterial_original, biomaterial_edited);
        bioDiff = comparator.getDifferenceOfType(diffs, BioMaterialDifference.class);
        Assert.assertNotNull(bioDiff);
        Assert.assertEquals(1, (int) bioDiff.getTaxonomyid_old());
        Assert.assertNull(bioDiff.getTaxonomyid_new());
        Assert.assertFalse(bioDiff.hasTissueDiff());

        Date mDate = new Date();
        bioDiff.initialise(biomaterial_original.getId(), user.getId(), mDate);

        Assert.assertEquals(user.getId(), bioDiff.getActorId());
        Assert.assertEquals((int)biomaterial_original.getId(),(int) bioDiff.getMaterialId());
        Assert.assertEquals(mDate, bioDiff.getModificationDate());

    }

    private Taxonomy createTaxonomy(int id) {
        Taxonomy taxo = new Taxonomy(id, new ArrayList<>(), new HazardInformation(), new StorageInformation(), new ArrayList<>(), user, new Date());
        taxo.setLevel(new TaxonomyLevel(999, "taxoLevel", 1));
        taxo.setACList(new ACList());
        return taxo;
    }

    private Tissue createTissue(int id, Taxonomy taxo) {
        Tissue tissue = new Tissue(id, new ArrayList<>(), taxo);
        tissue.setACList(new ACList());
        return tissue;
    }

    private BioMaterial createBioMaterial(int id, Taxonomy taxo, Tissue tissue) {
        BioMaterial biomaterial = new BioMaterial(id, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), taxo, tissue);
        biomaterial.setACList(new ACList());
        biomaterial.setOwner(user);

        return biomaterial;
    }
}
