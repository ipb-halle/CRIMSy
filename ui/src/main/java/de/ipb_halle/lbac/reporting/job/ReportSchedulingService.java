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

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * 
 * @author flange
 */
@Singleton
@Startup
@DependsOn("globalAdmissionContext")
public class ReportSchedulingService {
    @Inject
    private ReportJobService reportJobService;

    /**
     * Marks all busy reporting jobs as pending to ensure that previously unfinished
     * reporting jobs are restarted.
     */
    @PostConstruct
    void startUp() {
        reportJobService.markBusyJobsAsPending();

        // Could cause heavy load on application startup?
        // reportJobService.submitPendingTasksToExecutor();
    }

    /**
     * Frequently submits pending reporting jobs to the ManagedExecutorService.
     */
    @Schedule(second = "0", minute = "*", hour = "*")
    void submitPendingTasksToExecutor() {
        reportJobService.submitPendingTasksToExecutor();
    }

    /**
     * Removes old reporting jobs and cleans orphaned files in the temporary report
     * directory.
     */
    @Schedule(second = "0", minute = "0", hour = "0")
    void cleanUp() {

    }
}