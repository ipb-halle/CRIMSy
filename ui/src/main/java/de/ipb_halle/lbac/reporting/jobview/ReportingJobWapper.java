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
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobStatus;

/**
 * Wrapper class for reporting job objects.
 * 
 * @author flange
 */
public class ReportingJobWapper {
    private final Job job;

    public ReportingJobWapper(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public String getI18nKeyForStatus() {
        JobStatus status = job.getStatus();
        if (status == null) {
            return "";
        }
        return "jobStatus_" + status.toString();
    }

    public boolean isDownloadable() {
        return isCompleted() && outputFileExists();
    }

    public boolean isDeleteable() {
        return isFailed() || isCompleted();
    }

    public String getRowStyleClass() {
        JobStatus status = job.getStatus();
        if (status == null) {
            return "";
        }
        return "report-" + status.toString().toLowerCase();
    }

    private boolean isFailed() {
        return JobStatus.FAILED.equals(job.getStatus());
    }

    private boolean isCompleted() {
        return JobStatus.COMPLETED.equals(job.getStatus());
    }

    private boolean outputFileExists() {
        byte[] output = job.getOutput();
        if (output == null) {
            return false;
        }
        return new File(new String(output)).exists();
    }
}
