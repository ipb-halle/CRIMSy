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
import static de.ipb_halle.lbac.reporting.report.ReportType.CSV;
import static de.ipb_halle.lbac.reporting.report.ReportType.PDF;
import static de.ipb_halle.lbac.reporting.report.ReportType.XLSX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ReportJobServiceTest.war").addClass(ReportJobService.class)
                .addClass(JobService.class);
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
        job = jobService.save(job);

        reportJobService.markBusyJobsAsPending();

        job = jobService.loadById(job.getJobId());
        assertEquals(PENDING, job.getStatus());
    }

    @Test
    public void test_submitPendingTasksToExecutor() {
        Job pending1 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        Job pending2 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        Job pending3 = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        pending1 = jobService.save(pending1);
        pending2 = jobService.save(pending2);
        pending3 = jobService.save(pending3);

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
        pendingJob = jobService.save(pendingJob);

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

        assertThat(allJobs(), hasSize(0));
    }

    @Test
    public void test_markJobAsFailed_withValidJobId() {
        Job pendingJob = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("");
        pendingJob = jobService.save(pendingJob);

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

        assertThat(allJobs(), hasSize(0));
    }

    private List<Job> allJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        return jobService.load(cmap);
    }

    private List<Job> pendingJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, PENDING);
        return jobService.load(cmap);
    }

    private List<Job> busyJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, BUSY);
        return jobService.load(cmap);
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