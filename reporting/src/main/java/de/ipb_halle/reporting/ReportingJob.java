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
package de.ipb_halle.reporting;

import de.ipb_halle.job.Job;
import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobStatus;
import de.ipb_halle.job.JobType;
import de.ipb_halle.job.NetJob;

import java.io.File;
import java.util.Date;

/**
 * ReportingJob
 * Not to be confused with <code>ReportJob</code> from the ui module, 
 * which explicitly adds a user / owner.
 *
 * @author fbroda
 */
public class ReportingJob extends Job<ReportingJob> {

    public ReportingJob() {
        super();
        setJobType(JobType.REPORT);
    }

    public ReportingJob(JobEntity entity) {
        super(entity);
        if (entity.getJobType() != JobType.REPORT) {
            throw new IllegalArgumentException("Attempt to create PRINT job from non-report-job entity");
        }
    }
    
    @Override
    public JobEntity createEntity() {
        JobEntity e = super.createEntity();
        return e;
    }

    @Override
    public void update(NetJob netjob) {
        if (netjob.getJobType() != JobType.REPORT) {
            throw new IllegalArgumentException("Attempt to update PRINT job from non-report-job NetJob");
        }
        super.update(netjob);
    }


    public boolean isDeleteable() {
        return isFailed() || isCompleted();
    }


    private boolean isFailed() {
        return JobStatus.FAILED.equals(getStatus());
    }

    private boolean isCompleted() {
        return JobStatus.COMPLETED.equals(getStatus());
    }
}
