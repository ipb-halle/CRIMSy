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
package de.ipb_halle.lbac.material.sequence.search.service;

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SequenceSearchConditionBuilderTest {

    @Test
    public void test001_checkConditionBuilding() {
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(createUser(), 0, 2);
        requestBuilder.addMaterialType(MaterialType.SEQUENCE);
        requestBuilder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA,1);
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(graphBuilder.buildEntityGraph(true), "materials");
        List<Condition> cons = builder.getMaterialCondition(requestBuilder.build(), true);

        Assert.assertTrue(checkMaterialType(cons));
       
        Assert.assertTrue(checkSequenceType(cons));
    }

    @Test
    public void test002_checkSqlBuilding() {

        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(createUser(), 0, 2);
        requestBuilder.addMaterialType(MaterialType.SEQUENCE);
        requestBuilder.setSequenceInformation("AAA", SequenceType.DNA, SequenceType.DNA,1);
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();
        EntityGraph graph = graphBuilder.buildEntityGraph(true);

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(graph, "materials");
        Condition condition = builder.convertRequestToCondition(requestBuilder.build(), ACPermission.permREAD);
        SqlBuilder sqlBuilder = new SqlBuilder(graph);
        String sql = sqlBuilder.query(condition);

        Assert.assertNotNull(sql);
    }

    private boolean checkMaterialType(List<Condition> cons) {
        for (Condition c : cons) {
            if (c.getAttribute().getTypes().contains(AttributeType.MATERIAL_TYPE)) {
                Assert.assertTrue(((Set) cons.get(0).getValue().getValue()).contains(MaterialType.SEQUENCE.getId()));
                Assert.assertEquals(Operator.IN, cons.get(0).getOperator());
                return true;
            }
        }
        return false;
    }   

    private boolean checkSequenceType(List<Condition> cons) {
        for (Condition c : cons) {
            if (c.getAttribute().getTypes().contains(AttributeType.SEQUENCE_TYPE)) {
                Assert.assertEquals("DNA", (String) c.getValue().getValue());
                Assert.assertEquals(Operator.EQUAL, c.getOperator());
                return true;
            }
        }
        return false;
    }

    private User createUser() {
        User u = new User();
        u.setId(1);
        return u;
    }
}
