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
package de.ipb_halle.lbac.reporting.jobview;

import java.io.File;
import java.util.Date;

import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobStatus;

/**
 * 
 * @author flange
 */
public class ReportingJobWapper {
    private final Job job;

    public ReportingJobWapper(Job job) {
        this.job = job;
    }

    public Integer getJobId() {
        return job.getJobId();
    }

    public Date getSubmissionDate() {
        return job.getJobdate();
    }

    public String getI18nKeyForStatus() {
        JobStatus status = job.getStatus();
        if (status == null) {
            return "";
        }
        return "jobStatus_" + status.toString();
    }

    public File getOutputFile() {
        byte[] output = job.getOutput();
        if (output == null) {
            return null;
        }
        String outputFile = new String(output);
        return new File(outputFile);
    }

    public boolean isDownloadable() {
        boolean isNotComplete = !isCompleted();
        if (isNotComplete) {
            return false;
        }
        return getOutputFile().exists();
    }

    public boolean isDeleteable() {
        return isFailed() || isCompleted();
    }

    private boolean isFailed() {
        return JobStatus.FAILED.equals(job.getStatus());
    }

    private boolean isCompleted() {
        return JobStatus.COMPLETED.equals(job.getStatus());
    }
}
