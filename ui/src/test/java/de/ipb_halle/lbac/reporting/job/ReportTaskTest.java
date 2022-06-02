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

import static de.ipb_halle.lbac.device.job.JobStatus.COMPLETED;
import static de.ipb_halle.lbac.device.job.JobStatus.FAILED;
import static de.ipb_halle.lbac.device.job.JobStatus.PENDING;
import static de.ipb_halle.lbac.device.job.JobType.REPORT;
import static de.ipb_halle.lbac.reporting.report.ReportType.CSV;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportTaskTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private JobService jobService;

    @TempDir
    private static File tempReportsDir;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ReportTaskTest.war").addClasses(ReportJobService.class, JobService.class);
    }

    @Test
    public void test_run_generatesReport() throws IOException {
        String reportURI = this.getClass().getClassLoader().getResource("reports/testReport1.prpt").toString();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("paramHello", 42);
        parameters.put("paramWorld", "abcdef");
        ReportJobPojo reportJobPojo = new ReportJobPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportJobPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        Job finishedJob = jobService.loadJobById(jobId);
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
        ReportJobPojo reportJobPojo = new ReportJobPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportJobPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        Job failedJob = jobService.loadJobById(jobId);
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
        ReportJobPojo reportJobPojo = new ReportJobPojo(reportURI, CSV, parameters);
        int jobId = prepareJob();
        ReportTask task = new ReportTask(reportJobPojo, tempReportsDir.getAbsolutePath(), jobId);

        task.run();

        Job failedJob = jobService.loadJobById(jobId);
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

        Job failedJob = jobService.loadJobById(jobId);
        assertEquals(FAILED, failedJob.getStatus());
        assertNull(failedJob.getOutput());
    }

    private int prepareJob() {
        Job newJob = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(adminUser).setQueue("").setInput(null)
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