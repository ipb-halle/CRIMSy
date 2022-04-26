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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;

/**
 * 
 * @author flange
 */
@Stateless
public class ReportJobService {
    @Resource(name = "reportExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private JobService jobService;

    /**
     * Create a new pending reporting job and try to submit it to the
     * ManagedExecutorService immediately.
     * 
     * @param reportJobPojo
     * @param owner
     */
    public void submit(ReportJobPojo reportJobPojo, User owner) {
        Job newJob = new Job().setJobType(REPORT).setStatus(PENDING).setOwner(owner).setQueue("")
                .setInput(serialize(reportJobPojo));
        newJob = jobService.save(newJob);

        submitJob(newJob);
    }

    /**
     * Marks all busy jobs as pending. Use only at startup of the application to
     * ensure that previously unfinished reporting jobs are restarted.
     */
    public void markBusyJobsAsPending() {
        for (Job job : busyJobs()) {
            markJobAsPending(job);
        }
    }

    /**
     * Tries to submit all pending tasks to the ManagedExecutorService, but stops
     * this activity as soon as the ManagedExecutorService cannot accept any new
     * tasks.
     */
    public void submitPendingTasksToExecutor() {
        for (Job job : pendingJobs()) {
            boolean submitSuccessful = submitJob(job);
            if (!submitSuccessful) {
                break;
            }
        }
    }

    /**
     * Marks the job with the given ID as complete.
     * 
     * @param jobId
     * @param reportFilePath
     * @return the job DTO
     */
    public Job markJobAsCompleted(int jobId, String reportFilePath) {
        Job job = jobService.loadById(jobId);
        if (job == null) {
            return null;
        }

        job.setStatus(COMPLETED).setOutput(reportFilePath.getBytes());
        return jobService.save(job);
    }

    /**
     * Marks the job with the given ID as failed.
     * 
     * @param jobId
     * @return the job DTO
     */
    public Job markJobAsFailed(int jobId) {
        Job job = jobService.loadById(jobId);
        if (job == null) {
            return null;
        }

        job.setStatus(FAILED);
        return jobService.save(job);
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
        ReportJobPojo reportJobPojo = (ReportJobPojo) deserialize(job.getInput());
        return new ReportTask(reportJobPojo, globalAdmissionContext.getReportsDirectory(), job.getJobId());
    }

    private byte[] serialize(Object o) {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
            oos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    private Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Object o = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            o = ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return o;
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

    // Replace managedExecutorService in tests.
    public void setManagedExecutorService(ManagedExecutorService managedExecutorService) {
        this.managedExecutorService = managedExecutorService;
    }
}