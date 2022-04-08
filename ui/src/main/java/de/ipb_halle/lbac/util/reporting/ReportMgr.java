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
package de.ipb_halle.lbac.util.reporting;

import de.ipb_halle.lbac.util.jsf.SendFileBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Create Pentaho report 
 *
 * @author fbroda
 */
@RequestScoped
public class ReportMgr {

    @Inject
    private ReportService reportService;

    @Inject
    private SendFileBean sendFileBean;

    @Resource
    private ManagedExecutorService executor;

    private transient Logger logger;

    public ReportMgr() {
        logger = LogManager.getLogger(getClass().getName());
    }

    public List<SelectItem> getReports(String context) {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot()
            .getLocale();
        return reportService.load(context, locale.getLanguage());
    }

    public List<SelectItem> getReportTypes() {
        List<SelectItem> types = new ArrayList<> ();
        for (ReportType t : ReportType.class.getEnumConstants()) {
            types.add(new SelectItem(t.toString()));
        }
        return types;
    }

    /**
     * prepare a report and deliver it to the user
     * @param id id of the report template
     * @param parameters map of report parameters
     * @param type type of report (PDF, XLSX, CSV)
     */
    public void prepareReport(Integer id, Map<String, Object> parameters, ReportType type) {

        Report report = reportService.loadById(id);
        report.setParameters(parameters);
        report.setType(type);
        File tmpFile = null;
        try {
            String ext = ".tmp";
            switch(type) {
                case PDF:
                    ext = ".pdf";
                    break;
                case CSV:
                    ext = ".csv";
                    break;
                case XLSX:
                    ext = ".xlsx";
                    break;
            }
            tmpFile = File.createTempFile("report", ext);
            report.setFileName(tmpFile.getAbsolutePath());
            report.run();
            sendFileBean.sendFile(tmpFile);

        } catch(Exception e) {
            this.logger.warn("prepare Report caught an exception: ", (Throwable) e);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
}
