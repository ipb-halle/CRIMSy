/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.timezone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

import org.junit.Test;

public class ZoneIdDisplayWrapperTest {
    @Test
    public void test001_getId() {
        // This can be tested with all available time zones.
        for (String zone : ZoneId.getAvailableZoneIds()) {
            ZoneIdDisplayWrapper wrapper = new ZoneIdDisplayWrapper(
                    ZoneId.of(zone));
            assertEquals(zone, wrapper.getId());
        }
    }

    @Test
    public void test002_getDisplayName() {
        ZoneIdDisplayWrapper wrapper = new ZoneIdDisplayWrapper(
                ZoneId.of("UTC"));
        assertEquals("Coordinated Universal Time",
                wrapper.getDisplayName(Locale.ENGLISH));
        assertEquals("Koordinierte Universalzeit",
                wrapper.getDisplayName(Locale.GERMAN));

        wrapper = new ZoneIdDisplayWrapper(ZoneId.of("Europe/Berlin"));
        assertEquals("Central European Time",
                wrapper.getDisplayName(Locale.ENGLISH));
        assertEquals("Mitteleuropäische Zeit",
                wrapper.getDisplayName(Locale.GERMAN));
    }

    @Test
    public void test003_getOffset() {
        ZoneIdDisplayWrapper wrapper = new ZoneIdDisplayWrapper(
                ZoneId.of("UTC"));
        // ZoneOffset overrides equals, so these comparisons should be fine.
        assertEquals(ZoneOffset.of("+00:00"), wrapper.getOffset());

        /*
         * This will fail as soon as we switch between CEST and CET. Not good
         * for reproducible tests.
         */
        // wrapper = new ZoneIdDisplayWrapper(ZoneId.of("Europe/Berlin"));
        // assertEquals(ZoneOffset.of("+02:00"), wrapper.getOffset());

        // Kenya never used DST and hopefully never will. ;)
        wrapper = new ZoneIdDisplayWrapper(ZoneId.of("Africa/Nairobi"));
        assertEquals(ZoneOffset.of("+03:00"), wrapper.getOffset());
    }

    @Test
    public void test004_getOffsetString() {
        ZoneIdDisplayWrapper wrapper = new ZoneIdDisplayWrapper(
                ZoneId.of("UTC"));
        assertEquals("+00:00", wrapper.getOffsetString());

        wrapper = new ZoneIdDisplayWrapper(ZoneId.of("Africa/Nairobi"));
        assertEquals("+03:00", wrapper.getOffsetString());
    }

    @Test
    public void test005_constructorNPE() {
        assertThrows(NullPointerException.class,
                () -> new ZoneIdDisplayWrapper(null));
    }
}