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
package de.ipb_halle.lbac.material.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class MaterialSearchRequestBuilderTest {

    User user;

    @Before
    public void init() {
        user = new User();
        user.setId(1);
        user.setName("userName");
    }

    @Test
    public void testSearchRequestBuild() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(user, 5, 25);
        builder.addID(1);
        builder.addIndexName("Index1");
        builder.addProject("project1");
        builder.addType(MaterialType.BIOMATERIAL);
        builder.addSubMolecule("MOL");
        SearchRequest request = builder.buildSearchRequest();
        Assert.assertEquals(user.getId(), request.getUser().getId());
        Assert.assertEquals(5, request.getFirstResult());
        Assert.assertEquals(25, request.getMaxResults());
        MaterialEntityGraphBuilder entityGraphBuilder = new MaterialEntityGraphBuilder();
        SqlBuilder sqlBuilder = new SqlBuilder(entityGraphBuilder.buildEntityGraph(request.getCondition()));

        String sql = sqlBuilder.query(request.getCondition());
        Assert.assertNotNull(sql);
    }
}
