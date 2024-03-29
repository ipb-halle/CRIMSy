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
package de.ipb_halle.reporting;

import de.ipb_halle.reporting.ReportType;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;


/**
 * Builder for different report types (PDF, XLSX, CSV).
 * 
 * @author fbroda
 */
public class ReportBuilder {

    /**
     * Create a report and write it to the specified file
     * 
     * @param report
     * @param filename
     * @param report type
     * @throws Exception
     */
    public static void createReport(MasterReport report, String filename, ReportType reportType) throws Exception {
        switch(reportType) {
            case PDF:
                PdfReportUtil.createPDF(report, filename);
                break;
            case XLSX:
                ExcelReportUtil.createXLSX(report, filename);
                break;
            case CSV:
                CSVReportUtil.createCSV(report, filename, null);
                break;
            default:
                throw new IllegalArgumentException("Invalid report type");
        }
    }
}
