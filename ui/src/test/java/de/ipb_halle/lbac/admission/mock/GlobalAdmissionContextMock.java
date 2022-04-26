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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.io.FileUtils;

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

    /*
     * The reports directory is initialized lazily, i.e. only when requested. Most
     * tests won't need it. The shutdown() method needs to take care for its
     * recursive deletion. Marking it with File.deleteOnExit() may not work, because
     * it does not delete recursively.
     */
    private File reportsDirectory = null;

    @PostConstruct
    private void initialize() {
        super.init();
    }

    @PreDestroy
    private void shutdown() throws IOException {
        if (reportsDirectory != null) {
            FileUtils.deleteDirectory(reportsDirectory);
            reportsDirectory = null;
        }
    }

    private void initializeReportsDirectory() {
        try {
            reportsDirectory = Files.createTempDirectory("reports").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getLbacPropertiesPath() {
        return TEST_LBAC_PROPERTIES_PATH;
    }

    @Lock(LockType.WRITE)
    @Override
    public String getReportsDirectory() {
        if (reportsDirectory == null) {
            initializeReportsDirectory();
        }
        return reportsDirectory.getAbsolutePath();
    }
}