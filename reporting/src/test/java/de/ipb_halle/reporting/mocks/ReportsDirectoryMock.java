/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.reporting.mocks;

import de.ipb_halle.reporting.ReportsDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import org.apache.commons.io.FileUtils;

/**
 *
 * @author fbroda, flange
 */
@Singleton(name="reportsDirectory")
//@Startup
public class ReportsDirectoryMock extends ReportsDirectory {

    private static final long serialVersionUID = 1L;

    /*
     * The reports directory is initialized lazily, i.e. only when requested. Most
     * tests won't need it. The shutdown() method needs to take care for its
     * recursive deletion. Marking it with File.deleteOnExit() may not work, because
     * it does not delete recursively.
     */
    private File reportsDirectory = null;

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

    @Lock(LockType.WRITE)
    @Override
    public String getReportsDirectory() {
        if (reportsDirectory == null) {
            initializeReportsDirectory();
        }
        return reportsDirectory.getAbsolutePath();
    }
}
