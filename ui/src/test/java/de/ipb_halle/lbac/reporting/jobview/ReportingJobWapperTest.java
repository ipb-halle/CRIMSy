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
package de.ipb_halle.lbac.reporting.jobview;

import static de.ipb_halle.lbac.device.job.JobStatus.COMPLETED;
import static de.ipb_halle.lbac.device.job.JobStatus.FAILED;
import static de.ipb_halle.lbac.device.job.JobStatus.PENDING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobStatus;

public class ReportingJobWapperTest {
    private Job job;
    private ReportingJobWapper wrapper;

    @BeforeEach
    public void before() {
        job = new Job();
        wrapper = new ReportingJobWapper(job);
    }

    @Test
    public void test_getJob() {
        assertSame(job, wrapper.getJob());
    }

    @Test
    public void test_getI18nKeyForStatus() {
        job.setStatus(null);
        assertEquals("", wrapper.getI18nKeyForStatus());

        for (JobStatus status : JobStatus.values()) {
            job.setStatus(status);
            assertEquals("jobStatus_" + status.toString(), wrapper.getI18nKeyForStatus());
        }
    }

    @Test
    public void test_isDownloadable(@TempDir File tempDir) throws IOException {
        job.setStatus(FAILED);
        assertFalse(wrapper.isDownloadable());

        job.setStatus(COMPLETED).setOutput(null);
        assertFalse(wrapper.isDownloadable());

        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        job.setOutput(nonExistingFilename.getBytes());
        assertFalse(wrapper.isDownloadable());

        File tempFile = File.createTempFile("ReportingJobWapperTest", "test", tempDir);
        tempFile.deleteOnExit();
        job.setOutput(tempFile.getAbsolutePath().getBytes());
        assertTrue(wrapper.isDownloadable());
    }

    @Test
    public void test_isDeleteable() {
        job.setStatus(null);
        assertFalse(wrapper.isDeleteable());

        job.setStatus(PENDING);
        assertFalse(wrapper.isDeleteable());

        job.setStatus(FAILED);
        assertTrue(wrapper.isDeleteable());

        job.setStatus(COMPLETED);
        assertTrue(wrapper.isDeleteable());
    }

    @Test
    public void test_getRowStyleClass() {
        job.setStatus(null);
        assertEquals("", wrapper.getRowStyleClass());

        for (JobStatus status : JobStatus.values()) {
            job.setStatus(status);
            assertEquals("report-" + status.toString().toLowerCase(), wrapper.getRowStyleClass());
        }
    }
}
