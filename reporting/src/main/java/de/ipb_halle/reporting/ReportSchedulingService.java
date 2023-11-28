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

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Timer service for maintaining reporting jobs.
 * 
 * @author flange
 */
@Singleton
@Startup
public class ReportSchedulingService {

    private Logger logger = LogManager.getLogger(ReportSchedulingService.class.getName());

    @Inject
    private ReportingJobService reportJobService;

    /**
     * Marks all busy reporting jobs as pending to ensure that previously unfinished
     * reporting jobs are restarted.
     */
    @PostConstruct
    public void startUp() {
        logger.info("ReportSchedulingService has been started ...");
        reportJobService.markBusyJobsAsPending();

        // Could cause heavy load on application startup?
//        reportJobService.submitPendingJobsToExecutor();
    }

    /**
     * Frequently submits pending reporting jobs to the ManagedExecutorService.
     */
    @Schedule(second = "0", minute = "*", hour = "*")
    public void submitPendingJobsToExecutor() {
        reportJobService.submitPendingJobsToExecutor();
    }

    /**
     * Removes old reporting jobs and cleans orphaned files in the report directory.
     */
    @Schedule(second = "0", minute = "0", hour = "0")
    public void cleanUpOldJobsAndReportFiles() {
        reportJobService.cleanUpOldJobsAndReportFiles();
    }
}
