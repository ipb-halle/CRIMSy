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
import static org.junit.Assert.assertFalse;

import org.junit.jupiter.api.Test;

public class TimeZonesBeanTest {
    @Test
    public void test001_getAvailableTimeZones() {
        TimeZonesBean bean = new TimeZonesBean();
        bean.init();
        assertFalse(bean.getAvailableTimeZones().isEmpty());

        // ToDo: Could also test the list sorting???
    }

    @Test
    public void test002_getDefaultTimeZone() {
        TimeZonesBean bean = new TimeZonesBean();
        bean.init();
        assertEquals("Europe/Berlin", bean.getDefaultTimeZone());
    }
}
