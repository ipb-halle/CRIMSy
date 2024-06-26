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

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SystemSettingsTest extends TestBase {

    private static final long serialVersionUID = 1L;

    private SystemSettings settings;

    @Inject
    private InfoObjectService infoService;

    @Inject
    private GlobalAdmissionContext context;

    @BeforeEach
    public void beforeTest() {
        entityManagerService.doSqlUpdate("UPDATE info set value='True' WHERE key='SETTING_FORCE_LOGIN'");

        settings = new SystemSettings();
        settings.infoObjectService = infoService;
        settings.globalAdmissionContext = context;
        settings.SystemSettingsInit();
    }

    @AfterEach
    public void clean() {
        entityManagerService.doSqlUpdate("DELETE FROM info WHERE key='SETTING_INSTITUTION_WEB'");
    }

    @Test
    public void test001_getBoolean() {
        Assert.assertFalse(settings.getBoolean("XXX"));
        Assert.assertTrue(settings.getBoolean("SETTING_FORCE_LOGIN"));
    }

    @Test
    public void test002_getString() {
        Assert.assertEquals("Homepage", settings.getString("SETTING_INSTITUTION_WEB"));
        Assert.assertEquals("", settings.getString("XXX"));
    }

    @Test
    public void test003_getBoolSettings() {
        Assert.assertEquals(1, settings.getBoolSettings().size());
        List keys = settings.getBoolSettings().stream().map(v -> v.getKey()).collect(Collectors.toList());
        Assert.assertTrue(keys.contains(SystemSettings.SETTING_FORCE_LOGIN));
    }

    @Test
    public void test004_getStringSettings() {
        Assert.assertEquals(3, settings.getStringSettings().size());
        List<String> keys = settings.getStringSettings().stream().map(v -> v.getKey()).collect(Collectors.toList());
        Assert.assertTrue(keys.contains(SystemSettings.SETTING_INSTITUTION_WEB));
        Assert.assertTrue(keys.contains(SystemSettings.SETTING_GDPR_CONTACT));
        Assert.assertTrue(keys.contains(SystemSettings.SETTING_LOGIN_CUSTOM_TEXT));
    }

    @Test
    public void test005_save() {
        List<InfoObject> booleanInfoObjects = settings.getBoolSettings();
        booleanInfoObjects.get(0).setValue("False");
//      settings.messagePresenter = getMessagePresenterMock();
        settings.save();
        Assert.assertFalse(settings.getBoolean("SETTING_FORCE_LOGIN"));
        booleanInfoObjects.get(0).setValue("True");
        Assert.assertTrue(settings.getBoolean("SETTING_FORCE_LOGIN"));
    }

    @Test
    public void test006_hasHomePage() {
        entityManagerService.doSqlUpdate("DELETE FROM info WHERE key='SETTING_INSTITUTION_WEB'");
        Assert.assertFalse(settings.hasHomePage());
        addHomePageInfoObject();
        Assert.assertTrue(settings.hasHomePage());
    }

    @Test
    public void test007_getHomePage() {
        entityManagerService.doSqlUpdate("DELETE FROM info WHERE key='SETTING_INSTITUTION_WEB'");
        Assert.assertEquals("", settings.getHomePage());
        addHomePageInfoObject();
        Assert.assertEquals("example", settings.getHomePage());
    }

    @Test
    public void test008_set_get_customDsgvoText() {
        settings.setCustomDsgvoString("customText");
        Assert.assertEquals("customText", settings.getCustomDsgvoString());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("SystemSettingsTest.war"));
    }

    private void addHomePageInfoObject() {
        int ownerId = publicUser.getId();
        int aclId = GlobalAdmissionContext.getPublicReadACL().getId();
        entityManagerService.doSqlUpdate(
                String.format("INSERT INTO info VALUES (%s,%s,%d,%d)",
                        "'SETTING_INSTITUTION_WEB'",
                        "'example'",
                        ownerId,
                        aclId
                ));
    }
}
