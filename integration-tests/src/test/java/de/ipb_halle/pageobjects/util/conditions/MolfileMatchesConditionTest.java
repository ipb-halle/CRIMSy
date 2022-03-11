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
package de.ipb_halle.pageobjects.util.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * @author flange
 */
public class MolfileMatchesConditionTest {
    @Test
    public void test_molfilesMatch() throws Exception {
        String benzene = readResourceFile("molfiles/Benzene.mol");
        String caffeine1 = readResourceFile("molfiles/CaffeineFromChemSpider.mol");
        String caffeine2 = readResourceFile("molfiles/CaffeineFromPubChem.mol");
        MolfileMatchesCondition condition = new MolfileMatchesCondition(null, null);

        assertTrue(condition.molfilesMatch(benzene, benzene));
        assertFalse(condition.molfilesMatch(benzene, caffeine1));
        assertFalse(condition.molfilesMatch(caffeine1, benzene));
        assertTrue(condition.molfilesMatch(caffeine1, caffeine2));
        assertTrue(condition.molfilesMatch(caffeine2, caffeine1));
    }

    private String readResourceFile(String resourceFile) throws Exception {
        return new String(
                Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(resourceFile).toURI())));
    }
}