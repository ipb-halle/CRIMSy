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

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserEntity;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * This class will provide some test cases around Preferences.
 */
@RunWith(Arquillian.class)
public class PreferenceTest extends TestBase {

    private final static String TESTKEY = "TESTKEY";
    @Inject
    private PreferenceService preferenceService;

    @Inject 
    private EntityManagerService entityManagerService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("PreferenceTest.war")
            .addClass(EntityManagerService.class)
            .addClass(PreferenceService.class);
    }

    private User user;

    /**
     * preference tests
     */
    @Test
    public void testPreference() {
        this.user = createUser("ptester", "Preference Tester");

        assertEquals("testPermissions(): return default preference initially",
                this.preferenceService.getPreferenceValue(this.user, TESTKEY, "default"),
                "default");

        this.preferenceService.setPreference(this.user, TESTKEY, "success");

        assertEquals("testPermissions(): return real preference value",
                this.preferenceService.getPreferenceValue(this.user, TESTKEY, "default"),
                "success");
    }

    @After
    public void finish() {
        /*
         * deletion cascades to preferences table
         */
        this.entityManagerService.removeEntity(UserEntity.class, this.user.getId());
    }

}
