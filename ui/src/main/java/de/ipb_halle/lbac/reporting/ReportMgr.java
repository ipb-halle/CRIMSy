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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.reporting.Report;
import de.ipb_halle.reporting.ReportDataPojo;
import de.ipb_halle.reporting.ReportService;
import de.ipb_halle.reporting.ReportType;

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

import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fbroda
 */
@RequestScoped
public class ReportMgr {

    private static final String DEFAULT_LANGUAGE = "en";
    static final String DEFAULT_NAME = "Report with no name";

    private Logger logger = LogManager.getLogger(getClass().getName());

    @Inject
    private ReportService reportService;

    @Inject
    private ReportJobService reportJobService;

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
        if (!jsonTree.isJsonObject()) {
            return DEFAULT_NAME;
        }

        String name = extractNameFromLanguageElement(jsonTree.getAsJsonObject().get(preferredLanguage));
        if (name != null) {
            return name;
        }

        // resort to name in default language
        name = extractNameFromLanguageElement(jsonTree.getAsJsonObject().get(DEFAULT_LANGUAGE));
        if (name != null) {
            return name;
        }

        return DEFAULT_NAME;
    }

    private String extractNameFromLanguageElement(JsonElement languageElement) {
        if (languageElement == null) {
            return null;
        }

        String name = languageElement.getAsString();
        if ((name == null) || name.equals("")) {
            return null;
        }

        return name;
    }

    /**
     * Submit a report to the report generator.
     *
     * @param report report template description
     * @param parameters map of report parameters
     * @param type type of report
     * @param currentUser current user
     */
    public void submitReport(Report report, Map<String, Object> parameters, ReportType type, User currentUser) {
        ReportDataPojo pojo = new ReportDataPojo(report.getSource(), type, parameters);
        reportJobService.submit(pojo, currentUser);
    }
}
