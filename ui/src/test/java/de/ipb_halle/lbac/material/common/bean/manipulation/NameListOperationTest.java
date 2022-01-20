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
package de.ipb_halle.lbac.material.common.bean.manipulation;

import de.ipb_halle.lbac.material.common.bean.NameListOperation;
import de.ipb_halle.lbac.material.common.MaterialName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class NameListOperationTest {

    NameListOperation instance = new NameListOperation("de");

    @Test
    public void test01_isEnabled() {

        MaterialName m1 = new MaterialName("testName1", "de", 0);
        MaterialName m2 = new MaterialName("testName2", "en", 1);
        MaterialName m3 = new MaterialName("testName3", "de", 2);
        MaterialName m4 = new MaterialName("testName4", "en", 3);
        List<MaterialName> mn = Arrays.asList(m1, m2, m3, m4);
        List<MaterialName> mn2 = Arrays.asList(m1);

        Assert.assertFalse(
                "First Material should not be ranked up to HIGHEST",
                instance.isEnabled(m1, "HIGHEST", mn));
        Assert.assertFalse(
                "First Material should not be ranked up HIGHER",
                instance.isEnabled(m1, "HIGHER", mn));
        Assert.assertTrue(
                "Second Material should be ranked up to HIGHEST",
                instance.isEnabled(m2, "HIGHEST", mn));
        Assert.assertTrue(
                "Second Material should be ranked up HIGHER",
                instance.isEnabled(m2, "HIGHER", mn));

        Assert.assertFalse(
                "Fourth Material should not be ranked up to LOWEST",
                instance.isEnabled(m4, "LOWEST", mn));
        Assert.assertFalse(
                "Fourth Material should not be ranked up LOWEST",
                instance.isEnabled(m4, "LOWEST", mn));
        Assert.assertTrue(
                "Third Material should be ranked up to LOWER",
                instance.isEnabled(m3, "LOWER", mn));
        Assert.assertTrue(
                "Third Material should be ranked up LOWER",
                instance.isEnabled(m3, "LOWER", mn));

        Assert.assertTrue(
                "Third Material should be deletable",
                instance.isEnabled(m3, "DELETE", mn));
        Assert.assertFalse(
                "First and only Material should not be removable",
                instance.isEnabled(m1, "DELETE", mn2));
    }

    @Test
    public void test02_deleteName() {
        MaterialName m1 = new MaterialName("testName1", "de",0);
        MaterialName m2 = new MaterialName("testName2", "en",1);
        ArrayList<MaterialName> mn = new ArrayList<>(Arrays.asList(m1, m2));

        instance.deleteName(m1, mn);
        Assert.assertEquals(1, mn.size());

        instance.deleteName(m2, mn);
        Assert.assertEquals(1, mn.size());
    }

    @Test
    public void test03_addNewEmptyName() {
        MaterialName m1 = new MaterialName("testName1", "de",0);
        ArrayList<MaterialName> mn = new ArrayList<>(Arrays.asList(m1));
        instance.addNewEmptyName(mn);
        Assert.assertEquals(2, mn.size());
        instance.addNewEmptyName(mn);
        Assert.assertEquals(2, mn.size());
    }

    @Test
    public void test04_swapPositions() {
        MaterialName m1 = new MaterialName("testName1", "de",0);
        MaterialName m2 = new MaterialName("testName2", "en",1);
        MaterialName m3 = new MaterialName("testName3", "de",2);
        MaterialName m4 = new MaterialName("testName4", "en",3);
        ArrayList<MaterialName> mn = new ArrayList<>(Arrays.asList(m1, m2, m3, m4));

        Assert.assertEquals(m3, mn.get(2));
        instance.addOneRank(m3, mn);
        Assert.assertEquals(m3, mn.get(1));

        instance.substractOneRank(m3, mn);
        Assert.assertEquals(m3, mn.get(2));

        instance.setRankToHighest(m4, mn);
        Assert.assertEquals(m4, mn.get(0));

        instance.setRankToLowest(m4, mn);
        Assert.assertEquals(m4, mn.get(3));

    }
}
