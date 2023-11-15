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
package de.ipb_halle.reporting;

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
import static de.ipb_halle.reporting.ReportingJobService.MAX_AGE;
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
import de.ipb_halle.reporting.ReportDataPojo;
import de.ipb_halle.reporting.mocks.ReportsDirectoryMock;
import de.ipb_halle.test.EntityManagerService;
import de.ipb_halle.test.ManagedExecutorServiceMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportingJobServiceTest {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportingJobService jobService;

    @Inject
    private ReportsDirectoryMock reportsDirectory;

    @Inject
    private EntityManagerService entityManagerService;

    private ManagedExecutorServiceMock managedExecutorService;
    private Integer adminUser = 4;
    private Integer publicUser = 1;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "ReportingJobServiceTest.war")
                .addClass(JobService.class)
                .addClass(ReportsDirectoryMock.class)
                .addClass(ReportingJobService.class)
                .addClass(EntityManagerService.class)
                .addClass(ManagedExecutorServiceMock.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @BeforeEach
    public void initExecutor() {
        entityManagerService.doSqlUpdate("DELETE FROM jobs");
        managedExecutorService = new ManagedExecutorServiceMock(2);
        jobService.replaceManagedExecutorService(managedExecutorService);
        jobService.replaceReportsDirectory(reportsDirectory);
    }


    @Test
    public void test_submit() {
        Map<String, Object> params = new HashMap<>();
        params.put("abc", "def");

        ReportingJob busy1 = submit(new ReportDataPojo("report1 (busy)", CSV, params), adminUser);
        ReportingJob busy2 = submit(new ReportDataPojo("report2 (busy)", PDF, new HashMap<>()), adminUser);
        jobService.submitPendingJobsToExecutor();

        ReportingJob pending1 = submit(new ReportDataPojo("report3 (pending)", XLSX, null), adminUser);
        ReportingJob pending2 = submit(new ReportDataPojo("report4 (pending)", CSV, null), adminUser);
        jobService.submitPendingJobsToExecutor();
        
        assertThat(managedExecutorService.getSubmittedTasks(), hasSize(2));

        List<ReportingJob> allJobs = allJobs();
        List<ReportingJob> busyJobs = busyJobs();
        List<ReportingJob> pendingJobs = pendingJobs();
        assertThat(allJobs, hasSize(4));
        assertThat(busyJobs, hasSize(2));
        assertThat(pendingJobs, hasSize(2));

        assertEquals(busyJobs.get(0).getJobId(), busy1.getJobId());
        ReportDataPojo pojo = ReportDataPojo.deserialize(busyJobs.get(0).getInput());
        assertEquals("report1 (busy)", pojo.getReportURI());
        assertEquals(CSV, pojo.getType());
        assertEquals(params, pojo.getParameters());

        assertEquals(busyJobs.get(1).getJobId(), busy2.getJobId());
        pojo = ReportDataPojo.deserialize(busyJobs.get(1).getInput());
        assertEquals("report2 (busy)", pojo.getReportURI());
        assertEquals(PDF, pojo.getType());
        assertTrue(pojo.getParameters().isEmpty());

        assertEquals(pendingJobs.get(0).getJobId(), pending1.getJobId());
        pojo = ReportDataPojo.deserialize(pendingJobs.get(0).getInput());
        assertEquals("report3 (pending)", pojo.getReportURI());
        assertEquals(XLSX, pojo.getType());

        pojo = ReportDataPojo.deserialize(pendingJobs.get(1).getInput());
        assertEquals("report4 (pending)", pojo.getReportURI());
        assertEquals(CSV, pojo.getType());
        assertNull(pojo.getParameters());
    }

    @Test
    public void test_markBusyJobsAsPending() {
        ReportingJob job = new ReportingJob().setJobType(REPORT).setStatus(BUSY).setOwnerId(adminUser).setQueue("");
        job = jobService.saveJob(job);

        jobService.markBusyJobsAsPending();

        job = jobService.loadJobById(job.getJobId());
        assertEquals(PENDING, job.getStatus());
    }

    @Test
    public void test_markJobAsCompleted_withValidJobId() {
        ReportingJob pendingJob = new ReportingJob().setJobType(REPORT).setStatus(PENDING).setOwnerId(adminUser).setQueue("");
        pendingJob = jobService.saveJob(pendingJob);

        ReportingJob completedJob = jobService.markJobAsCompleted(pendingJob.getJobId(), "somewhere");

        assertEquals(COMPLETED, completedJob.getStatus());
        assertEquals("somewhere", new String(completedJob.getOutput()));

        // everything correct in the database?
        List<ReportingJob> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(COMPLETED, allJobs.get(0).getStatus());
        assertEquals("somewhere", new String(allJobs.get(0).getOutput()));
    }

    @Test
    public void test_markJobAsCompleted_withInvalidJobId() {
        assertNull(jobService.markJobAsCompleted(42, "somewhere"));

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_markJobAsFailed_withValidJobId() {
        ReportingJob pendingJob = new ReportingJob().setJobType(REPORT).setStatus(PENDING).setOwnerId(adminUser).setQueue("");
        pendingJob = jobService.saveJob(pendingJob);

        ReportingJob failedJob = jobService.markJobAsFailed(pendingJob.getJobId());

        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());

        // everything correct in the database?
        List<ReportingJob> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(FAILED, allJobs.get(0).getStatus());
        assertNull(allJobs.get(0).getOutput());
    }

    @Test
    public void test_markJobAsFailed_withInvalidJobId() {
        assertNull(jobService.markJobAsFailed(42));

        assertThat(allJobs(), is(empty()));
    }

    @Test
    public void test_cleanUpOldJobsAndReportFiles() throws IOException {
        Instant now = Instant.now();
        Date beforeMaxAge = Date.from(now.minus(MAX_AGE).minusSeconds(1000));
        Date afterMaxAge = Date.from(now.minus(MAX_AGE).plusSeconds(1000));

        // create jobs and their associated output files
        File fileToBeDeleted = File.createTempFile("ReportingJobServiceTest", "test");
        File fileToBeKept = File.createTempFile("ReportingJobServiceTest", "test");
        fileToBeDeleted.deleteOnExit();
        fileToBeKept.deleteOnExit();
        assertTrue(fileToBeDeleted.exists());
        assertTrue(fileToBeKept.exists());

        ReportingJob jobToBeDeleted = new ReportingJob().setJobDate(beforeMaxAge).setJobType(REPORT).setStatus(COMPLETED)
                .setOwnerId(adminUser).setQueue("").setOutput(fileToBeDeleted.getAbsolutePath().getBytes());
        ReportingJob jobToBeKept = new ReportingJob().setJobDate(afterMaxAge).setJobType(REPORT).setStatus(COMPLETED).setOwnerId(adminUser)
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
        jobService.cleanUpOldJobsAndReportFiles();

        // check jobs and their associated output files
        List<ReportingJob> allJobs = allJobs();
        assertThat(allJobs, hasSize(1));
        assertEquals(jobToBeKept.getJobId(), allJobs.get(0).getJobId());

        assertFalse(fileToBeDeleted.exists());
        assertTrue(fileToBeKept.exists());

        // check orphaned files in the reports directory
        assertFalse(orphanedFileToBeDeleted.exists());
        assertTrue(orphanedFileToBeKept.exists());
    }

    private File createTempFileInReportsDirectory() throws IOException {
        File reportsDir = new File(reportsDirectory.getReportsDirectory());
        File tempFile = File.createTempFile("ReportingJobServiceTest", "test", reportsDir);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private List<ReportingJob> allJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        return jobService.loadJobs(cmap);
    }

    private List<ReportingJob> pendingJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, PENDING);
        return jobService.loadJobs(cmap);
    }

    private List<ReportingJob> busyJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, BUSY);
        return jobService.loadJobs(cmap);
    }

    private ReportingJob submit(ReportDataPojo pojo, Integer userId) {
        ReportingJob job = new ReportingJob()
                .setStatus(PENDING)
                .setOwnerId(userId)
                .setQueue("")
                .setInput(pojo.serialize());
        return jobService.saveJob(job);
    }
}
