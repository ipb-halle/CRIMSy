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
package de.ipb_halle.lbac.reporting;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobService;
import de.ipb_halle.reporting.ReportDataPojo;

/**
 * 
 * @author flange
 */
@Stateless
public class ReportJobService extends JobService<ReportJob> {
    private Logger logger = LogManager.getLogger(getClass().getName());

    /**
     * Maximum age of reporting jobs in the database and files in the reports
     * directory.
     */
    static final Duration MAX_AGE = Duration.ofDays(7);

    @Inject
    private MemberService memberService;

    @Override
    protected ReportJob buildJob(JobEntity entity) {
        if (entity != null) {
            return new ReportJob(entity, memberService.loadUserById(entity.getOwnerId()));
        }
        return null;
    }

    /**
     * Delete the given reporting job and its report file.
     * 
     * @param job
     */
    public void deleteJob(ReportJob job) {
        removeJob(job);
        byte[] output = job.getOutput();
        if (output != null) {
            deleteFileIfExists(new String(output));
        }
    }


    private void deleteFileIfExists(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Request the report file of the given reporting job.
     * 
     * @param job
     * @return report file or null in case the report job does not exist in the
     *         database, does not have a report file or the file does not exist
     */
    public File getOutputFileOfJob(ReportJob job) {
        ReportJob jobFromDB = loadJobById(job.getJobId());
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

    public List<ReportJob> loadJobsForUser(User u) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_OWNERID, u.getId());

        ArrayList<ReportJob> jobs = new ArrayList<> ();
        for (ReportJob j : loadJobs(cmap)) {
            jobs.add(new ReportJob(j.createEntity(), memberService.loadUserById(j.getOwnerId())));
        }
        return jobs;
    }

    /**
     * Create a new pending reporting job
     * 
     * @param reportDataPojo
     * @param owner
     * @return the ReportJob object
     */
    public ReportJob submit(ReportDataPojo reportDataPojo, User owner) {
        ReportJob newJob = new ReportJob().setOwner(owner).setStatus(PENDING).setQueue("")
                .setInput(reportDataPojo.serialize());
        return saveJob(newJob);
    }
}
