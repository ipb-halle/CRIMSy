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

import static de.ipb_halle.lbac.device.job.JobService.CONDITION_JOBTYPE;
import static de.ipb_halle.lbac.device.job.JobService.CONDITION_STATUS;
import static de.ipb_halle.lbac.device.job.JobStatus.BUSY;
import static de.ipb_halle.lbac.device.job.JobStatus.COMPLETED;
import static de.ipb_halle.lbac.device.job.JobStatus.FAILED;
import static de.ipb_halle.lbac.device.job.JobStatus.PENDING;
import static de.ipb_halle.lbac.device.job.JobType.REPORT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;

/**
 * 
 * @author flange
 */
@Singleton
@Startup
public class ReportJobService {
    private Logger logger = LogManager.getLogger(getClass().getName());
    private Gson gson = new Gson(); // Gson is thread-safe

    @Resource(name = "reportExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private JobService jobService;

    /**
     * Marks all existing busy reporting jobs as pending upon application startup.
     */
    @PostConstruct
    void startUp() {
        markBusyJobsAsPending();

        // Could cause heavy load on application startup?
        // submitTasksToExecutor();
    }

    private void markBusyJobsAsPending() {
        for (Job job : busyJobs()) {
            markJobAsPending(job);
        }
    }

    /**
     * Tries to submit all pending tasks to the ManagedExecutorService, but stops
     * this activity as soon as the ManagedExecutorService cannot accept any new
     * tasks.
     */
    @Schedule(second = "0", minute = "*", hour = "*")
    void submitTasksToExecutor() {
        for (Job job : pendingJobs()) {
            boolean submitSuccessful = submitJob(job);
            if (!submitSuccessful) {
                break;
            }
        }
    }

    /**
     * Tries to submit a report job to the ManagedExecutorService and marks it as
     * busy in case the submission was successful.
     * 
     * @param job report job
     * @return if the job could be submitted successfully
     */
    private boolean submitJob(Job job) {
        try {
            ReportTask task = prepareTask(job);
            managedExecutorService.submit(task);
        } catch (RejectedExecutionException e) {
            return false;
        }
        markJobAsBusy(job);
        return true;
    }

    private ReportTask prepareTask(Job job) {
        String json = new String(job.getInput());
        ReportJobPojo reportJobPojo = gson.fromJson(json, ReportJobPojo.class);
        return new ReportTask(reportJobPojo, job.getJobId());
    }

    /**
     * Submit a new reporting job and try to submit it to the ManagedExecutorService
     * immediately.
     * 
     * @param report
     * @param owner
     */
    public void submit(ReportJobPojo reportJobPojo, User owner) {
        Job newJob = new Job();
        newJob.setJobType(REPORT);
        newJob.setStatus(PENDING);
        newJob.setOwner(owner);
        String inputJson = toJson(reportJobPojo);
        newJob.setInput(inputJson.getBytes());
        newJob = jobService.save(newJob);

        submitJob(newJob);
    }

    private String toJson(ReportJobPojo pojo) {
        return gson.toJson(pojo);
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

    private Job markJobAsPending(Job job) {
        job.setStatus(PENDING);
        return jobService.save(job);
    }

    private Job markJobAsBusy(Job job) {
        job.setStatus(BUSY);
        return jobService.save(job);
    }

    Job markJobAsCompleted(int jobId, String reportFilePath) {
        Job job = jobService.loadById(jobId);
        job.setStatus(COMPLETED);
        job.setOutput(reportFilePath.getBytes());
        return jobService.save(job);
    }

    Job markJobAsFailed(int jobId) {
        Job job = jobService.loadById(jobId);
        job.setStatus(FAILED);
        return jobService.save(job);
    }
}