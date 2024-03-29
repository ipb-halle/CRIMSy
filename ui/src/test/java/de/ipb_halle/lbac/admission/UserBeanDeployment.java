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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.admission.mock.UserPluginSettingsBeanMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.timezone.TimeZonesBean;
import de.ipb_halle.lbac.util.pref.PreferenceService;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author fbroda
 */
public class UserBeanDeployment {

    public static WebArchive add(WebArchive deployment) {
        return deployment
                .addClass(KeyManager.class)
                .addClass(LdapProperties.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(SystemSettings.class)
                .addClass(Navigator.class)
                .addClass(PreferenceService.class)
                .addClass(UserPluginSettingsBeanMock.class)
                .addClass(TimeZonesBean.class)
                .addClass(UserTimeZoneSettingsBean.class)
                .addClass(UserBeanMock.class);
    }
}
