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
package de.ipb_halle.lbac.exp.images;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ImageTest {

    private User user;
    private ACList aclist;

    @Before
    public void init() {
        user = new User();
        user.setId(10);
        aclist = new ACList();
        aclist.setId(100);
    }

    @Test
    public void test001_createDBEntity() {
        Image image = new Image("preview", "image", user, aclist);
        ImageEntity entity = image.createEntity();
        Assert.assertEquals("preview", entity.getPreview());
        Assert.assertEquals("image", entity.getImage());
        Assert.assertEquals(user.getId(), entity.getOwner());
        Assert.assertEquals(aclist.getId(), entity.getACList());

        entity.setId(1);
        Image imageFromEntity = new Image(entity, aclist, user);
        Assert.assertEquals("preview", imageFromEntity.getPreview());
        Assert.assertEquals("image", imageFromEntity.getImage());
        Assert.assertEquals(user, imageFromEntity.user);
        Assert.assertEquals(aclist, imageFromEntity.aclist);
        Assert.assertEquals(1, imageFromEntity.id, 0);

    }
}
