/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.admission.mock;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;

/**
 * 
 * @author flange
 */
@Singleton(name = "globalAdmissionContext")
@Startup
public class GlobalAdmissionContextMock extends GlobalAdmissionContext {
    private static final long serialVersionUID = 1L;
    private static final String TEST_LBAC_PROPERTIES_PATH = "target/test-classes/keystore/lbac_properties.xml";

    @PostConstruct
    private void initialize() {
        super.init();
    }


    @Override
    public String getLbacPropertiesPath() {
        return TEST_LBAC_PROPERTIES_PATH;
    }
}
