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
package de.ipb_halle.lbac.util.reporting;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.enterprise.inject.spi.CDI;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * 
 * @author flange
 */
public class ReportTask implements Runnable, ManagedTask, ManagedTaskListener {
    private Logger logger = LogManager.getLogger(getClass().getName());
    private final ReportJobPojo reportJobPojo;
    private final String reportsDir;
    private final int jobId;

    public ReportTask(ReportJobPojo reportJobPojo, String reportsDir, int jobId) {
        this.reportJobPojo = reportJobPojo;
        this.reportsDir = reportsDir;
        this.jobId = jobId;
    }

    @Override
    public void run() {
        File reportFile = null;
        String reportFilePath = null;

        try {
            reportFile = File.createTempFile("report", reportJobPojo.getType().getFileExtension(), getReportDir());
            reportFilePath = reportFile.getAbsolutePath();
            URL url = new URL(reportJobPojo.getReportURI());

            ClassicEngineBoot.getInstance().start();
            ResourceManager manager = new ResourceManager();
            manager.registerDefaults();
            Resource resource = manager.createDirectly(url, MasterReport.class);
            MasterReport report = (MasterReport) resource.getResource();

            for (Entry<String, Object> entry : reportJobPojo.getParameters().entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();

                report.getParameterValues().put(paramName, paramValue);
            }

            reportJobPojo.getType().createReport(report, reportFilePath);
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
        reportJobService().markJobAsCompleted(jobId, reportFilePath);
    }

    private void fail(Throwable exception) {
        logger.warn("Task with jobId={} failed: {}", jobId, ExceptionUtils.getStackTrace(exception));
        reportJobService().markJobAsFailed(jobId);
    }

    private ReportJobService reportJobService() {
        return CDI.current().select(ReportJobService.class).get();
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

    @Override
    public void taskAborted(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
        fail(exception);
    }

    @Override
    public void taskDone(Future<?> future, ManagedExecutorService executor, Object task, Throwable exception) {
    }
}