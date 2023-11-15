/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.job.JobService;
import de.ipb_halle.lbac.admission.MemberService;

import java.io.Serializable;
import java.util.ArrayList; 
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

/**
 * PrintJobService loads, stores and deletes print jobs.
 */
@Stateless
public class PrintJobService extends JobService<PrintJob> implements Serializable {

    @Inject
    private MemberService memberService;

    @Override
    protected PrintJob buildJob(JobEntity entity) {
        if (entity != null) {
            return new PrintJob(entity, memberService.loadUserById(entity.getOwnerId()));
        }
        return null;
    }

    public PrintJob saveJob(PrintJob job) {
        return new PrintJob(super.saveEntity(job), job.getOwner());
    }
}
