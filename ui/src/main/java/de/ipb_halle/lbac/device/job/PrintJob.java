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
package de.ipb_halle.lbac.device.job;

import de.ipb_halle.job.Job;
import de.ipb_halle.job.JobEntity;
import de.ipb_halle.job.JobType;
import de.ipb_halle.job.NetJob;
import de.ipb_halle.lbac.admission.User;


/**
 * PrintJob 
 *
 * @author fbroda
 */
public class PrintJob extends Job {
    private User    owner;

    public PrintJob() {
        super();
        setJobType(JobType.PRINT);
    }

    public PrintJob(JobEntity entity, User owner) {
        super(entity);
        if (entity.getJobType() != JobType.PRINT) {
            throw new IllegalArgumentException("Attempt to create PRINT job from non-print-job entity");
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


    public User getOwner() {
        return this.owner;
    }

    public PrintJob setOwner(User owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public void update(NetJob netJob) {
        if (netJob.getJobType() != JobType.PRINT) {
            throw new IllegalArgumentException("Attempt to update PRINT job from non-print-job NetJob");
        }
        super.update(netJob);
    }
}
