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

import static de.ipb_halle.lbac.device.job.JobService.CONDITION_JOBTYPE;
import static de.ipb_halle.lbac.device.job.JobService.CONDITION_OWNERID;
import static de.ipb_halle.lbac.device.job.JobType.REPORT;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

/**
 * 
 * @author flange
 */
@Named
@SessionScoped
public class ReportingJobsBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private JobService jobService;

    @Inject
    private SendFileBean sendFileBean;

    @Inject
    private UserBean userBean;

    @Inject
    private transient MessagePresenter messagePresenter;

    private List<ReportingJobWapper> reportingJobs;

    @PostConstruct
    void init() {
        loadReportingJobs();
    }

    private void loadReportingJobs() {
        List<ReportingJobWapper> newReportingJobs = new ArrayList<>();

        Map<String, Object> cmap = new HashMap<>();
        cmap.put(CONDITION_JOBTYPE, REPORT);
        cmap.put(CONDITION_OWNERID, userBean.getCurrentAccount().getId());
        for (Job job : jobService.load(cmap)) {
            newReportingJobs.add(new ReportingJobWapper(job));
        }
        reportingJobs = newReportingJobs;
    }

    /*
     * Actions
     */
    public void actionReloadTable() {
        loadReportingJobs();
    }

    public void actionDownloadReport(ReportingJobWapper job) throws IOException {
        if (job.isDownloadable()) {
            sendFileBean.sendFile(job.getOutputFile());
        }
    }

    public void actionDeleteReport(ReportingJobWapper job) {
        if (job.isDeleteable()) {
            jobService.remove(job.getJobId());
            job.getOutputFile().delete();
            loadReportingJobs();
        }
    }

    /*
     * Getters
     */
    public List<ReportingJobWapper> getReportingJobs() {
        return reportingJobs;
    }

    public String getJobStatus(ReportingJobWapper job) {
        return messagePresenter.presentMessage(job.getI18nKeyForStatus());
    }
}