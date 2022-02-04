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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.units.Quality;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
class CreateSolutionBeanTest {
    private CreateSolutionBean bean;

    @BeforeEach
    public void before() {
        bean = new CreateSolutionBean();
    }

    @Test
    public void test_gettersAndSetters() {
        assertNull(bean.getTargetConcentration());
        bean.setTargetConcentration(42d);
        assertEquals(42d, bean.getTargetConcentration());

        assertNull(bean.getTargetConcentrationUnit());
        bean.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());

        assertNull(bean.getAvailableConcentrationUnits());

        assertNull(bean.getTargetVolume());
        bean.setTargetVolume(10d);
        assertEquals(10d, bean.getTargetVolume());

        assertNull(bean.getTargetVolumeUnit());
        bean.setTargetVolumeUnit(Unit.getUnit("ml"));
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());

        assertNull(bean.getAvailableVolumeUnits());
    }

    @Test
    public void test_init_resetsValues() {
        // preparation
        bean.actionStartCreateSolution(new Item());
        bean.setTargetConcentration(42d);
        bean.setTargetVolume(10d);

        // assumptions
        assertNotNull(bean.getTargetConcentration());
        assertNotNull(bean.getTargetConcentrationUnit());
        assertThat(bean.getAvailableConcentrationUnits(), not(empty()));
        assertNotNull(bean.getTargetVolume());
        assertNotNull(bean.getTargetVolumeUnit());
        assertThat(bean.getAvailableVolumeUnits(), not(empty()));

        // execution
        bean.init();

        // assertions
        assertNull(bean.getTargetConcentration());
        assertNull(bean.getTargetConcentrationUnit());
        assertThat(bean.getAvailableConcentrationUnits(), empty());
        assertNull(bean.getTargetVolume());
        assertNull(bean.getTargetVolumeUnit());
        assertThat(bean.getAvailableVolumeUnits(), empty());
    }

    @Test
    public void test_actionStartCreateSolution_withSequence() {
        Sequence seq = new Sequence(null, null, null);
        Item i = new Item();
        i.setMaterial(seq);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());
        assertEquals(Unit.getUnitsOfQuality(Quality.MASS_CONCENTRATION), bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getUnitsOfQuality(Quality.VOLUME), bean.getAvailableVolumeUnits());
    }

    @Test
    public void test_actionStartCreateSolution_withStructureWithoutMolarMass() {
        Structure s = new Structure(null, null, null, 1, null, null);
        Item i = new Item();
        i.setMaterial(s);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("g/l"), bean.getTargetConcentrationUnit());
        assertEquals(Unit.getUnitsOfQuality(Quality.MASS_CONCENTRATION), bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getUnitsOfQuality(Quality.VOLUME), bean.getAvailableVolumeUnits());
    }

    @Test
    public void test_actionStartCreateSolution_withStructureWithMolarMass() {
        Structure s = new Structure(null, 300d, null, 1, null, null);
        Item i = new Item();
        i.setMaterial(s);

        bean.actionStartCreateSolution(i);

        assertNull(bean.getTargetConcentration());
        assertEquals(Unit.getUnit("mM"), bean.getTargetConcentrationUnit());
        List<Unit> allConcentrations = new ArrayList<>();
        allConcentrations.addAll(Unit.getUnitsOfQuality(Quality.MOLAR_CONCENTRATION));
        allConcentrations.addAll(Unit.getUnitsOfQuality(Quality.MASS_CONCENTRATION));
        assertEquals(allConcentrations, bean.getAvailableConcentrationUnits());
        assertNull(bean.getTargetVolume());
        assertEquals(Unit.getUnit("ml"), bean.getTargetVolumeUnit());
        assertEquals(Unit.getUnitsOfQuality(Quality.VOLUME), bean.getAvailableVolumeUnits());
    }
}