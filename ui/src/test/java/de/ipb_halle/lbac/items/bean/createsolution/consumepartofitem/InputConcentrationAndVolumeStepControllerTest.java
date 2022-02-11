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

import static de.ipb_halle.lbac.util.units.Quality.MASS;
import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
class InputConcentrationAndVolumeStepControllerTest {
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
    public void test_initialValues_withItemWitoutAmount() {
        Sequence seq = new Sequence(null, null, null);
        Item item = new Item();
        item.setMaterial(seq);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                null);

        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), controller.getAvailableVolumeUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS), controller.getAvailableMassUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION), controller.getAvailableConcentrationUnits());

        assertNull(controller.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), controller.getTargetConcentrationUnit());
        assertNull(controller.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), controller.getTargetVolumeUnit());
        assertNull(controller.getTargetMass());
        assertNull(controller.getTargetMassUnit());
        assertNull(controller.getAvailableMassFromItem());
        assertNull(controller.getAvailableMassFromItemUnit());

        assertFalse(controller.isUserChangedMassUnit());
    }

    @Test
    public void test_initialValues_withSequenceAsMaterial() {
        Sequence seq = new Sequence(null, null, null);
        Item item = new Item();
        item.setAmount(13.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(seq);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                null);

        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), controller.getAvailableVolumeUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS), controller.getAvailableMassUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION), controller.getAvailableConcentrationUnits());

        assertNull(controller.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), controller.getTargetConcentrationUnit());
        assertNull(controller.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), controller.getTargetVolumeUnit());
        assertNull(controller.getTargetMass());
        assertEquals(Unit.getUnit("g"), controller.getTargetMassUnit());
        assertEquals(13.0, controller.getAvailableMassFromItem(), DELTA);
        assertEquals(Unit.getUnit("g"), controller.getAvailableMassFromItemUnit());

        assertFalse(controller.isUserChangedMassUnit());
    }

    @Test
    public void test_initialValues_withStructureWithoutMolarMass() {
        Structure s = new Structure(null, null, null, 1, null, null);
        Item item = new Item();
        item.setAmount(42.0);
        item.setUnit(Unit.getUnit("mg"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                null);

        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), controller.getAvailableVolumeUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS), controller.getAvailableMassUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION), controller.getAvailableConcentrationUnits());

        assertNull(controller.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), controller.getTargetConcentrationUnit());
        assertNull(controller.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), controller.getTargetVolumeUnit());
        assertNull(controller.getTargetMass());
        assertEquals(Unit.getUnit("mg"), controller.getTargetMassUnit());
        assertEquals(42.0, controller.getAvailableMassFromItem(), DELTA);
        assertEquals(Unit.getUnit("mg"), controller.getAvailableMassFromItemUnit());

        assertFalse(controller.isUserChangedMassUnit());
    }

    @Test
    public void test_initialValues_withStructureWithMolarMass() {
        Structure s = new Structure(null, 300.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(10.0);
        item.setUnit(Unit.getUnit("kg"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                null);

        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), controller.getAvailableVolumeUnits());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS), controller.getAvailableMassUnits());
        List<Unit> allConcentrations = new ArrayList<>();
        allConcentrations.addAll(Unit.getVisibleUnitsOfQuality(MOLAR_CONCENTRATION));
        allConcentrations.addAll(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION));
        assertEquals(allConcentrations, controller.getAvailableConcentrationUnits());

        assertNull(controller.getTargetConcentration());
        assertEquals(Unit.getUnit("mM"), controller.getTargetConcentrationUnit());
        assertNull(controller.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), controller.getTargetVolumeUnit());
        assertNull(controller.getTargetMass());
        assertEquals(Unit.getUnit("kg"), controller.getTargetMassUnit());
        assertEquals(10.0, controller.getAvailableMassFromItem(), DELTA);
        assertEquals(Unit.getUnit("kg"), controller.getAvailableMassFromItemUnit());

        assertFalse(controller.isUserChangedMassUnit());
    }

    /*
     * Tests for actionUpdateTargetMass()
     */
    @Test
    public void test_actionUpdateTargetMass_withoutTargetConcentration() {
        // preparation
        Structure s = new Structure(null, 300.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(1500.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(null);
        controller.setTargetConcentrationUnit(Unit.getUnit("mM"));
        controller.setTargetVolume(10.0);
        controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("g", controller.getTargetMassUnit().toString());
        assertFalse(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertNull(controller.getTargetMass());
        assertEquals("g", controller.getTargetMassUnit().toString());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withoutTargetVolume() {
        // preparation
        Structure s = new Structure(null, 300.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(1500.0);
        item.setUnit(Unit.getUnit("kg"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(100.0);
        controller.setTargetConcentrationUnit(Unit.getUnit("mM"));
        controller.setTargetVolume(null);
        controller.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("kg", controller.getTargetMassUnit().toString());
        assertFalse(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertNull(controller.getTargetMass());
        assertEquals("kg", controller.getTargetMassUnit().toString());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withoutMolarMass() {
        // preparation
        Structure s = new Structure(null, null, null, 1, null, null);
        Item item = new Item();
        item.setAmount(1500.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(100.0);
        controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        controller.setTargetVolume(10.0);
        controller.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("g", controller.getTargetMassUnit().toString());
        assertFalse(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertEquals(1.0, controller.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("kg"), controller.getTargetMassUnit());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withMolarMass() {
        // preparation
        Structure s = new Structure(null, 100.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(1500.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(1.0);
        controller.setTargetConcentrationUnit(Unit.getUnit("M"));
        controller.setTargetVolume(10.0);
        controller.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("g", controller.getTargetMassUnit().toString());
        assertFalse(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertEquals(1.0, controller.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("kg"), controller.getTargetMassUnit());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_targetMassTooHigh() {
        // preparation
        Structure s = new Structure(null, 100.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(1.0);
        controller.setTargetConcentrationUnit(Unit.getUnit("M"));
        controller.setTargetVolume(10.0);
        controller.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("g", controller.getTargetMassUnit().toString());
        assertFalse(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertEquals(1.0, controller.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("kg"), controller.getTargetMassUnit());
        assertEquals("itemCreateSolution_error_targetMassTooHigh", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_afterUserChangedTheTargetUnit() {
        // preparation
        Structure s = new Structure(null, 100.0, null, 1, null, null);
        Item item = new Item();
        item.setAmount(10000.0);
        item.setUnit(Unit.getUnit("g"));
        item.setMaterial(s);

        InputConcentrationAndVolumeStepController controller = new InputConcentrationAndVolumeStepController(item,
                messagePresenter);

        controller.setTargetConcentration(1.0);
        controller.setTargetConcentrationUnit(Unit.getUnit("M"));
        controller.setTargetVolume(10.0);
        controller.setTargetVolumeUnit(Unit.getUnit("l"));

        controller.setTargetMassUnit(Unit.getUnit("mg"));
        controller.setUserChangedMassUnit(true);

        // assumptions
        assertNull(controller.getTargetMass());
        assertEquals("mg", controller.getTargetMassUnit().toString());
        assertTrue(controller.isUserChangedMassUnit());

        // execution
        controller.actionUpdateTargetMass();

        // assertions
        assertEquals(1e6, controller.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("mg"), controller.getTargetMassUnit());
        assertNull(messagePresenter.getLastErrorMessage());
    }
}