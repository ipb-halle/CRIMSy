/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Quantity;

public class ItemUtilsTest {
    private static final double DELTA = 1e-6;

    /*
     * Tests for molarMassFromItem(Item)
     */
    @Test
    public void test_molarMassFromItem_withNoMaterial() {
        Item item = new Item();

        assertNull(ItemUtils.molarMassFromItem(item));
    }

    @Test
    public void test_molarMassFromItem_withSequenceAsMaterial() {
        Sequence seq = new Sequence(null, null, null);
        Item item = new Item();
        item.setMaterial(seq);

        assertNull(ItemUtils.molarMassFromItem(item));
    }

    @Test
    public void test_molarMassFromItem_withStructureWithoutMolarMass() {
        Structure s = new Structure(null, null, null, 1, null, null);
        Item item = new Item();
        item.setMaterial(s);

        assertNull(ItemUtils.molarMassFromItem(item));
    }

    @Test
    public void test_molarMassFromItem_withStructureWithMolarMass() {
        Structure s = new Structure(null, 42.0, null, 1, null, null);
        Item item = new Item();
        item.setMaterial(s);

        Quantity molarMass = ItemUtils.molarMassFromItem(item);
        assertEquals(42.0, molarMass.getValue(), DELTA);
        assertEquals("g/mol", molarMass.getUnit().toString());
    }
}