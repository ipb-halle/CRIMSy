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

import java.util.Map;

/**
 * 
 * @author flange
 */
public class ReportJobPojo {
    private final String reportURI;
    private final ReportType type;
    private final Map<String, Object> parameters;

    public ReportJobPojo(String reportURI, ReportType type, Map<String, Object> parameters) {
        this.reportURI = reportURI;
        this.type = type;
        this.parameters = parameters;
    }

    public String getReportURI() {
        return reportURI;
    }

    public ReportType getType() {
        return type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}