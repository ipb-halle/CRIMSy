/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.device.print;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Bean for label printing. This bean provides a list of 
 * available printers for the current user and easy access 
 * to the print driver and job submission for other 
 * (session scoped) beans.
 * 
 * @author fbroda
 */
@SessionScoped
@Named
public class PrintBean implements Serializable {

    @Inject
    private JobService jobService;

    @Inject
    private PrinterService printerService;

    @Inject
    private UserBean userBean;

    private String printerName;

    public String getPrinterName() {
        return this.printerName;
    }

    /**
     * obtain the driver for the selected printer
     */
    public PrintDriver getDriver() {
        Printer printer = printerService.loadById(this.printerName);
        return PrintDriverFactory.buildPrintDriver(printer);
    }

    /**
     */
    public List<Printer> getPrinters() {
        /*
         * ToDo: xxxxx limit accessible printers
         */
        return printerService.load();
    }

    /**
     * set the name of the currently selected printer
     */
    public void setPrinterName(String name) {
        this.printerName = name;
    }

    /**
     * submit a job for printing
     */
    public void submitJob(PrintDriver driver) {
        Job job = driver.createJob();
        job.setOwner(userBean.getCurrentAccount());
        this.jobService.save(job); 
    }
}
