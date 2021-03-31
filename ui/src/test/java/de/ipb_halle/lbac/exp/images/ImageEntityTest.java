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
package de.ipb_halle.lbac.exp.images;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;

/**
 * 
 * @author flange
 */
public class ImageEntityTest {
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
    public void test001_gettersAndSetters() {
        ImageEntity entity = new ImageEntity();
        
        assertEquals(null, entity.getId());
        assertEquals(null, entity.getTitle());
        assertEquals(null, entity.getPreview());
        assertEquals(null, entity.getImage());
        assertEquals(null, entity.getOwner());
        assertEquals(null, entity.getACList());
        
        entity.setId(42L);
        entity.setTitle("title");
        entity.setPreview("preview");
        entity.setImage("image");
        entity.setOwner(user.getId());
        entity.setACList(aclist.getId());
        
        assertEquals(Long.valueOf(42L), entity.getId());
        assertEquals("title", entity.getTitle());
        assertEquals("preview", entity.getPreview());
        assertEquals("image", entity.getImage());
        assertEquals(user.getId(), entity.getOwner());
        assertEquals(aclist.getId(), entity.getACList());
    }
}
