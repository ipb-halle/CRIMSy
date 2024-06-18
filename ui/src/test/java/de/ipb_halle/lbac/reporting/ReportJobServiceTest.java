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

import static de.ipb_halle.job.JobService.CONDITION_JOBTYPE;
import static de.ipb_halle.job.JobService.CONDITION_STATUS;
import static de.ipb_halle.job.JobStatus.BUSY;
import static de.ipb_halle.job.JobStatus.COMPLETED;
import static de.ipb_halle.job.JobStatus.FAILED;
import static de.ipb_halle.job.JobStatus.PENDING;
import static de.ipb_halle.job.JobType.REPORT;
import static de.ipb_halle.reporting.ReportType.CSV;
import static de.ipb_halle.reporting.ReportType.PDF;
import static de.ipb_halle.reporting.ReportType.XLSX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.ipb_halle.job.JobService;
import de.ipb_halle.job.JobStatus;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.reporting.mocks.ReportsDirectoryMock;
import de.ipb_halle.reporting.ReportDataPojo;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportJobServiceTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportJobService jobService;

    @Inject
    private ReportsDirectoryMock reportsDirectory;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ReportJobServiceTest.war")
                .addClass(ReportJobService.class)
                .addClass(ReportsDirectoryMock.class)
                .addClass(JobService.class);
    }


    @Test
    public void test_submit() {
        Map<String, Object> params = new HashMap<>();
        params.put("abc", "def");

        jobService.submit(new ReportDataPojo("report1 (pending)", CSV, params), adminUser);
        jobService.saveJob(jobService.submit(new ReportDataPojo("report2 (busy)", 
                PDF, new HashMap<>()), adminUser).setStatus(BUSY));
        jobService.saveJob(jobService.submit(new ReportDataPojo("report3 (completed)", 
                XLSX, null), adminUser).setStatus(COMPLETED));
        jobService.saveJob(jobService.submit(new ReportDataPojo("report4 (failed)",
                CSV, null), adminUser).setStatus(FAILED));

        List<ReportJob> allJobs = allJobs();
        List<ReportJob> busyJobs = jobsByStatus(BUSY);
        List<ReportJob> pendingJobs = jobsByStatus(PENDING);
        assertThat(allJobs, hasSize(4));
        assertThat(busyJobs, hasSize(1));
        assertThat(pendingJobs, hasSize(1));

        ReportDataPojo pojo = ReportDataPojo.deserialize(pendingJobs.get(0).getInput());
        assertEquals(adminUser.getId(), pendingJobs.get(0).getOwner().getId());
        assertEquals("report1 (pending)", pojo.getReportURI());
        assertEquals(CSV, pojo.getType());
        assertEquals(params, pojo.getParameters());

        pojo = ReportDataPojo.deserialize(busyJobs.get(0).getInput());
        assertEquals("report2 (busy)", pojo.getReportURI());
        assertEquals(PDF, pojo.getType());
        assertTrue(pojo.getParameters().isEmpty());

        pojo = ReportDataPojo.deserialize(jobsByStatus(COMPLETED).get(0).getInput());
        assertEquals("report3 (completed)", pojo.getReportURI());
        assertEquals(XLSX, pojo.getType());
        assertNull(pojo.getParameters());

        pojo = ReportDataPojo.deserialize(jobsByStatus(FAILED).get(0).getInput());
        assertEquals("report4 (failed)", pojo.getReportURI());
        assertEquals(CSV, pojo.getType());
        assertNull(pojo.getParameters());
    }


    @Test
    public void test_loadJobsForUser() {
        ReportJob job1 = new ReportJob().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        ReportJob job2 = new ReportJob().setJobType(REPORT).setStatus(PENDING).setOwner(publicUser).setQueue("");
        job1 = jobService.saveJob(job1);
        job2 = jobService.saveJob(job2);

        List<ReportJob> allJobs = allJobs();
        List<ReportJob> jobs = jobService.loadJobsForUser(adminUser);

        assertThat(allJobs, hasSize(2));
        assertThat(jobs, hasSize(1));
        assertEquals(job1.getJobId(), jobs.get(0).getJobId());
    }

    @Test
    public void test_deleteJob_fileExists() throws IOException {
        File tempFile = File.createTempFile("ReportJobServiceTest", "test");
        tempFile.deleteOnExit();
        assertTrue(tempFile.exists());

        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        jobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
        assertFalse(tempFile.exists());
    }

    @Test
    public void test_deleteJob_fileDoesNotExist(@TempDir File tempDir) {
        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(nonExistingFilename.getBytes());
        job = jobService.saveJob(job);

        jobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_deleteJob_jobOutputIsNull() {
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("").setOutput(null);
        job = jobService.saveJob(job);

        jobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_getOutputFileOfJob_noJobInDB() {
        ReportJob job = new ReportJob().setJobId(-1);

        assertNull(jobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_jobOutputIsNull() {
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("").setOutput(null);
        job = jobService.saveJob(job);

        assertNull(jobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_fileDoesNotExist(@TempDir File tempDir) {
        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(nonExistingFilename.getBytes());
        job = jobService.saveJob(job);

        assertNull(jobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_fileExists() throws IOException {
        File tempFile = File.createTempFile("ReportJobServiceTest", "test2");
        tempFile.deleteOnExit();
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        File f = jobService.getOutputFileOfJob(job);

        assertNotNull(f);
        assertEquals(tempFile, f);
    }

    private File createTempFileInReportsDirectory() throws IOException {
        File reportsDir = new File(reportsDirectory.getReportsDirectory());
        File tempFile = File.createTempFile("ReportJobServiceTest", "test", reportsDir);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private List<ReportJob> allJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        return jobService.loadJobs(cmap);
    }

    private List<ReportJob> jobsByStatus(JobStatus status) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, status);
        return jobService.loadJobs(cmap);
    }
}
