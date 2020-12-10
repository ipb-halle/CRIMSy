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
package de.ipb_halle.lbac.items.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import org.apache.sis.internal.metadata.sql.SQLBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemSearchRequestBuilderTest {

    private User user;

    @Before
    public void init() {
        user = new User();
        user.setId(1);
        user.setLogin("test");
        user.setName("testuser");

    }

    @Test
    public void test001_createItemSearchRequest() {
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(user, 0, 5);
        builder.addIndexName("Wasser");
        SearchRequest request = builder.buildSearchRequest();

    }
}
