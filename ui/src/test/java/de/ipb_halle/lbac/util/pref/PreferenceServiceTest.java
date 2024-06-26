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

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserEntity;
import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * This class will provide some test cases around PreferenceService.
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class PreferenceServiceTest extends TestBase {

    private final static String TESTKEY = "TESTKEY";
    @Inject
    private PreferenceService preferenceService;

    @Inject
    private EntityManagerService entityManagerService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("PreferenceServiceTest.war")
                .addClass(EntityManagerService.class)
                .addClass(PreferenceService.class);
    }

    private User user;

    @BeforeEach
    public void beforeTest() {
        this.user = createUser("ptester", "Preference Tester");
    }

    /**
     * 
     */
    @Test()
    public void test001_testValues() {
        assertEquals("testPermissions(): return default preference initially",
                this.preferenceService.getPreferenceValue(this.user, TESTKEY,
                        "default"),
                "default");

        this.preferenceService.setPreference(this.user, TESTKEY, "success");

        assertEquals("testPermissions(): return real preference value",
                this.preferenceService.getPreferenceValue(this.user, TESTKEY,
                        "default"),
                "success");
    }

    @Test
    public void test002_save_and_overwrite() {
        this.preferenceService.setPreference(this.user, TESTKEY, "abc");
        assertEquals("abc", this.preferenceService.getPreferenceValue(user,
                TESTKEY, "def"));

        this.preferenceService.setPreference(this.user, TESTKEY, "def");
        assertEquals("def", this.preferenceService.getPreferenceValue(user,
                TESTKEY, "ghi"));
    }

    @Test
    public void test003_empty_key() {
        assertThrows(Exception.class, () -> this.preferenceService.setPreference(this.user, "", "xyz"));
    }

    @AfterEach
    public void finish() {
        /*
         * deletion cascades to preferences table
         */
        this.entityManagerService.removeEntity(UserEntity.class,
                this.user.getId());
    }

}
