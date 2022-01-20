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
package de.ipb_halle.lbac.container;

import de.ipb_halle.lbac.container.entity.ContainerEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class ContainerTest {

    @Test
    public void test001_constructorTest() {
        ContainerEntity entity = new ContainerEntity();
        entity.setColumns(3);
        entity.setRows(5);

        Container c = new Container(entity);
        Assert.assertEquals(5, c.getItems().length);
        Assert.assertEquals(3, c.getItems()[0].length);

        entity.setColumns(null);
        entity.setRows(5);
        c = new Container(entity);
        Assert.assertEquals(1, c.getItems().length);
        Assert.assertEquals(5, c.getItems()[0].length);

        entity.setColumns(5);
        entity.setRows(null);

        Assert.assertThrows(NullPointerException.class, () -> {
            new Container(entity);
        });

        entity.setColumns(null);
        entity.setRows(null);
        c = new Container(entity);
        Assert.assertNull(c.getItems());

    }
}
