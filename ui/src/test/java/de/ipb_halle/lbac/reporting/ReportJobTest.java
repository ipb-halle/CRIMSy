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
package de.ipb_halle.lbac.reporting;

import static de.ipb_halle.job.JobStatus.COMPLETED;
import static de.ipb_halle.job.JobStatus.FAILED;
import static de.ipb_halle.job.JobStatus.PENDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import de.ipb_halle.job.JobStatus;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


public class ReportJobTest {
    private ReportJob job;

    @BeforeEach
    public void before() {
        job = new ReportJob();
    }

    @Test
    public void test_getI18nKeyForStatus() {
        job.setStatus(null);
        assertEquals("", job.getI18nKeyForStatus());

        for (JobStatus status : JobStatus.values()) {
            job.setStatus(status);
            assertEquals("jobStatus_" + status.toString(), job.getI18nKeyForStatus());
        }
    }

    @Test
    public void test_isDownloadable(@TempDir File tempDir) throws IOException {
        job.setStatus(FAILED);
        assertFalse(job.isDownloadable());

        job.setStatus(COMPLETED).setOutput(null);
        assertFalse(job.isDownloadable());

        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        job.setOutput(nonExistingFilename.getBytes());
        assertFalse(job.isDownloadable());

        File tempFile = File.createTempFile("ReportJobTest", "test", tempDir);
        tempFile.deleteOnExit();
        job.setOutput(tempFile.getAbsolutePath().getBytes());
        assertTrue(job.isDownloadable());
    }

    @Test
    public void test_isDeleteable() {
        job.setStatus(null);
        assertFalse(job.isDeleteable());

        job.setStatus(PENDING);
        assertFalse(job.isDeleteable());

        job.setStatus(FAILED);
        assertTrue(job.isDeleteable());

        job.setStatus(COMPLETED);
        assertTrue(job.isDeleteable());
    }

    @Test
    public void test_getRowStyleClass() {
        job.setStatus(null);
        assertEquals("", job.getRowStyleClass());

        for (JobStatus status : JobStatus.values()) {
            job.setStatus(status);
            assertEquals("report-" + status.toString().toLowerCase(), job.getRowStyleClass());
        }
    }
}
