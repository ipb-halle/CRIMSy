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

import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class TaxonomyTreeControllerTest {

    @Test
    public void test001_addTaxonomy() {
        Taxonomy t1 = createTaxonomy(0);
        Taxonomy t2 = createTaxonomy(1);
        Taxonomy t3 = createTaxonomy(2);
        TaxonomyTreeController controller = new TaxonomyTreeController(null, null, null);
        controller.addAbsentTaxos(Arrays.asList(t1));
        Assert.assertEquals(1, controller.shownTaxonomies.size());
        controller.addAbsentTaxos(Arrays.asList(t1));
        Assert.assertEquals(1, controller.shownTaxonomies.size());
    }

    private Taxonomy createTaxonomy(int id) {

        return new Taxonomy(id, Arrays.asList(new MaterialName(String.valueOf("TAXO " + id), "de", 0)), new HazardInformation(), new StorageClassInformation(), new ArrayList<>(), null, new Date());
    }
}
