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

import de.ipb_halle.lbac.entity.DTO;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Create Pentaho report
 *
 * @author fbroda
 */
public class Report implements Runnable, DTO {
    private Integer id;
    private String context;
    private Logger logger;
    private String name;
    private String source;
    private ReportType type;
    private String fileName;
    private Map<String, Object> parameters;

    public Report(ReportEntity entity) {
        id = entity.getId();
        context = entity.getContext();
        name = entity.getName();
        source = entity.getSource();
        type = ReportType.PDF;
        logger = LogManager.getLogger(getClass().getName());
    }

    @Override
    public ReportEntity createEntity() {
        return new ReportEntity()
                .setId(id)
                .setContext(context)
                .setName(name)
                .setSource(source);
    }

    /**
     * prepare a report and deliver it to the user
     */
    public void run() {

        try {
            URL url = new URL(source);
            logger.info("Processing report: " + source);

            ClassicEngineBoot.getInstance().start();
            ResourceManager manager = new ResourceManager();
            manager.registerDefaults();
            Resource resource = manager.createDirectly(url, MasterReport.class);
            MasterReport report = (MasterReport) resource.getResource();

            for (Entry<String, Object> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();
                report.getParameterValues().put(paramName, paramValue);
            }

            type.createReport(report, fileName);
        } catch (Exception e) {
            this.logger.warn("run() caught an exception during report preparation: ", (Throwable) e);
        }
    }

    public Integer getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public ReportType getType() {
        return type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param name name of the temporary report file
     */
    public void setFileName(String name) {
        fileName = name;
    }

    /**
     * @param parameters map of report parameters
     */
    public void setParameters(Map<String, Object> p) {
        parameters = p;
    }

    /**
     * @param t the type of report to generate (PDF, CSV, XLSX, ...)
     */
    public void setType(ReportType t) {
        this.type = t;
    }
}
