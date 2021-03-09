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

import javax.faces.context.FacesContext;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.ipb_halle.lbac.admission.mock.FacesContextMock;
import de.ipb_halle.molecularfaces.MolPluginCore.PluginType;

public class UserPluginSettingsBeanTest {
    @Ignore("Mockito throws a weird exception because of a transitive dependency"
            + " conflict with hibernate-core, see"
            + " https://github.com/mockito/mockito/issues/1606#issuecomment-475281035")
    @Test
    public void testAvailableAndDefaultMolPluginTypes() {
        // This mocks the FacesContext calls in the WebXml utility class.
        FacesContext fcMock = mock(FacesContext.class, RETURNS_DEEP_STUBS);
        FacesContextMock context = new FacesContextMock(fcMock);
        FacesContextMock.setCurrentInstance(context);

        List<String> plugins = new ArrayList<>();
        for (PluginType pt : PluginType.values()) {
            plugins.add(pt.toString());
        }

        UserPluginSettingsBean bean;

        if (plugins.isEmpty()) {
            /*
             * No plugins are available in MolecularFaces, so we expect an empty
             * plugin list and an empty default plugin type.
             */

            // define mock behaviour
            when(fcMock.getExternalContext().getInitParameter(
                    UserPluginSettingsBean.WEBXML_AVAILABLE_MOLPLUGINTYPES))
                            .thenReturn("OpenChemLibJS,MolPaintJS");

            bean = new UserPluginSettingsBean();
            bean.init();

            assertEquals(new ArrayList<String>(), bean.getAllMolPluginTypes());
            assertEquals("", bean.getDefaultMolPluginType());
        } else {
            // comma-separated list of plugins
            String allPlugins;

            // plugin list is in order that was defined by
            // MolPluginCore.PluginType
            allPlugins = plugins.stream().collect(Collectors.joining(","));

            // define mock behaviour
            when(fcMock.getExternalContext().getInitParameter(
                    UserPluginSettingsBean.WEBXML_AVAILABLE_MOLPLUGINTYPES))
                            .thenReturn("SomeWeirdPluginNameThatWillNeverExist,"
                                    + allPlugins);

            bean = new UserPluginSettingsBean();
            bean.init();

            assertEquals(plugins, bean.getAllMolPluginTypes());
            assertEquals(plugins.get(0), bean.getDefaultMolPluginType());

            // next test: shuffle the plugin list
            List<String> shuffled = new ArrayList<>(plugins);
            Collections.shuffle(shuffled);

            allPlugins = shuffled.stream().collect(Collectors.joining(","));

            // define mock behaviour
            when(fcMock.getExternalContext().getInitParameter(
                    UserPluginSettingsBean.WEBXML_AVAILABLE_MOLPLUGINTYPES))
                            .thenReturn(allPlugins
                                    + ",SomeWeirdPluginNameThatWillNeverExist");

            bean = new UserPluginSettingsBean();
            bean.init();

            assertEquals(shuffled, bean.getAllMolPluginTypes());
            assertEquals(shuffled.get(0), bean.getDefaultMolPluginType());
        }

    }
}