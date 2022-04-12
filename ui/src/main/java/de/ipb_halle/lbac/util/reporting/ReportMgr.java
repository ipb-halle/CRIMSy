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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Faces;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 * Create Pentaho report
 *
 * @author fbroda
 */
@RequestScoped
public class ReportMgr {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_NAME = "Report with no name";

    @Inject
    private ReportService reportService;

    @Inject
    private SendFileBean sendFileBean;

    private Logger logger = LogManager.getLogger(getClass().getName());

    public List<Report> getAvailableReports(String context) {
        List<Report> reports = reportService.loadByContext(context);

        for (Report report : reports) {
            localizeReportName(report);
        }
        reports.sort(Comparator.comparing(Report::getName));

        return reports;
    }

    private void localizeReportName(Report report) throws JsonSyntaxException {
        String targetLanguage;
        if (Faces.getContext() != null) {
            targetLanguage = Faces.getLocale().getLanguage();
        } else {
            targetLanguage = DEFAULT_LANGUAGE;
        }
        String json = report.getName();

        String localizedName = DEFAULT_NAME;
        try {
            localizedName = extractNameFromJson(json, targetLanguage);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to obtain localized [{}] name for report [{}]: {}", targetLanguage, report.getId(),
                    e.getMessage());
        }

        report.setName(localizedName);
    }

    private String extractNameFromJson(String json, String preferredLanguage) throws JsonSyntaxException {
        JsonElement jsonTree = JsonParser.parseString(json);

        String name = jsonTree.getAsJsonObject().get(preferredLanguage).getAsString();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        // resort to name in default language
        name = jsonTree.getAsJsonObject().get(DEFAULT_LANGUAGE).getAsString();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }

        return DEFAULT_NAME;
    }

    /**
     * prepare a report and deliver it to the user
     * 
     * @param id         id of the report template
     * @param parameters map of report parameters
     * @param type       type of report (PDF, XLSX, CSV)
     */
    public void prepareReport(Report report, Map<String, Object> parameters, ReportType type) {
        report.setParameters(parameters);
        report.setType(type);
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("report", type.getFileExtension());
            report.setFileName(tmpFile.getAbsolutePath());
            report.run();
            sendFileBean.sendFile(tmpFile);
        } catch (Exception e) {
            this.logger.warn("prepare Report caught an exception: ", (Throwable) e);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }
}
