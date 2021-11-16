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
package de.ipb_halle.lbac.material.sequence.history;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.material.sequence.SequenceSearchConditionBuilder;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.Operator;
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
    public void test001() {
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(createUser(), 0, 2);
        requestBuilder.addMaterialType(MaterialType.SEQUENCE);
        requestBuilder.setSequenceType(SequenceType.DNA);
        requestBuilder.setSequenceString("AAA");
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(graphBuilder.buildEntityGraph(true), "materials");
        List<Condition> cons = builder.getMaterialCondition(requestBuilder.build(), true);

        Assert.assertTrue(cons.get(0).getAttribute().getTypes().contains(AttributeType.MATERIAL_TYPE));
        Assert.assertTrue(((Set) cons.get(0).getValue().getValue()).contains(MaterialType.SEQUENCE.getId()));
        Assert.assertEquals(Operator.IN, cons.get(0).getOperator());

        Assert.assertTrue(cons.get(1).getAttribute().getTypes().contains(AttributeType.SEQUENCE_STRING));
        Assert.assertEquals("AAA", (String) cons.get(1).getValue().getValue());
        Assert.assertEquals(Operator.EQUAL, cons.get(1).getOperator());

        Assert.assertTrue(cons.get(2).getAttribute().getTypes().contains(AttributeType.SEQUENCE_TYPE));
        Assert.assertEquals("DNA", (String) cons.get(2).getValue().getValue());
        Assert.assertEquals(Operator.EQUAL, cons.get(2).getOperator());

    }

    private User createUser() {
        User u = new User();
        u.setId(1);
        return u;
    }
}
