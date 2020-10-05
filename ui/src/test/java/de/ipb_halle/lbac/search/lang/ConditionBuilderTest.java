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
package de.ipb_halle.lbac.search.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ConditionBuilderTest {

    List<Condition> conditions;

    @Test
    public void test001_buildConditionTree() {
        conditions = new ArrayList<>();
        Condition c1 = createCondition("c1");
        Condition c2 = createCondition("c2");
        Condition c3 = createCondition("c3");
        Condition c4 = createCondition("c4");
        Condition c5 = createCondition("c5");
        ConditionBuilder builder = new ConditionBuilder(conditions);
        Condition finalCondition = builder.buildConditionTree();
        Assert.assertFalse(finalCondition.isLeaf());

        Assert.assertNotNull(finalCondition.getLeftCondition());
        Assert.assertNotNull(finalCondition.getLeftCondition().getLeftCondition());
        Assert.assertNull(finalCondition.getLeftCondition().getLeftCondition().getLeftCondition());
        Assert.assertNull(finalCondition.getLeftCondition().getLeftCondition().getRightCondition());
        Assert.assertEquals(c3.getValue(), finalCondition.getLeftCondition().getLeftCondition().getValue());
        Assert.assertEquals(c2.getValue(), finalCondition.getLeftCondition().getRightCondition().getValue());
        Assert.assertEquals(c1.getValue(), finalCondition.getRightCondition().getLeftCondition().getValue());
        Assert.assertFalse(finalCondition.getRightCondition().getRightCondition().isLeaf());
        Assert.assertEquals(c5.getValue(), finalCondition.getRightCondition().getRightCondition().getLeftCondition().getValue());
        Assert.assertEquals(c4.getValue(), finalCondition.getRightCondition().getRightCondition().getRightCondition().getValue());

    }

    private Condition createCondition(String name) {
        Condition con = new Condition(new Attribute(AttributeType.INSTITUTION), Operator.ILIKE, new Value(name));
        conditions.add(con);
        return con;
    }
}
