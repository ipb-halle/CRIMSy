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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.jsf.SendFileBean;
import de.ipb_halle.lbac.reporting.ReportJob;
import de.ipb_halle.lbac.reporting.ReportJobService;

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

    private List<ReportJob> reportJobs;

    @PostConstruct
    void init() {
        loadReportJobs();
    }

    private void loadReportJobs() {
        List<ReportJob> newReportJobs = new ArrayList<>();
        for (ReportJob job : reportJobService.loadJobsForUser(userBean.getCurrentAccount())) {
            newReportJobs.add(job);
        }
        reportJobs = newReportJobs;
    }

    /*
     * Actions
     */
    public void actionReloadTable() {
        loadReportJobs();
    }

    public void actionDownloadReport(ReportJob job) throws IOException {
        if (!job.isDownloadable()) {
            return;
        }

        File f = reportJobService.getOutputFileOfJob(job);
        if (f != null) {
            sendFileBean.sendFile(f);
        }
    }

    public void actionDeleteReport(ReportJob job) {
        if (job.isDeleteable()) {
            reportJobService.deleteJob(job);
            loadReportJobs();
        }
    }

    /*
     * Getters
     */
    public List<ReportJob> getReportingJobs() {
        return reportJobs;
    }

    public String getJobStatusI18n(ReportJob job) {
        return messagePresenter.presentMessage(job.getI18nKeyForStatus());
    }
}
