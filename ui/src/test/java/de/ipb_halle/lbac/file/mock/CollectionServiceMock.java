/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.file.mock;

import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.CollectionService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class CollectionServiceMock extends CollectionService {

    @Override
    public List<Collection> load(Map<String, Object> cmap) {
        Collection c = new Collection();
        c.setId(UUID.randomUUID());
        Node n = new Node();
        n.setId(UUID.randomUUID());
        c.setNode(n);

        c.setName("Coll1");
        return Arrays.asList(c);
    }
}
