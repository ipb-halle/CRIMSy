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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.util.WebXml;
import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.molecularfaces.component.molplugin.MolPluginCore.PluginType;
import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class UserPluginSettingsBeanTest extends TestBase {
    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("UserPluginSettingsBeanTest.war")
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
    public void testAvailableAndDefaultMolPluginTypes() {
        List<String> plugins = new ArrayList<>();
        for (PluginType pt : PluginType.values()) {
            plugins.add(pt.toString());
        }

        WebXml webXml;
        UserPluginSettingsBean bean;

        if (plugins.isEmpty()) {
            /*
             * No plugins are available in MolecularFaces, so we expect an empty
             * plugin list and an empty default plugin type. This is probably
             * dead code.
             */

            // define mock behaviour
            webXml = new WebXml() {
                @Override
                public String getContextParam(String paramName, String defaultValue) {
                    return "OpenChemLibJS,MolPaintJS";
                }

                @Override
                public String getContextParam(String paramName,
                        FacesContext context, String defaultValue) {
                    return getContextParam(paramName, defaultValue);
                }
            };

            bean = new UserPluginSettingsBean(webXml, null, null);
            bean.init();

            assertEquals(new ArrayList<String>(), bean.getAllMolPluginTypes());
            assertEquals("", bean.getDefaultMolPluginType());
        } else {
            // comma-separated list of plugins
            String allPlugins;

            /*
             * Plugin list is in order that was defined by
             * MolPluginCore.PluginType.
             */
            allPlugins = plugins.stream().collect(Collectors.joining(","));

            // define mock behaviour
            final String paramValue = "SomeWeirdPluginNameThatWillNeverExist,"
                    + allPlugins;
            webXml = new WebXml() {
                @Override
                public String getContextParam(String paramName, String defaultValue) {
                    return paramValue;
                }

                @Override
                public String getContextParam(String paramName,
                        FacesContext context, String defaultValue) {
                    return getContextParam(paramName, defaultValue);
                }
            };

            bean = new UserPluginSettingsBean(webXml, null, null);
            bean.init();

            assertEquals(plugins, bean.getAllMolPluginTypes());
            assertEquals(plugins.get(0), bean.getDefaultMolPluginType());

            /*
             * Next test: reverse the plugin list
             */
            List<String> reversed = new ArrayList<>(plugins);
            Collections.reverse(reversed);

            allPlugins = reversed.stream().collect(Collectors.joining(","));

            // define mock behaviour
            final String paramValue2 = allPlugins
                    + ",SomeWeirdPluginNameThatWillNeverExist";
            webXml = new WebXml() {
                @Override
                public String getContextParam(String paramName, String defaultValue) {
                    return paramValue2;
                }

                @Override
                public String getContextParam(String paramName,
                        FacesContext context, String defaultValue) {
                    return getContextParam(paramName, defaultValue);
                }
            };

            bean = new UserPluginSettingsBean(webXml, null, null);
            bean.init();

            assertEquals(reversed, bean.getAllMolPluginTypes());
            assertEquals(reversed.get(0), bean.getDefaultMolPluginType());

            /*
             * Next test: empty plugin list
             */
            // define mock behaviour
            webXml = new WebXml() {
                @Override
                public String getContextParam(String paramName, String defaultValue) {
                    return "";
                }

                @Override
                public String getContextParam(String paramName,
                        FacesContext context, String defaultValue) {
                    return getContextParam(paramName, defaultValue);
                }
            };

            bean = new UserPluginSettingsBean(webXml, null, null);
            bean.init();

            assertEquals(new ArrayList<>(), bean.getAllMolPluginTypes());
            assertEquals("", bean.getDefaultMolPluginType());
        }
    }

    @Test
    public void getAndSetPreferencesTest() {
        UserBeanMock ub = new UserBeanMock();
        ub.setCurrentAccount(user);

        /*
         * Define mock behaviour of WebXml: return all possible plugin types.
         */
        List<String> plugins = new ArrayList<>();
        for (PluginType pt : PluginType.values()) {
            plugins.add(pt.toString());
        }

        final String allPlugins = plugins.stream()
                .collect(Collectors.joining(","));
        WebXml webXml = new WebXml() {
            @Override
            public String getContextParam(String paramName, String defaultValue) {
                return allPlugins;
            }

            @Override
            public String getContextParam(String paramName,
                    FacesContext context, String defaultValue) {
                return getContextParam(paramName, defaultValue);
            }
        };

        UserPluginSettingsBean bean = new UserPluginSettingsBean(webXml,
                preferenceService, ub);
        bean.init();

        if (!plugins.isEmpty()) {
            /*
             * receive default plugin type if no preference exists
             */
            assertEquals(plugins.get(0), bean.getPreferredMolPluginType());

            /*
             * try to set plugin name that does not exist, so we expect the
             * default plugin
             */
            assertFalse(bean.setPreferredMolPluginType(
                    "SomeWeirdPluginNameThatWillNeverExist"));
            assertEquals(plugins.get(0), bean.getPreferredMolPluginType());

            /*
             * try to set a valid plugin name
             */
            assertTrue(bean.setPreferredMolPluginType(plugins.get(0)));
            assertEquals(plugins.get(0), bean.getPreferredMolPluginType());
        } else {
            /*
             * receive default plugin type if no preference exists
             */
            assertEquals("", bean.getPreferredMolPluginType());
        }
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
