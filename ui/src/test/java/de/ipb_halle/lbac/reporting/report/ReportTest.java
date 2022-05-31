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
package de.ipb_halle.lbac.reporting.report;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * @author flange
 */
public class ReportTest {
    @Test
    public void test_getters_and_setters() {
        ReportEntity entity = new ReportEntity();
        entity.setId(42).setContext("abc").setName("def").setSource("ghi");
        Report report = new Report(entity);

        assertEquals(Integer.valueOf(42), report.getId());
        assertEquals("def", report.getName());
        assertEquals("ghi", report.getSource());

        report.setName("new name");
        assertEquals("new name", report.getName());
    }

    @Test
    public void test_createEntity() {
        ReportEntity entity = new ReportEntity();
        entity.setId(42).setContext("abc").setName("def").setSource("ghi");
        Report report = new Report(entity);
        report.setName("new name");

        ReportEntity newEntity = report.createEntity();

        assertEquals(Integer.valueOf(42), newEntity.getId());
        assertEquals("abc", newEntity.getContext());
        assertEquals("new name", newEntity.getName());
        assertEquals("ghi", newEntity.getSource());
    }
}