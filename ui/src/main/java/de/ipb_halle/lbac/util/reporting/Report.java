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

import java.io.PipedOutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Create Pentaho report 
 *
 * @author fbroda
 */
public class Report implements Runnable {

    private Logger logger;
    private PipedOutputStream outputStream;
    private String name;
    private Map<String, Object> parameters;

    public Report() {
        logger = LogManager.getLogger(getClass().getName());
    }

    public PipedOutputStream getPipedOutputStream() {
        outputStream = new PipedOutputStream();
        return outputStream;
    }

    /**
     * prepare a report and deliver it to the user
     */
    public void run() {

        URL url = this.getClass().getResource(name);
        logger.info("Processing report: "  + url.toString());

        try {
            ClassicEngineBoot.getInstance().start();
            ResourceManager manager = new ResourceManager();
            manager.registerDefaults();
            Resource resource = manager.createDirectly(url, MasterReport.class);
            MasterReport report = (MasterReport) resource.getResource();

            Iterator<String> iter = parameters.keySet().iterator();
            while(iter.hasNext()) {
                    String paramName = iter.next();
                    Object o = parameters.get(paramName);
//                  logger.info("Setting parameter: " + paramName + " --> " + ((o == null) ? "null" : o.toString()));
                    report.getParameterValues().put(paramName, parameters.get(paramName));
            }

            PdfReportUtil.createPDF(report, outputStream);

        } catch(Exception e) {
            this.logger.warn("run() caught an exception during report preparation: ", (Throwable) e);
        }
    }

    /**
     * @param name of the report template
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * @param parameters map of report parameters
     */
    public void setParameters(Map<String, Object> p) {
        parameters = p;
    }
}
