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
package de.ipb_halle.lbac.util.pref;

import de.ipb_halle.lbac.admission.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Assert;
import org.junit.Before;

/**
 * This class will provide some test cases around Preferences.
 */
public class PreferenceTest {
    private User user;

    @Before
    public void beforeTest() {
        this.user = new User();
        user.setName("Preference Tester");
        user.setId(123);
    }

    @Test
    public void test_equals_hashCode_toString() {
        Preference pref1 = new Preference(user, "category-1", "1");
        Preference pref2 = new Preference(user, "category-1", "1");
        Assert.assertTrue(pref1.equals(pref2));

        pref1.setValue("2");
        Assert.assertFalse(pref1.equals(pref2));

        Assert.assertTrue(pref1.equals(pref1));

        Assert.assertFalse(pref1.equals(user));

        Preference pref3 = new Preference(user, "category-2", "1");
        Assert.assertFalse(pref1.equals(pref3));
        Assert.assertEquals("category-2", pref3.getKey());

        Assert.assertEquals(1537730926, pref3.hashCode());

        Assert.assertEquals(
                "Preference{id='null', key='category-2', value='1', user='Preference Tester'}",
                pref3.toString());
    }

    @Test
    public void test_constructors_NPE() {
        assertThrows(NullPointerException.class,
                () -> new Preference(null, null, null));
        assertThrows(NullPointerException.class,
                () -> new Preference(user, null, null));
        assertThrows(NullPointerException.class,
                () -> new Preference(null, "key", null));

        PreferenceEntity entity = new PreferenceEntity();
        entity.setKey(null);
        assertThrows(NullPointerException.class,
                () -> new Preference(entity, user));

        entity.setKey("key");
        assertThrows(NullPointerException.class,
                () -> new Preference(entity, null));
    }

    @Test
    public void test_getters_setters() {
        PreferenceEntity entity = new PreferenceEntity();
        entity.setId(42).setKey("key abc").setValue("val def");
        Preference pref = new Preference(entity, user);

        assertEquals(Integer.valueOf(42), pref.getId());
        assertEquals("key abc", pref.getKey());
        assertEquals("val def", pref.getValue());
        assertEquals(user, pref.getUser());

        pref.setValue("new value");
        assertEquals("new value", pref.getValue());

        pref = new Preference(user, "key abc", "val def");
        assertEquals(null, pref.getId());
        assertEquals("key abc", pref.getKey());
        assertEquals("val def", pref.getValue());
        assertEquals(user, pref.getUser());
    }

    @Test
    public void test_createEntity() {
        PreferenceEntity entity = new PreferenceEntity();
        entity.setId(42).setKey("key abc").setUserId(1).setValue("val def");
        PreferenceEntity newEntity = new Preference(entity, user)
                .createEntity();

        assertEquals(Integer.valueOf(42), newEntity.getId());
        // userId from PreferenceEntity will be overridden
        assertEquals(Integer.valueOf(123), newEntity.getUserId());
        assertEquals("key abc", newEntity.getKey());
        assertEquals("val def", newEntity.getValue());
    }
}
