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
    }

    public void test001_equals_hashCode_toString() {
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

    @Test(expected = NullPointerException.class)
    public void test002_constructor1_NPE() {
        new Preference(null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void test003_constructor2_nullKey_NPE() {
        PreferenceEntity entity = new PreferenceEntity();
        entity.setKey(null);
        new Preference(entity, user);
    }

    @Test(expected = NullPointerException.class)
    public void test003_constructor2_nullUser_NPE() {
        PreferenceEntity entity = new PreferenceEntity();
        entity.setKey("abc");
        new Preference(entity, null);
    }
}
