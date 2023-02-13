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
package de.ipb_halle.lbac.reporting.job;

import static de.ipb_halle.lbac.device.job.JobService.CONDITION_JOBTYPE;
import static de.ipb_halle.lbac.device.job.JobService.CONDITION_STATUS;
import static de.ipb_halle.lbac.device.job.JobStatus.BUSY;
import static de.ipb_halle.lbac.device.job.JobStatus.COMPLETED;
import static de.ipb_halle.lbac.device.job.JobStatus.FAILED;
import static de.ipb_halle.lbac.device.job.JobStatus.PENDING;
import static de.ipb_halle.lbac.device.job.JobType.REPORT;
import static de.ipb_halle.lbac.reporting.job.ReportJobService.MAX_AGE;
import static de.ipb_halle.lbac.reporting.report.ReportType.CSV;
import static de.ipb_halle.lbac.reporting.report.ReportType.PDF;
import static de.ipb_halle.lbac.reporting.report.ReportType.XLSX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportJobServiceTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportJobService reportJobService;

    @Inject
    private JobService jobService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ReportJobServiceTest.war").addClasses(ReportJobService.class, JobService.class);
    }

    private ManagedExecutorServiceMock managedExecutorService;

    @BeforeEach
    private void before() {
        managedExecutorService = new ManagedExecutorServiceMock();
        reportJobService.setManagedExecutorService(managedExecutorService);
    }

    @Test
    public void test_submit() {
        Map<String, Object> params = new HashMap<>();
        params.put("abc", "def");

        reportJobService.submit(new ReportJobPojo("report1 (busy)", CSV, params), adminUser);
        reportJobService.submit(new ReportJobPojo("report2 (busy)", PDF, new HashMap<>()), adminUser);
        reportJobService.submit(new ReportJobPojo("report3 (pending)", XLSX, null), adminUser);

        assertThat(managedExecutorService.getSubmittedTasks(), hasSize(2));

        List<Job> allJobs = allJobs();
        List<Job> busyJobs = busyJobs();
        List<Job> pendingJobs = pendingJobs();
        assertThat(allJobs, hasSize(3));
        assertThat(busyJobs, hasSize(2));
        assertThat(pendingJobs, hasSize(1));

        ReportJobPojo pojo = deserializeToReportJobPojo(busyJobs.get(0).getInput());
        assertEquals("report1 (busy)", pojo.getReportURI());
        assertEquals(CSV, pojo.getType());
        assertEquals(params, pojo.getParameters());

        pojo = deserializeToReportJobPojo(busyJobs.get(1).getInput());
        assertEquals("report2 (busy)", pojo.getReportURI());
        assertEquals(PDF, pojo.getType());
        assertTrue(pojo.getParameters().isEmpty());

        pojo = deserializeToReportJobPojo(pendingJobs.get(0).getInput());
        assertEquals("report3 (pending)", pojo.getReportURI());
        assertEquals(XLSX, pojo.getType());
        assertNull(pojo.getParameters());
    }

    @Test
    public void test_markBusyJobsAsPending() {
        Job job = new Job().setJobType(REPORT).setStatus(BUSY).setOwner(adminUser).setQueue("");
        job = jobService.saveJob(job);

        reportJobService.markBusyJobsAsPending();

        job = jobService.loadJobById(job.getJobId());
        assertEquals(PENDING, job.getStatus());
    }

    @Test
    public void test_submitPendingTasksToExecutor() {
        Job pending1 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        Job pending2 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        Job pending3 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        pending1 = jobService.saveJob(pending1);
        pending2 = jobService.saveJob(pending2);
        pending3 = jobService.saveJob(pending3);

        reportJobService.submitPendingTasksToExecutor();

        assertThat(managedExecutorService.getSubmittedTasks(), hasSize(2));

        List<Job> allJobs = allJobs();
        List<Job> busyJobs = busyJobs();
        List<Job> pendingJobs = pendingJobs();
        assertThat(allJobs, hasSize(3));
        assertThat(busyJobs, hasSize(2));
        assertThat(pendingJobs, hasSize(1));

        assertEquals(pending1.getJobId(), busyJobs.get(0).getJobId());
        assertEquals(pending2.getJobId(), busyJobs.get(1).getJobId());
        assertEquals(pending3.getJobId(), pendingJobs.get(0).getJobId());
    }

    @Test
    public void test_markJobAsCompleted_withValidJobId() {
        Job pendingJob = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        pendingJob = jobService.saveJob(pendingJob);

        Job completedJob = reportJobService.markJobAsCompleted(pendingJob.getJobId(), "somewhere");

        assertEquals(COMPLETED, completedJob.getStatus());
        assertEquals("somewhere", new String(completedJob.getOutput()));

        // everything correct in the database?
        List<Job> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(COMPLETED, allJobs.get(0).getStatus());
        assertEquals("somewhere", new String(allJobs.get(0).getOutput()));
    }

    @Test
    public void test_markJobAsCompleted_withInvalidJobId() {
        assertNull(reportJobService.markJobAsCompleted(42, "somewhere"));

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_markJobAsFailed_withValidJobId() {
        Job pendingJob = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        pendingJob = jobService.saveJob(pendingJob);

        Job failedJob = reportJobService.markJobAsFailed(pendingJob.getJobId());

        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());

        // everything correct in the database?
        List<Job> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(FAILED, allJobs.get(0).getStatus());
        assertNull(allJobs.get(0).getOutput());
    }

    @Test
    public void test_markJobAsFailed_withInvalidJobId() {
        assertNull(reportJobService.markJobAsFailed(42));

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_loadJobsForUser() {
        Job job1 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        Job job2 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(publicUser).setQueue("");
        job1 = jobService.saveJob(job1);
        job2 = jobService.saveJob(job2);

        List<Job> jobs = reportJobService.loadJobsForUser(adminUser);

        assertThat(jobs, hasSize(1));
        assertEquals(job1.getJobId(), jobs.get(0).getJobId());
    }

    @Test
    public void test_deleteJob_fileExists() throws IOException {
        File tempFile = File.createTempFile("ReportJobServiceTest", "test");
        tempFile.deleteOnExit();
        assertTrue(tempFile.exists());

        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        reportJobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
        assertFalse(tempFile.exists());
    }

    @Test
    public void test_deleteJob_fileDoesNotExist(@TempDir File tempDir) {
        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(nonExistingFilename.getBytes());
        job = jobService.saveJob(job);

        reportJobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_deleteJob_jobOutputIsNull() {
        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("").setOutput(null);
        job = jobService.saveJob(job);

        reportJobService.deleteJob(job);

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_getOutputFileOfJob_noJobInDB() {
        Job job = new Job().setJobId(-1);

        assertNull(reportJobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_jobOutputIsNull() {
        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("").setOutput(null);
        job = jobService.saveJob(job);

        assertNull(reportJobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_fileDoesNotExist(@TempDir File tempDir) {
        String nonExistingFilename = tempDir.getAbsolutePath() + "/doesNotExist.file";
        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(nonExistingFilename.getBytes());
        job = jobService.saveJob(job);

        assertNull(reportJobService.getOutputFileOfJob(job));
    }

    @Test
    public void test_getOutputFileOfJob_fileExists() throws IOException {
        File tempFile = File.createTempFile("ReportJobServiceTest", "test2");
        tempFile.deleteOnExit();
        Job job = new Job().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        File f = reportJobService.getOutputFileOfJob(job);

        assertNotNull(f);
        assertEquals(tempFile, f);
    }

    @Test
    public void test_cleanUpOldJobsAndReportFiles() throws IOException {
        Instant now = Instant.now();
        Date beforeMaxAge = Date.from(now.minus(MAX_AGE).minusSeconds(1000));
        Date afterMaxAge = Date.from(now.minus(MAX_AGE).plusSeconds(1000));

        // create jobs and their associated output files
        File fileToBeDeleted = File.createTempFile("ReportJobServiceTest", "test");
        File fileToBeKept = File.createTempFile("ReportJobServiceTest", "test");
        fileToBeDeleted.deleteOnExit();
        fileToBeKept.deleteOnExit();
        assertTrue(fileToBeDeleted.exists());
        assertTrue(fileToBeKept.exists());

        Job jobToBeDeleted = new Job().setJobDate(beforeMaxAge).setJobType(REPORT).setStatus(COMPLETED)
                .setOwner(adminUser).setQueue("").setOutput(fileToBeDeleted.getAbsolutePath().getBytes());
        Job jobToBeKept = new Job().setJobDate(afterMaxAge).setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser)
                .setQueue("").setOutput(fileToBeKept.getAbsolutePath().getBytes());
        jobToBeDeleted = jobService.saveJob(jobToBeDeleted);
        jobToBeKept = jobService.saveJob(jobToBeKept);

        // create orphaned files in the reports directory
        File orphanedFileToBeDeleted = createTempFileInReportsDirectory();
        File orphanedFileToBeKept = createTempFileInReportsDirectory();
        assertTrue(orphanedFileToBeDeleted.exists());
        assertTrue(orphanedFileToBeKept.exists());
        orphanedFileToBeDeleted.setLastModified(beforeMaxAge.getTime());
        orphanedFileToBeKept.setLastModified(afterMaxAge.getTime());

        // execution
        reportJobService.cleanUpOldJobsAndReportFiles();

        // check jobs and their associated output files
        List<Job> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(jobToBeKept.getJobId(), allJobs.get(0).getJobId());

        assertFalse(fileToBeDeleted.exists());
        assertTrue(fileToBeKept.exists());

        // check orphaned files in the reports directory
        assertFalse(orphanedFileToBeDeleted.exists());
        assertTrue(orphanedFileToBeKept.exists());
    }

    private File createTempFileInReportsDirectory() throws IOException {
        File reportsDir = new File(globalAdmissionContext.getReportsDirectory());
        File tempFile = File.createTempFile("ReportJobServiceTest", "test", reportsDir);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private List<Job> allJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        return jobService.loadJobs(cmap);
    }

    private List<Job> pendingJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, PENDING);
        return jobService.loadJobs(cmap);
    }

    private List<Job> busyJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, BUSY);
        return jobService.loadJobs(cmap);
    }

    private ReportJobPojo deserializeToReportJobPojo(byte[] bytes) {
        Object o = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            o = ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return (ReportJobPojo) o;
    }
}
