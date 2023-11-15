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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.timezone.TimeZonesBean;
import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class UserTimeZoneSettingsBeanTest extends TestBase {
    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("UserTimeZoneSettingsBeanTest.war")
                        .addClass(EntityManagerService.class)
                        .addClass(PreferenceService.class));
    }

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private EntityManagerService entityManagerService;

    private User user;

    @BeforeEach
    public void beforeTest() {
        this.user = createUser("ptester", "Preference Tester");
    }

    @Test
    public void test001_getAndSetPreferences() {
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(user);
        TimeZonesBean timeZonesBean = new TimeZonesBean();
        timeZonesBean.init();

        UserTimeZoneSettingsBean bean = new UserTimeZoneSettingsBean(
                preferenceService, userBean, timeZonesBean);
        bean.init();

        // Try to insert something invalid.
        assertFalse(bean.setPreferredTimeZone("Invalid time zone"));

        // Nothing is in database yet, so we expect the default time zone.
        assertEquals(timeZonesBean.getDefaultTimeZone(),
                bean.getPreferredTimeZone());

        // Manipulate database entry to an invalid time zone.
        preferenceService.setPreference(user,
                UserTimeZoneSettingsBean.TIMEZONE_PREFERENCE_KEY,
                "Invalid time zone");
        // Expecting default.
        assertEquals(timeZonesBean.getDefaultTimeZone(),
                bean.getPreferredTimeZone());

        // Insert something valid
        assertTrue(bean.setPreferredTimeZone("UTC"));
        assertEquals("UTC", bean.getPreferredTimeZone());

        // Manipulate database entry to an invalid time zone.
        preferenceService.setPreference(user,
                UserTimeZoneSettingsBean.TIMEZONE_PREFERENCE_KEY,
                "Invalid time zone");
        // Expecting that the setting wasn't changed.
        assertEquals("UTC", bean.getPreferredTimeZone());
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
