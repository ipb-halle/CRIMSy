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
package de.ipb_halle.lbac.items.bean.aliquot.createaliquot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.util.units.Quality;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
public class CreateAliquotStep1ControllerTest {
    private static final double DELTA = 1e-6;

    /*
     * Tests for getters after initialization
     */
    @Test
    public void test_getters_after_init() {
        Item parentItem = new Item();
        parentItem.setAmount(42.0);
        parentItem.setUnit(Unit.getUnit("µl"));

        CreateAliquotStep1Controller controller = new CreateAliquotStep1Controller(parentItem);

        assertEquals(Unit.getVisibleUnitsOfQuality(Quality.VOLUME), controller.getAvailableAmountUnits());
        assertEquals(42.0, controller.getAvailableAmountFromItem(), DELTA);
        assertEquals("µl", controller.getAvailableAmountFromItemUnit().toString());
        assertNull(controller.getAmount());
        assertEquals("µl", controller.getAmountUnit().toString());
    }

    /*
     * Tests for isAmountGreaterThanItemAmount()
     */
    @Test
    public void test_isAmountGreaterThanItemAmount() {
        Item parentItem = new Item();
        parentItem.setAmount(5.0);
        parentItem.setUnit(Unit.getUnit("kg"));

        CreateAliquotStep1Controller controller = new CreateAliquotStep1Controller(parentItem);

        controller.setAmount(null);
        controller.setAmountUnit(null);
        assertTrue(controller.isAmountGreaterThanItemAmount());

        controller.setAmount(10000.0);
        controller.setAmountUnit(Unit.getUnit("g"));
        assertTrue(controller.isAmountGreaterThanItemAmount());

        controller.setAmount(5000.0);
        controller.setAmountUnit(Unit.getUnit("g"));
        assertFalse(controller.isAmountGreaterThanItemAmount());

        controller.setAmount(1.0);
        controller.setAmountUnit(Unit.getUnit("kg"));
        assertFalse(controller.isAmountGreaterThanItemAmount());
    }

    /*
     * Tests for getAmountAsQuantity()
     */
    @Test
    public void test_getAmountAsQuantity() {
        Item parentItem = new Item();
        parentItem.setAmount(5.0);
        parentItem.setUnit(Unit.getUnit("kg"));

        CreateAliquotStep1Controller controller = new CreateAliquotStep1Controller(parentItem);

        controller.setAmount(null);
        controller.setAmountUnit(null);
        assertNull(controller.getAmountAsQuantity());

        controller.setAmount(1.0);
        controller.setAmountUnit(Unit.getUnit("kg"));
        Quantity q = controller.getAmountAsQuantity();
        assertEquals(1.0, q.getValue(), DELTA);
        assertEquals("kg", q.getUnit().toString());
    }
}
