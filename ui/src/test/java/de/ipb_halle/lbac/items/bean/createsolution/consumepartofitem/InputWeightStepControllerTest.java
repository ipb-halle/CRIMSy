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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

public class InputWeightStepControllerTest {
    private static final double DELTA = 1e-6;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @BeforeEach
    public void before() {
        messagePresenter.resetMessages();
    }

    /*
     * Initial field values after construction
     */
    @Test
    public void test_initialValues() {
        InputWeightStepController controller = new InputWeightStepController(null, null, null);

        assertEquals(5, controller.getErrorMargin());
        assertNull(controller.getWeigh());
        assertNull(controller.getWeighUnit());
    }

    /*
     * Tests for init()
     */
    @Test
    public void test_init() {
        InputConcentrationAndVolumeStepController step1Controller = new InputConcentrationAndVolumeStepController(
                new Item(), messagePresenter);
        step1Controller.setTargetMassUnit(Unit.getUnit("µg"));
        InputWeightStepController controller = new InputWeightStepController(step1Controller, null, null);

        controller.init();
        assertEquals("µg", controller.getWeighUnit().toString());

        controller.setWeighUnit(Unit.getUnit("g"));
        controller.init();
        assertEquals("g", controller.getWeighUnit().toString());
    }

    /*
     * Tests for actionWeighChange()
     */
    @Test
    public void test_actionWeighChange() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        InputWeightStepController controller = new InputWeightStepController(null, item, messagePresenter);

        // weigh is lower than item amount
        controller.setWeigh(10000.0);
        controller.setWeighUnit(Unit.getUnit("mg"));
        controller.actionWeighChange();
        assertNull(messagePresenter.getLastErrorMessage());

        // weigh equals item amount
        controller.setWeigh(0.1);
        controller.setWeighUnit(Unit.getUnit("kg"));
        controller.actionWeighChange();
        assertNull(messagePresenter.getLastErrorMessage());

        // weigh is too high
        controller.setWeigh(10.0);
        controller.setWeighUnit(Unit.getUnit("kg"));
        controller.actionWeighChange();
        assertEquals("itemCreateSolution_error_weighTooHigh", messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for getTargetMassPlusMargin()
     */
    @Test
    public void test_getTargetMassPlusMargin() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        InputConcentrationAndVolumeStepController step1Controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);
        InputWeightStepController controller = new InputWeightStepController(step1Controller, null, null);

        // Without target mass, there can't be any margin.
        assertNull(step1Controller.getTargetMass());
        assertNull(controller.getTargetMassPlusMargin());

        // prepare step1Controller with a target mass
        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step1Controller.setTargetVolume(10.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("l"));
        step1Controller.setTargetMassUnit(Unit.getUnit("g"));
        step1Controller.actionOnChangeTargetMassUnit();
        assertEquals(100.0, step1Controller.getTargetMass(), DELTA);
        assertEquals("g", step1Controller.getTargetMassUnit().toString());

        controller.setErrorMargin(10);
        assertEquals(110.0, controller.getTargetMassPlusMargin(), DELTA);

        controller.setErrorMargin(5);
        assertEquals(105.0, controller.getTargetMassPlusMargin(), DELTA);
    }

    /*
     * Tests for getTargetMassMinusMargin()
     */
    @Test
    public void test_getTargetMassMinusMargin() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        InputConcentrationAndVolumeStepController step1Controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);
        InputWeightStepController controller = new InputWeightStepController(step1Controller, null, null);

        // Without target mass, there can't be any margin.
        assertNull(step1Controller.getTargetMass());
        assertNull(controller.getTargetMassMinusMargin());

        // prepare step1Controller with a target mass
        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step1Controller.setTargetVolume(10.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("l"));
        step1Controller.setTargetMassUnit(Unit.getUnit("g"));
        step1Controller.actionOnChangeTargetMassUnit();
        assertEquals(100.0, step1Controller.getTargetMass(), DELTA);
        assertEquals("g", step1Controller.getTargetMassUnit().toString());

        controller.setErrorMargin(10);
        assertEquals(90.0, controller.getTargetMassMinusMargin(), DELTA);

        controller.setErrorMargin(5);
        assertEquals(95.0, controller.getTargetMassMinusMargin(), DELTA);
    }

    /*
     * Tests for isWeighGreaterThanItemMass()
     */
    @Test
    public void test_isWeighGreaterThanItemMass() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        InputWeightStepController controller = new InputWeightStepController(null, item, null);

        // greater
        controller.setWeigh(10.0);
        controller.setWeighUnit(Unit.getUnit("kg"));
        assertTrue(controller.isWeighGreaterThanItemMass());

        // equal
        controller.setWeigh(0.1);
        controller.setWeighUnit(Unit.getUnit("kg"));
        assertFalse(controller.isWeighGreaterThanItemMass());

        // lesser
        controller.setWeigh(10.0);
        controller.setWeighUnit(Unit.getUnit("mg"));
        assertFalse(controller.isWeighGreaterThanItemMass());
    }

    /*
     * Tests for getWeighAsQuantity()
     */
    @Test
    public void test_getWeighAsQuantity() {
        InputWeightStepController controller = new InputWeightStepController(null, null, null);

        controller.setWeigh(null);
        controller.setWeighUnit(null);
        assertNull(controller.getWeighAsQuantity());

        controller.setWeigh(10.0);
        controller.setWeighUnit(null);
        assertNull(controller.getWeighAsQuantity());

        controller.setWeigh(null);
        controller.setWeighUnit(Unit.getUnit("mg"));
        assertNull(controller.getWeighAsQuantity());

        controller.setWeigh(10.0);
        controller.setWeighUnit(Unit.getUnit("mg"));
        Quantity q = controller.getWeighAsQuantity();
        assertEquals(10.0, q.getValue(), DELTA);
        assertEquals("mg", q.getUnit().toString());
    }
}