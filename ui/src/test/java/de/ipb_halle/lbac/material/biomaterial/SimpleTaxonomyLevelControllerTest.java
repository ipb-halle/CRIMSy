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
package de.ipb_halle.lbac.material.biomaterial;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class SimpleTaxonomyLevelControllerTest {

    @Test
    public void test001_getLevels() {
        List<TaxonomyLevel> levels = new ArrayList<>();
        levels.add(new TaxonomyLevel(0, "level 1", 1));
        levels.add(new TaxonomyLevel(1, "level 2", 2));
        levels.add(new TaxonomyLevel(2, "level 4", 4));
        SimpleTaxonomyLevelController controller = new SimpleTaxonomyLevelController(levels);
        Assert.assertEquals(3, controller.getLevels().size());

    }
}
