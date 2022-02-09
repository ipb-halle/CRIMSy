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
package de.ipb_halle.lbac.items.bean;

import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
@ExtendWith(ArquillianExtension.class)
class CreateSolutionBeanTest extends TestBase {
    private static final long serialVersionUID = 1L;
    private static final double DELTA = 1e-6;

    @Inject
    private CreateSolutionBean bean;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @Test
    public void test_gettersSettersAndDefaults() {
        assertThat(bean.getAvailableConcentrationUnits(), empty());

        assertThat(bean.getAvailableVolumeUnits(), empty());

        assertNull(bean.getTargetConcentration());
        bean.setTargetConcentration(42d);
        assertEquals(42d, bean.getTargetConcentration(), DELTA);

        assertNull(bean.getTargetConcentrationUnit());
        bean.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());

        assertNull(bean.getTargetVolume());
        bean.setTargetVolume(10d);
        assertEquals(10d, bean.getTargetVolume(), DELTA);

        assertNull(bean.getTargetVolumeUnit());
        bean.setTargetVolumeUnit(Unit.getUnit("ml"));
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());

        assertNull(bean.getTargetMass());

        assertNull(bean.getTargetMassUnit());

        assertNull(bean.getAvailableMassFromItem());

        assertNull(bean.getAvailableMassFromItemUnit());
    }

    /*
     * Tests for init()
     */
    @Test
    public void test_init_resetsValues() {
        // preparation
        bean.actionStartCreateSolution(new Item());
        bean.setTargetConcentration(42d);
        bean.setTargetVolume(10d);

        // assumptions
        assertThat(bean.getAvailableConcentrationUnits(), not(empty()));
        assertThat(bean.getAvailableVolumeUnits(), not(empty()));
        assertNotNull(bean.getTargetConcentration());
        assertNotNull(bean.getTargetConcentrationUnit());
        assertNotNull(bean.getTargetVolume());
        assertNotNull(bean.getTargetVolumeUnit());
        assertNull(bean.getTargetMass());
        assertNull(bean.getTargetMassUnit());
        assertNull(bean.getAvailableMassFromItem());
        assertNull(bean.getAvailableMassFromItemUnit());

        // execution
        bean.init();

        // assertions
        assertThat(bean.getAvailableConcentrationUnits(), empty());
        assertThat(bean.getAvailableVolumeUnits(), empty());
        assertNull(bean.getTargetConcentration());
        assertNull(bean.getTargetConcentrationUnit());
        assertNull(bean.getTargetVolume());
        assertNull(bean.getTargetVolumeUnit());
        assertNull(bean.getTargetMass());
        assertNull(bean.getTargetMassUnit());
        assertNull(bean.getAvailableMassFromItem());
        assertNull(bean.getAvailableMassFromItemUnit());
    }

    /*
     * Tests for actionStartCreateSolution()
     */
    @Test
    public void test_actionStartCreateSolution_withSequence() {
        Sequence seq = new Sequence(null, null, null);
        Item i = new Item();
        i.setMaterial(seq);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION), bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), bean.getAvailableVolumeUnits());
        assertNull(bean.getTargetMassUnit());
        assertNull(bean.getAvailableMassFromItem());
        assertNull(bean.getAvailableMassFromItemUnit());
    }

    @Test
    public void test_actionStartCreateSolution_withStructureWithoutMolarMass() {
        Structure s = new Structure(null, null, null, 1, null, null);
        Item i = new Item();
        i.setAmount(13d);
        i.setUnit(Unit.getUnit("kg"));
        i.setMaterial(s);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());
        assertEquals(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION), bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), bean.getAvailableVolumeUnits());
        assertEquals(Unit.getUnit("kg"), bean.getTargetMassUnit());
        assertEquals(13d, bean.getAvailableMassFromItem(), DELTA);
        assertEquals(Unit.getUnit("kg"), bean.getAvailableMassFromItemUnit());
    }

    @Test
    public void test_actionStartCreateSolution_withStructureWithMolarMass() {
        Structure s = new Structure(null, 300d, null, 1, null, null);
        Item i = new Item();
        i.setAmount(1.0);
        i.setUnit(Unit.getUnit("mg"));
        i.setMaterial(s);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("mM"), bean.getTargetConcentrationUnit());
        List<Unit> allConcentrations = new ArrayList<>();
        allConcentrations.addAll(Unit.getVisibleUnitsOfQuality(MOLAR_CONCENTRATION));
        allConcentrations.addAll(Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION));
        assertEquals(allConcentrations, bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getVisibleUnitsOfQuality(VOLUME), bean.getAvailableVolumeUnits());
        assertEquals(Unit.getUnit("mg"), bean.getTargetMassUnit());
        assertEquals(1.0, bean.getAvailableMassFromItem(), DELTA);
        assertEquals(Unit.getUnit("mg"), bean.getAvailableMassFromItemUnit());
    }

    /*
     * Tests for actionUpdateTargetMass()
     */
    @Test
    public void test_actionUpdateTargetMass_withoutTargetConcentration() {
        // preparation
        Structure s = new Structure(null, 300d, null, 1, null, null);
        Item i = new Item();
        i.setAmount(1500d);
        i.setUnit(Unit.getUnit("g"));
        i.setMaterial(s);
        bean.actionStartCreateSolution(i);
        bean.setTargetConcentration(null);
        bean.setTargetConcentrationUnit(Unit.getUnit("mM"));
        bean.setTargetVolume(10d);
        bean.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(bean.getTargetMass());

        // execution
        bean.actionUpdateTargetMass();

        // assertions
        assertNull(bean.getTargetMass());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withoutTargetVolume() {
        // preparation
        Structure s = new Structure(null, 300d, null, 1, null, null);
        Item i = new Item();
        i.setAmount(1500d);
        i.setUnit(Unit.getUnit("g"));
        i.setMaterial(s);
        bean.actionStartCreateSolution(i);
        bean.setTargetConcentration(100d);
        bean.setTargetConcentrationUnit(Unit.getUnit("mM"));
        bean.setTargetVolume(null);
        bean.setTargetVolumeUnit(Unit.getUnit("ml"));

        // assumptions
        assertNull(bean.getTargetMass());

        // execution
        bean.actionUpdateTargetMass();

        // assertions
        assertNull(bean.getTargetMass());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withoutMolarMass() {
        // preparation
        Structure s = new Structure(null, null, null, 1, null, null);
        Item i = new Item();
        i.setAmount(1500d);
        i.setUnit(Unit.getUnit("g"));
        i.setMaterial(s);
        bean.actionStartCreateSolution(i);
        bean.setTargetConcentration(100d);
        bean.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        bean.setTargetVolume(10d);
        bean.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(bean.getTargetMass());

        // execution
        bean.actionUpdateTargetMass();

        // assertions
        assertEquals(1000d, bean.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("g"), bean.getTargetMassUnit());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_withMolarMass() {
        // preparation
        Structure s = new Structure(null, 100d, null, 1, null, null);
        Item i = new Item();
        i.setAmount(1500d);
        i.setUnit(Unit.getUnit("g"));
        i.setMaterial(s);
        bean.actionStartCreateSolution(i);
        bean.setTargetConcentration(1d);
        bean.setTargetConcentrationUnit(Unit.getUnit("M"));
        bean.setTargetVolume(10d);
        bean.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(bean.getTargetMass());

        // execution
        bean.actionUpdateTargetMass();

        // assertions
        assertEquals(1000d, bean.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("g"), bean.getTargetMassUnit());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionUpdateTargetMass_targetMassTooHigh() {
        // preparation
        Structure s = new Structure(null, 100d, null, 1, null, null);
        Item i = new Item();
        i.setAmount(100d);
        i.setUnit(Unit.getUnit("g"));
        i.setMaterial(s);
        bean.actionStartCreateSolution(i);
        bean.setTargetConcentration(1d);
        bean.setTargetConcentrationUnit(Unit.getUnit("M"));
        bean.setTargetVolume(10d);
        bean.setTargetVolumeUnit(Unit.getUnit("l"));

        // assumptions
        assertNull(bean.getTargetMass());

        // execution
        bean.actionUpdateTargetMass();

        // assertions
        assertEquals(1000d, bean.getTargetMass(), DELTA);
        assertEquals(Unit.getUnit("g"), bean.getTargetMassUnit());
        assertEquals("itemCreateSolution_error_targetMassTooHigh", messagePresenter.getLastErrorMessage());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CreateSolutionBeanTest.war").addClass(CreateSolutionBean.class);
    }
}