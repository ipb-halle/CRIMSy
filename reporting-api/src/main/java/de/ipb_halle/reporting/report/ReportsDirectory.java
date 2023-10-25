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
package de.ipb_halle.reporting.report;

import java.io.Serializable;
import javax.ejb.Singleton;
import javax.ejb.Startup;


/**
 * 
 * @author fbroda
 */
@Singleton(name="reportsDirectory")
@Startup
public class ReportsDirectory implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String REPORTS_DIRECTORY = "/data/tmp/reports/";

    public String getReportsDirectory() {
        return REPORTS_DIRECTORY;
    }
}
