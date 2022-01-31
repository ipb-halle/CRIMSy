/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class TaxonomyTreeControllerTest {

    TaxonomyTreeController controller;

    @BeforeEach
    public void init() {
        controller = new TaxonomyTreeController(
                new Taxonomy(0, new ArrayList<>(), new HazardInformation(), new StorageInformation(), new ArrayList<>(), new User(), new Date()), null, null);
    }

    @Test
    public void test001_addTaxonomy() {
        Taxonomy t1 = createTaxonomy("Taxo1", 0, 1);
        Taxonomy t2 = createTaxonomy("Taxo2", 1, 2);
        Taxonomy t3 = createTaxonomy("Taxo3", 2, 3);
        controller.addAbsentTaxos(Arrays.asList(t1));
        Assert.assertEquals(1, controller.shownTaxonomies.size());
        controller.addAbsentTaxos(Arrays.asList(t1, t2, t3));
        Assert.assertEquals(3, controller.shownTaxonomies.size());
    }

    @Test
    public void test002_orderTaxonomies() {
        controller.addAbsentTaxo(createTaxonomy("BB", 1, 2)); // #3
        controller.addAbsentTaxo(createTaxonomy("AA", 2, 2)); // #2
        controller.addAbsentTaxo(createTaxonomy("AA", 4, 3)); // #4
        controller.addAbsentTaxo(createTaxonomy("CC", 3, 3)); // #5
        controller.addAbsentTaxo(createTaxonomy("ZZ", 0, 1)); // #1

        controller.reorderTaxonomies();

        Assert.assertEquals(5, controller.shownTaxonomies.size());
        Assert.assertEquals("ZZ", controller.shownTaxonomies.get(0).getFirstName());
        Assert.assertEquals("AA", controller.shownTaxonomies.get(1).getFirstName());
        Assert.assertEquals("BB", controller.shownTaxonomies.get(2).getFirstName());
        Assert.assertEquals("AA", controller.shownTaxonomies.get(3).getFirstName());
        Assert.assertEquals("CC", controller.shownTaxonomies.get(4).getFirstName());
    }

    @Test
    public void test003_replaceExistingTaxo() {
        controller.addAbsentTaxo(createTaxonomy("BB", 1, 2));

        controller.replaceTaxonomy(createTaxonomy("BB-edited", 1, 2));

        Assert.assertEquals(1, controller.shownTaxonomies.size());
        Assert.assertEquals("BB-edited", controller.shownTaxonomies.get(0).getFirstName());

    }

    private Taxonomy createTaxonomy(String name, int id, int rank) {
        Taxonomy t = new Taxonomy(id, Arrays.asList(new MaterialName(name, "de", 0)), new HazardInformation(), new StorageInformation(), new ArrayList<>(), null, new Date());
        t.setLevel(new TaxonomyLevel(rank, "Level " + rank, rank));
        return t;
    }
}
