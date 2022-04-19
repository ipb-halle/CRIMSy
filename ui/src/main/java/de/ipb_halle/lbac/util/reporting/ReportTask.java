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

import java.util.Map;
import java.util.concurrent.Future;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;
import javax.enterprise.inject.spi.CDI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author flange
 */
public class ReportTask implements Runnable, ManagedTask, ManagedTaskListener {
    private Logger logger = LogManager.getLogger(getClass().getName());
    private final ReportJobPojo reportJobPojo;
    private final int jobId;

    public ReportTask(ReportJobPojo reportJobPojo, int jobId) {
        this.reportJobPojo = reportJobPojo;
        this.jobId = jobId;
    }

    @Override
    public void run() {
        String tempFilePath = null;

        try {
            // Pentaho
        } catch (Exception e) {
            fail(e);
        }

        done(tempFilePath);
    }

    private void done(String tempFilePath) {
        reportJobService().markJobAsCompleted(jobId, tempFilePath);
    }

    private void fail(Throwable exception) {
        logger.warn("Task with jobId={} failed: {}", jobId, exception);
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