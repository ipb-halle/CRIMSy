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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Bean for managing (and adding, removing) label printers 
 * and managing their jobs
 * 
 * @author fbroda
 */
@SessionScoped
@Named
public class PrintAdminBean implements Serializable {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private JobService jobService;

    @Inject
    private PrinterService printerService;

    @Inject
    private UserBean userBean;

    private String  driverType;
    private Printer printer; 


    @PostConstruct
    private void printAdminBeanInit() {
        this.printer = new Printer(new PrinterEntity(),
                GlobalAdmissionContext.getPublicReadACL(),
                globalAdmissionContext.getAdminAccount());
    }

    /**
     * print a testpage for the selected Printer
     */
    public void actionPrintTestPage() {
        PrintDriver driver = PrintDriverFactory.buildPrintDriver(this.printer);
        driver.printBarcode(BarcodeType.INTERLEAVE25, "1234567895");
        driver.printLine("Label printing test.");
        submitJob(driver);
    }

    /** ToDo:   */
    public void actionAddPrinter() {
        this.printer = new Printer(new PrinterEntity(),
                GlobalAdmissionContext.getPublicReadACL(),
                globalAdmissionContext.getAdminAccount());
    }

    public void actionSelectPrinter(String name) {
        this.printer = printerService.loadById(name);
        // xxxxx error handling!!!
    }

    public void actionSave() {
        this.printer = printerService.save(this.printer);
        // xxxxx error handling!!!
    }


    public String getDriverType() {
        return this.driverType;
    }

    public Printer getPrinter() {
        return this.printer;
    }

    /**
     * get a list of available Print Drivers
     */
    public List<String> getDrivers() {
        return PrintDriverFactory.getDrivers();
    }

    /**
     * get a list of available Printers
     */
    public List<Printer> getPrinters() {
        return printerService.load();
    }

    /**
     * set the currently selected driver type 
     */
    public void setDriverType(String driverType) {
        this.driverType = driverType;
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
