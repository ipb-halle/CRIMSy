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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
public class ConsumePartOfItemStep3ControllerTest {
    private static final double DELTA = 1e-6;

    /*
     * Tests for getters/setters
     */
    @Test
    public void test_gettersAndSettersAndDefaults() {
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(null, null, null);

        assertNull(controller.getDispensedVolume());
        assertNull(controller.getSolvent());

        controller.setDispensedVolume(42.0);
        assertEquals(42.0, controller.getDispensedVolume(), DELTA);

        Solvent solvent = new Solvent();
        controller.setSolvent(solvent);
        assertSame(solvent, controller.getSolvent());
    }

    /*
     * Tests for init()
     */
    @Test
    public void test_init_itemWithoutMolarMass() {
        // preparation
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(controller.getDispensedVolume());

        // execution
        controller.init();

        // assertions
        assertEquals(0.5, controller.getDispensedVolume(), DELTA);
    }

    @Test
    public void test_init_itemWithMolarMass() {
        // preparation
        Structure s = new Structure(null, 200.0, null, 1, null, null);
        Item item = new Item();
        item.setMaterial(s);
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("mM"));
        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(controller.getDispensedVolume());

        // execution
        controller.init();

        // assertions
        assertEquals(2.5, controller.getDispensedVolume(), DELTA);
    }

    /*
     * Tests for getVolumeToDispense()
     */
    @Test
    public void test_getVolumeToDispense_itemWithoutMolarMass() {
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        assertEquals(0.5, controller.getVolumeToDispense(), DELTA);
    }

    @Test
    public void test_getVolumeToDispense_itemWithMolarMass() {
        Structure s = new Structure(null, 200.0, null, 1, null, null);
        Item item = new Item();
        item.setMaterial(s);
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step1Controller.setTargetConcentration(10.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("mM"));
        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assertions
        assertEquals(2.5, controller.getVolumeToDispense(), DELTA);
    }

    /*
     * Tests for getFinalConcentration()
     */
    @Test
    public void test_getFinalConcentration_itemWithoutMolarMass() {
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        controller.setDispensedVolume(100.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("µl"));
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));

        assertEquals(50, controller.getFinalConcentration(), DELTA);
    }

    @Test
    public void test_getFinalConcentration_itemWithMolarMass() {
        Structure s = new Structure(null, 200.0, null, 1, null, null);
        Item item = new Item();
        item.setMaterial(s);
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep2Controller step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, item,
                null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller,
                step2Controller, item);

        step2Controller.setWeight(5.0);
        step2Controller.setWeightUnit(Unit.getUnit("mg"));
        controller.setDispensedVolume(100.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("µl"));
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("mM"));

        // assertions
        assertEquals(250.0, controller.getFinalConcentration(), DELTA);
    }

    /*
     * Tests for getDispensedVolumeAsQuantity()
     */
    @Test
    public void test_getDispensedVolumeAsQuantity() {
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep3Controller controller = new ConsumePartOfItemStep3Controller(step1Controller, null, null);

        controller.setDispensedVolume(null);
        step1Controller.setTargetVolumeUnit(null);
        assertNull(controller.getDispensedVolumeAsQuantity());

        controller.setDispensedVolume(42.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));
        Quantity dispensedVolume = controller.getDispensedVolumeAsQuantity();
        assertEquals(42.0, dispensedVolume.getValue(), DELTA);
        assertEquals("ml", dispensedVolume.getUnit().toString());
    }
}
