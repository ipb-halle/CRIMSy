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

import de.ipb_halle.reporting.ReportDataPojo;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Future;

import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.enterprise.concurrent.ManagedTaskListener;
import jakarta.enterprise.inject.spi.CDI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Task that generates reports using Pentaho. It is associated to a reporting
 * job.
 * 
 * @author flange
 */
public class ReportTask implements Runnable, ManagedTask, ManagedTaskListener {
    private Logger logger = LogManager.getLogger(getClass().getName());
    private final ReportDataPojo reportDataPojo;
    private final String reportsDir;
    private final int jobId;

    public ReportTask(ReportDataPojo reportDataPojo, String reportsDir, int jobId) {
        this.reportDataPojo = reportDataPojo;
        this.reportsDir = reportsDir;
        this.jobId = jobId;
    }

    /**
     * Generates a report using Pentaho. If successful, it marks the associated job
     * as completed. If unsuccessful, it marks the job as failed.
     */
    @Override
    public void run() {
        File reportFile = null;
        String reportFilePath = null;

        try {
            reportFile = File.createTempFile("report", reportDataPojo.getType().getFileExtension(), getReportDir());
            reportFilePath = reportFile.getAbsolutePath();
            URL url = new URL(reportDataPojo.getReportURI());

            ClassicEngineBoot.getInstance().start();
            ResourceManager manager = new ResourceManager();
            manager.registerDefaults();
            Resource resource = manager.createDirectly(url, MasterReport.class);
            MasterReport report = (MasterReport) resource.getResource();

            for (Entry<String, Object> entry : reportDataPojo.getParameters().entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();

                report.getParameterValues().put(paramName, paramValue);
            }
            // This circumvents Pentaho's caching.
            report.getParameterValues().put("paramRandom", new Random().nextLong());

            ReportBuilder.createReport(report, reportFilePath, reportDataPojo.getType());
        } catch (Exception e) {
            if ((reportFile != null) && (reportFile.exists())) {
                reportFile.delete();
            }

            fail(e);
            return;
        }

        done(reportFilePath);
    }

    private File getReportDir() throws IOException {
        // createDirectories() does not fail in case the directory already exists.
        return Files.createDirectories(Paths.get(reportsDir)).toFile();
    }

    private void done(String reportFilePath) {
        reportingJobService().markJobAsCompleted(jobId, reportFilePath);
    }

    private void fail(Throwable exception) {
        logger.warn(String.format("Task with jobId=%d failed: ", jobId), (Throwable) exception);
        reportingJobService().markJobAsFailed(jobId);
    }

    private ReportingJobService reportingJobService() {
        return CDI.current().select(ReportingJobService.class).get();
    }

    @Override
    public ManagedTaskListener getManagedTaskListener() {
        return this;
    }

    @Override
    public Map<String, String> getExecutionProperties() {
        return null;
    }

    @Override
    public void taskSubmitted(Future<?> future, ManagedExecutorService executor, Object task) {
    }

    @Override
    public void taskStarting(Future<?> future, ManagedExecutorService executor, Object task) {
    }

    /**
     * Called when the ManagedExecutorService decides to abort the task. This marks
     * the associated job as failed.
     */
    @Override
    public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        fail(exception);
    }

    @Override
    public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
    }
}
