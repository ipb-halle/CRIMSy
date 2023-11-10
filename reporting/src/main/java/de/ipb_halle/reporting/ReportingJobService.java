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

import static de.ipb_halle.job.JobService.CONDITION_JOBTYPE;
import static de.ipb_halle.job.JobService.CONDITION_OWNERID;
import static de.ipb_halle.job.JobService.CONDITION_STATUS;
import static de.ipb_halle.job.JobStatus.BUSY;
import static de.ipb_halle.job.JobStatus.COMPLETED;
import static de.ipb_halle.job.JobStatus.FAILED;
import static de.ipb_halle.job.JobStatus.PENDING;
import static de.ipb_halle.job.JobType.REPORT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobService;

/**
 * 
 * @author flange
 */
@Stateless
public class ReportingJobService extends JobService<ReportingJob> {
    private Logger logger = LogManager.getLogger(getClass().getName());

    /**
     * Maximum age of reporting jobs in the database and files in the reports
     * directory.
     */
    static final Duration MAX_AGE = Duration.ofDays(7);

    @Resource(name = "reportExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private ReportsDirectory reportsDirectory;


    protected ReportingJob buildJob(JobEntity e) {
        if (e != null) {
            return new ReportingJob(e);
        }
        return null;
    }

    /**
     * Marks all busy jobs as pending. Use only at startup of the application to
     * ensure that previously unfinished reporting jobs are restarted.
     */
    public void markBusyJobsAsPending() {
        for (ReportingJob job : busyJobs()) {
            markJobAsPending(job);
        }
    }

    /**
     * Tries to submit all pending tasks to the ManagedExecutorService, but stops
     * this activity as soon as the ManagedExecutorService cannot accept any new
     * tasks.
     */
    public void submitPendingJobsToExecutor() {
        for (ReportingJob job : pendingJobs()) {
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
    public ReportingJob markJobAsCompleted(int jobId, String reportFilePath) {
        ReportingJob job = loadJobById(jobId);
        if (job == null) {
            return null;
        }

        job.setStatus(COMPLETED).setOutput(reportFilePath.getBytes());
        return saveJob(job);
    }

    /**
     * Marks the job with the given ID as failed.
     * 
     * @param jobId
     * @return the job DTO
     */
    public ReportingJob markJobAsFailed(int jobId) {
        ReportingJob job = loadJobById(jobId);
        if (job == null) {
            return null;
        }

        job.setStatus(FAILED);
        return saveJob(job);
    }

    /**
     * Delete the given reporting job and its report file.
     * 
     * @param job
     */
    public void deleteJob(ReportingJob job) {
        removeJob(job);
        byte[] output = job.getOutput();
        if (output != null) {
            deleteFileIfExists(new String(output));
        }
    }

    /**
     * Request the report file of the given reporting job.
     * 
     * @param job
     * @return report file or null in case the report job does not exist in the
     *         database, does not have a report file or the file does not exist
     */
    public File getOutputFileOfJob(ReportingJob job) {
        ReportingJob jobFromDB = loadJobById(job.getJobId());
        if (jobFromDB == null) {
            return null;
        }

        byte[] output = jobFromDB.getOutput();
        if (output == null) {
            return null;
        }

        String outputFile = new String(output);
        File f = new File(outputFile);
        if (!f.exists()) {
            return null;
        }

        return f;
    }

    /**
     * Removes old reporting jobs and cleans orphaned files in the report directory.
     */
    public void cleanUpOldJobsAndReportFiles() {
        for (ReportingJob job : oldJobs()) {
            deleteJob(job);
        }

        try {
            deleteOrphanedReportFiles();
        } catch (IOException e) {
            logger.error("{}", e);
        }
    }

    private ReportTask prepareTask(ReportingJob job) {
        ReportDataPojo reportDataPojo = ReportDataPojo.deserialize(job.getInput());
        return new ReportTask(reportDataPojo, getReportsDirectory(), job.getJobId());
    }

    private boolean submitJob(ReportingJob job) {
        try {
            ReportTask task = prepareTask(job);
            managedExecutorService.submit(task);
        } catch (RejectedExecutionException e) {
            return false;
        }
        markJobAsBusy(job);
        return true;
    }

    // default access for testing
    String getReportsDirectory() {
        return reportsDirectory.getReportsDirectory();
    }


    private List<ReportingJob> pendingJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, PENDING);
        return loadJobs(cmap);
    }

    private List<ReportingJob> busyJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_STATUS, BUSY);
        return loadJobs(cmap);
    }

    private ReportingJob markJobAsPending(ReportingJob job) {
        job.setStatus(PENDING);
        return saveJob(job);
    }

    private ReportingJob markJobAsBusy(ReportingJob job) {
        job.setStatus(BUSY);
        return saveJob(job);
    }

    private List<ReportingJob> oldJobs() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        return loadJobsOlderThan(Date.from(Instant.now().minus(MAX_AGE)), cmap);
    }

    private void deleteFileIfExists(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    private void deleteOrphanedReportFiles() throws IOException {
        Instant cutoff = Instant.now().minus(MAX_AGE);

        // from https://stackoverflow.com/a/46791681
        Files.list(Paths.get(getReportsDirectory())).filter(path -> {
            try {
                return Files.isRegularFile(path) && Files.getLastModifiedTime(path).toInstant().isBefore(cutoff);
            } catch (IOException e) {
                logger.error("{}", e);
                return false;
            }
        }).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                logger.error("{}", e);
            }
        });
    }

    public void replaceManagedExecutorService(ManagedExecutorService managedExecutorService) {
        this.managedExecutorService = managedExecutorService;
    }

    public void replaceReportsDirectory(ReportsDirectory rp) {
        this.reportsDirectory = rp;
    }
}
