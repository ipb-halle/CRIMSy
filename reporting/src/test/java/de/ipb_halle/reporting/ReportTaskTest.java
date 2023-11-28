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

import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobService;
import de.ipb_halle.test.ManagedExecutorServiceMock;

import static de.ipb_halle.job.JobStatus.COMPLETED;
import static de.ipb_halle.job.JobStatus.FAILED;
import static de.ipb_halle.job.JobStatus.PENDING;
import static de.ipb_halle.job.JobType.REPORT;
import static de.ipb_halle.reporting.ReportType.CSV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportTaskTest {

    private static final long serialVersionUID = 1L;

    @Inject
    private ReportingJobService jobService;

    @TempDir
    private static File tempReportsDir;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "ReportTaskTest.war")
                .addClass(JobEntity.class)
                .addClass(ReportsDirectory.class)
                .addClass(JobService.class)
                .addClass(ReportingJobService.class)
                .addClass(ManagedExecutorServiceMock.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @BeforeEach
    public void init() {
        jobService.replaceManagedExecutorService(new ManagedExecutorServiceMock(2));
    }

    @Test
    public void test_run_generatesReport() throws IOException {
        String reportURI = this.getClass().getClassLoader().getResource("reports/testReport1.prpt").toString();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("paramHello", 42);
        parameters.put("paramWorld", "abcdef");
        ReportDataPojo reportDataPojo = new ReportDataPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportDataPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        ReportingJob finishedJob = jobService.loadJobById(jobId);
        assertEquals(COMPLETED, finishedJob.getStatus());

        byte[] output = finishedJob.getOutput();
        assertNotNull(output);

        String reportFilename = new String(output);
        assertTrue(isInTempReportsDir(reportFilename));

        File reportFile = new File(reportFilename);
        assertEquals(String.format("42,abcdef%n"), FileUtils.readFileToString(reportFile));

        reportFile.delete();
    }

    @Test
    public void test_run_failsDueToWrongParameterType() {
        String reportURI = this.getClass().getClassLoader().getResource("reports/testReport1.prpt").toString();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("paramHello", "42"); // wrong type!
        parameters.put("paramWorld", "abcdef");
        ReportDataPojo reportDataPojo = new ReportDataPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportDataPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        ReportingJob failedJob = jobService.loadJobById(jobId);
        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());

        assertTrue(tempReportsDirIsEmpty());
    }

    @Test
    public void test_run_failsDueToMissingReportTemplate() {
        String reportURI = tempReportsDir.getAbsolutePath() + "/doesNotExist.prpt";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("paramHello", 42);
        parameters.put("paramWorld", "abcdef");
        ReportDataPojo reportDataPojo = new ReportDataPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportDataPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        ReportingJob failedJob = jobService.loadJobById(jobId);
        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());

        assertTrue(tempReportsDirIsEmpty());
    }

    @Test
    public void test_getManagedTaskListener() {
        ReportTask task = new ReportTask(null, null, 0);
        assertSame(task, task.getManagedTaskListener());
    }

    @Test
    public void test_getExecutionProperties() {
        ReportTask task = new ReportTask(null, null, 0);
        assertNull(task.getExecutionProperties());
    }

    @Test
    public void test_taskAborted() {
        int jobId = prepareJob();
        ReportTask task = new ReportTask(null, null, jobId);

        task.taskAborted(null, null, task, new Exception());

        ReportingJob failedJob = jobService.loadJobById(jobId);
        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());
    }

    private int prepareJob() {
        ReportingJob newJob = new ReportingJob().setJobType(REPORT).setStatus(PENDING).setQueue("").setInput(null)
                .setOutput(null);
        newJob = jobService.saveJob(newJob);
        return newJob.getJobId();
    }

    private boolean isInTempReportsDir(String filename) {
        return filename.startsWith(tempReportsDir.getAbsolutePath());
    }

    private boolean tempReportsDirIsEmpty() {
        return tempReportsDir.list().length == 0;
    }
}
