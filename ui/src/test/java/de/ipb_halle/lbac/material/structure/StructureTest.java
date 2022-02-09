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
package de.ipb_halle.lbac.material.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.util.units.Quantity;

/**
 * @author flange
 */
class StructureTest {
    @Test
    void test_getAverageMolarMassAsQuantity() {
        Structure s = new Structure("NaCl", 58.44, 57.9586220, 1, null, 1);
        Quantity averageMolarMass = s.getAverageMolarMassAsQuantity();

        assertEquals(58.44, averageMolarMass.getValue(), 1e-3);
        assertEquals("g/mol", averageMolarMass.getUnit().toString());

        s.setAverageMolarMass(null);
        assertNull(s.getAverageMolarMassAsQuantity());
    }
}