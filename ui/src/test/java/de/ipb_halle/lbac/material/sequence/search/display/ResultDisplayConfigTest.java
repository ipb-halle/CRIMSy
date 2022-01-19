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
package de.ipb_halle.lbac.material.sequence.search.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author flange
 */
public class ResultDisplayConfigTest {
    @Test
    public void test_defaults() {
        ResultDisplayConfig config = new ResultDisplayConfig();

        assertTrue(config.getLineLength() > 0);
        assertTrue(config.getPrefixSpaces() > 0);
        assertTrue(config.getSuffixSpaces() > 0);
        assertTrue(config.getQueryLineIndexMultiplier() > 0);
        assertFalse(config.isQueryAlignmentCanReverse());
        assertTrue(config.getSubjectLineIndexMultiplier() > 0);
        assertFalse(config.isSubjectAlignmentCanReverse());
    }

    @Test
    public void test_settersAndGetters() {
        ResultDisplayConfig config = new ResultDisplayConfig();

        config.setLineLength(3489775);
        assertEquals(3489775, config.getLineLength());

        config.setPrefixSpaces(3489775);
        assertEquals(3489775, config.getPrefixSpaces());

        config.setSuffixSpaces(3489775);
        assertEquals(3489775, config.getSuffixSpaces());

        config.setQueryLineIndexMultiplier(3489775);
        assertEquals(3489775, config.getQueryLineIndexMultiplier());

        config.setQueryAlignmentCanReverse(true);
        assertTrue(config.isQueryAlignmentCanReverse());

        config.setSubjectLineIndexMultiplier(3489775);
        assertEquals(3489775, config.getSubjectLineIndexMultiplier());

        config.setSubjectAlignmentCanReverse(true);
        assertTrue(config.isSubjectAlignmentCanReverse());
    }

    @Test
    public void test_exceptions() {
        ResultDisplayConfig config = new ResultDisplayConfig();

        config.setLineLength(1);
        assertThrows(IllegalArgumentException.class, () -> config.setLineLength(0));
        assertThrows(IllegalArgumentException.class, () -> config.setLineLength(-1));

        config.setPrefixSpaces(1);
        assertThrows(IllegalArgumentException.class, () -> config.setPrefixSpaces(0));
        assertThrows(IllegalArgumentException.class, () -> config.setPrefixSpaces(-1));

        config.setSuffixSpaces(1);
        assertThrows(IllegalArgumentException.class, () -> config.setSuffixSpaces(0));
        assertThrows(IllegalArgumentException.class, () -> config.setSuffixSpaces(-1));

        config.setQueryLineIndexMultiplier(1);
        assertThrows(IllegalArgumentException.class, () -> config.setQueryLineIndexMultiplier(0));
        assertThrows(IllegalArgumentException.class, () -> config.setQueryLineIndexMultiplier(-1));

        config.setSubjectLineIndexMultiplier(1);
        assertThrows(IllegalArgumentException.class, () -> config.setSubjectLineIndexMultiplier(0));
        assertThrows(IllegalArgumentException.class, () -> config.setSubjectLineIndexMultiplier(-1));
    }
}