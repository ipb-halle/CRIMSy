/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.crimsy_api.DTO;
import de.ipb_halle.job.Job;
import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobType;
import de.ipb_halle.job.JobStatus;
import de.ipb_halle.lbac.admission.User;

import java.io.File;
import java.util.Date;

/**
 * ReportJob
 * Not to be confused with  <code>ReportingJob</code> from the reporting module,
 * which does not care of job ownership.
 *
 * @author fbroda
 */
public class ReportJob extends Job<ReportJob> {
    private User    owner;

    public ReportJob() {
        super();
        setJobType(JobType.REPORT);
    }

    public ReportJob(JobEntity entity, User owner) {
        super(entity);
        if (entity.getJobType() != JobType.REPORT) {
            throw new IllegalArgumentException("Attempt to create REPORT job from non-report-job entity");
        }
        this.owner = owner;
    }
    
    @Override
    public JobEntity createEntity() {
        JobEntity e = super.createEntity();
        if (this.owner != null) {
            e.setOwnerId(this.owner.getId());
        }
        return e;
    }

    @Override
    public Integer getOwnerId() {
        if (this.owner != null) {
            return this.owner.getId();
        }
        return null;
    }

    public User getOwner() {
        return this.owner;
    }

    public ReportJob setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public ReportJob setOwnerId(Integer o) {
        throw new IllegalArgumentException("setting of ownerId not allowed");
    }

    public String getI18nKeyForStatus() {
        JobStatus status = getStatus();
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
        JobStatus status = getStatus();
        if (status == null) {
            return "";
        }
        return "report-" + status.toString().toLowerCase();
    }

    private boolean isFailed() {
        return JobStatus.FAILED.equals(getStatus());
    }

    private boolean isCompleted() {
        return JobStatus.COMPLETED.equals(getStatus());
    }

    private boolean outputFileExists() {
        byte[] output = getOutput();
        if (output == null) {
            return false;
        }
        return new File(new String(output)).exists();
    }
}
