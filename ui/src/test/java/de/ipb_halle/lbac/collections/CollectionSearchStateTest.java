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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.entity.Collection;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class CollectionSearchStateTest {

    @Test
    public void addCollectionsTest() {
        CollectionSearchState collSearchState = new CollectionSearchState();
        UUID id = UUID.randomUUID();
        Collection coll1 = new Collection();
        coll1.setId(id);
        coll1.setCountDocs(0L);
        coll1.setDescription("Test Collection");
        coll1.setIndexPath("none");
        coll1.setName("Test Collection");
        coll1.setNode(null);
        coll1.setOwner(null);
        coll1.setStoragePath("none");

        Collection coll2 = new Collection();
        coll2.setId(id);
        coll2.setCountDocs(0L);
        coll2.setDescription("Test Collection");
        coll2.setIndexPath("none");
        coll2.setName("Test Collection");
        coll2.setNode(null);
        coll2.setOwner(null);
        coll2.setStoragePath("none");

        collSearchState.addCollections(Arrays.asList(coll1, coll2));

        Assert.assertEquals("Only 1 collection must be present", 1, collSearchState.getCollections().size());
    }
}
