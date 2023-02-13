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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.reporting.job.ReportJobService;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

/**
 * Controller for myReports.xhtml
 * 
 * @author flange
 */
@Named
@RequestScoped
public class ReportingJobsBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportJobService reportJobService;

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
        for (Job job : reportJobService.loadJobsForUser(userBean.getCurrentAccount())) {
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

    public void actionDownloadReport(ReportingJobWapper wrapper) throws IOException {
        if (!wrapper.isDownloadable()) {
            return;
        }

        File f = reportJobService.getOutputFileOfJob(wrapper.getJob());
        if (f != null) {
            sendFileBean.sendFile(f);
        }
    }

    public void actionDeleteReport(ReportingJobWapper wrapper) {
        if (wrapper.isDeleteable()) {
            reportJobService.deleteJob(wrapper.getJob());
            loadReportingJobs();
        }
    }

    /*
     * Getters
     */
    public List<ReportingJobWapper> getReportingJobs() {
        return reportingJobs;
    }

    public String getJobStatusI18n(ReportingJobWapper wrapper) {
        return messagePresenter.presentMessage(wrapper.getI18nKeyForStatus());
    }
}
