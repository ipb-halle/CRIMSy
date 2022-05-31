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
package de.ipb_halle.lbac.reporting.report;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.csv.CSVReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;

/**
 * Report types
 *
 * @author fbroda
 */
public enum ReportType {
    PDF(".pdf") {
        @Override
        public void createReport(MasterReport report, String filename) throws Exception {
            PdfReportUtil.createPDF(report, filename);
        }
    },
    XLSX(".xlsx") {
        @Override
        public void createReport(MasterReport report, String filename) throws Exception {
            ExcelReportUtil.createXLSX(report, filename);
        }
    },
    CSV(".csv") {
        @Override
        public void createReport(MasterReport report, String filename) throws Exception {
            CSVReportUtil.createCSV(report, filename, null);
        }
    };

    private final String fileExtension;

    private ReportType(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Create a report and write it to the specified file
     * 
     * @param report
     * @param filename
     * @throws Exception
     */
    public abstract void createReport(MasterReport report, String filename) throws Exception;
}
