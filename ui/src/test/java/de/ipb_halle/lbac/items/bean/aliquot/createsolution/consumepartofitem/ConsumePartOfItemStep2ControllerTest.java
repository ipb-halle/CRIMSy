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
package de.ipb_halle.lbac.items.bean.aliquot.createsolution.consumepartofitem;

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

/**
 * @author flange
 */
public class ConsumePartOfItemStep2ControllerTest {
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
        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(null, null, null);

        assertEquals(5, controller.getErrorMargin());
        assertNull(controller.getWeight());
        assertNull(controller.getWeightUnit());
    }

    /*
     * Tests for init()
     */
    @Test
    public void test_init() {
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(new Item(),
                messagePresenter);
        step1Controller.setTargetMassUnit(Unit.getUnit("µg"));
        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(step1Controller, null, null);

        controller.init();
        assertEquals("µg", controller.getWeightUnit().toString());

        controller.setWeightUnit(Unit.getUnit("g"));
        controller.init();
        assertEquals("g", controller.getWeightUnit().toString());
    }

    /*
     * Tests for actionWeightChange()
     */
    @Test
    public void test_actionWeightChange() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(null, item,
                messagePresenter);

        // weight is lower than item amount
        controller.setWeight(10000.0);
        controller.setWeightUnit(Unit.getUnit("mg"));
        controller.actionWeightChange();
        assertNull(messagePresenter.getLastErrorMessage());

        // weight equals item amount
        controller.setWeight(0.1);
        controller.setWeightUnit(Unit.getUnit("kg"));
        controller.actionWeightChange();
        assertNull(messagePresenter.getLastErrorMessage());

        // weight is too high
        controller.setWeight(10.0);
        controller.setWeightUnit(Unit.getUnit("kg"));
        controller.actionWeightChange();
        assertEquals("itemCreateSolution_error_weightTooHigh", messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for getTargetMassPlusMargin()
     */
    @Test
    public void test_getTargetMassPlusMargin() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, messagePresenter);
        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(step1Controller, null, null);

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

        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, messagePresenter);
        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(step1Controller, null, null);

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
     * Tests for isWeightGreaterThanItemMass()
     */
    @Test
    public void test_isWeightGreaterThanItemMass() {
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));

        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(null, item, null);

        // greater
        controller.setWeight(10.0);
        controller.setWeightUnit(Unit.getUnit("kg"));
        assertTrue(controller.isWeightGreaterThanItemMass());

        // equal
        controller.setWeight(0.1);
        controller.setWeightUnit(Unit.getUnit("kg"));
        assertFalse(controller.isWeightGreaterThanItemMass());

        // lesser
        controller.setWeight(10.0);
        controller.setWeightUnit(Unit.getUnit("mg"));
        assertFalse(controller.isWeightGreaterThanItemMass());
    }

    /*
     * Tests for getWeightAsQuantity()
     */
    @Test
    public void test_getWeightAsQuantity() {
        ConsumePartOfItemStep2Controller controller = new ConsumePartOfItemStep2Controller(null, null, null);

        controller.setWeight(null);
        controller.setWeightUnit(null);
        assertNull(controller.getWeightAsQuantity());

        controller.setWeight(10.0);
        controller.setWeightUnit(null);
        assertNull(controller.getWeightAsQuantity());

        controller.setWeight(null);
        controller.setWeightUnit(Unit.getUnit("mg"));
        assertNull(controller.getWeightAsQuantity());

        controller.setWeight(10.0);
        controller.setWeightUnit(Unit.getUnit("mg"));
        Quantity q = controller.getWeightAsQuantity();
        assertEquals(10.0, q.getValue(), DELTA);
        assertEquals("mg", q.getUnit().toString());
    }
}