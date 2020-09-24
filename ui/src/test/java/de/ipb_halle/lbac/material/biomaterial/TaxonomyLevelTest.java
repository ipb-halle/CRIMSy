/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.material.biomaterial;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class TaxonomyLevelTest {

    @Test
    public void test001_equalsAndHash() {
        TaxonomyLevel level_1 = new TaxonomyLevel(1, "level1", 100);
        TaxonomyLevel level_2 = new TaxonomyLevel(2, "level2", 200);
        TaxonomyLevel level_3 = new TaxonomyLevel(3, "level2", 300);
        TaxonomyLevel level_1_copy = new TaxonomyLevel(1, "level1", 100);

        Assert.assertFalse(level_1.equals(level_2));
        Assert.assertFalse(level_1.equals(level_3));
        Assert.assertTrue(level_1.equals(level_1_copy));

        Assert.assertNotEquals(level_1.hashCode(), level_2.hashCode());
        Assert.assertNotEquals(level_1.hashCode(), level_3.hashCode());
        Assert.assertEquals(level_1.hashCode(), level_1_copy.hashCode());

    }
}
