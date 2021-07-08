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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.globals.NavigationConstants;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.lbac.navigation.Navigator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Bean for managing (and adding, removing) label printers 
 * and managing their jobs
 * 
 * @author fbroda
 */
@SessionScoped
@Named
public class PrintAdminBean implements ACObjectBean, Serializable {

    private final static long serialVersionUID = 1L;

    private final static String PRINTER_LIST = "print/printerList";
    private final static String PRINTER_DETAILS = "print/printerDetails";
    private final static String PRINTER_JOBS = "print/printerJobs";
    private final static String LABEL_DETAILS = "print/labelDetails";

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private JobService jobService;

    @Inject
    private LabelService labelService;

    @Inject
    private PrinterService printerService;

    @Inject
    private MemberService memberService;

    @Inject
    private Navigator navigator;

    @Inject
    private UserBean userBean;

    private ACObjectController acObjectController;
    private String currentPage;
    private String driverType;
    private Printer printer; 
    private Label label;
    private Logger logger;

    private List<Label> labels;
    private List<Printer> printers;

    /*
     * default constructor
     */
    public PrintAdminBean() {
        this.currentPage = PRINTER_LIST;
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    private void printAdminBeanInit() {
/*
        this.printer = new Printer(new PrinterEntity(),
                GlobalAdmissionContext.getPublicReadACL(),
                globalAdmissionContext.getAdminAccount());
        this.label = new Label(new LabelEntity());
*/
        this.printers = printerService.load();
        this.labels = labelService.load(new HashMap<String, Object> ());
    }


    public void actionAddLabel() {
        this.label = new Label(new LabelEntity());
        this.currentPage = LABEL_DETAILS;
        this.navigator.navigate(LABEL_DETAILS);
    }

    public void actionAddPrinter() {
        this.printer = new Printer(new PrinterEntity(),
                GlobalAdmissionContext.getPublicReadACL(),
                globalAdmissionContext.getAdminAccount());
        this.currentPage = PRINTER_DETAILS;
        this.navigator.navigate(PRINTER_DETAILS);
    }

    public void actionClose() {
        this.printer = null;
        currentPage = PRINTER_LIST;
        this.navigator.navigate(PRINTER_LIST);
    }

    public void actionDelete(Printer printer) {
        printerService.delete(printer);
        printAdminBeanInit();
    }

    public void actionDelete(Label label) {
        labelService.delete(label);
        printAdminBeanInit();
    }

    /**
     * print a testpage for the selected Printer
     */
    public void actionPrintTestPage() {
        PrintDriver driver = PrintDriverFactory.buildPrintDriver(this.printer);
        driver.printBarcode(2.0, 2.0, 38.0, 6.0, BarcodeType.INTERLEAVE25, "1234567895");
        driver.printLine(3.0, 16.0, "Label printing test.");
        submitJob(driver);
    }

    public void actionSaveLabel() {
        this.label = labelService.save(this.label);
        // ToDo: xxxxx error handling!!!
        printAdminBeanInit();
    }

    public void actionSavePrinter() {
        this.printer = printerService.save(this.printer);
        // ToDo: xxxxx error handling!!!
        printAdminBeanInit();
    }

    public void actionSelectLabel(Label label) {
        this.label = label;
        currentPage = LABEL_DETAILS;
        this.navigator.navigate(LABEL_DETAILS);
        // ToDo: xxxxx error handling!!!
    }

    public void actionSelectPrinter(Printer printer) {
        this.printer = printer; 
        currentPage = PRINTER_DETAILS;
        this.navigator.navigate(PRINTER_DETAILS);
        // ToDo: xxxxx error handling!!!
    }

    public void actionShowJobs(String queue) {
        currentPage = PRINTER_JOBS;
        this.navigator.navigate(PRINTER_JOBS);
    }

    public void actionStartAclChange(ACObject aco) { 
        try {
            this.printer = (Printer) aco;

            Map<String, Object> cmap = new HashMap<> ();
            cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, new AdmissionSubSystemType[] {
                    AdmissionSubSystemType.BUILTIN, 
                    AdmissionSubSystemType.LOCAL, 
                    AdmissionSubSystemType.LDAP });

            List<Group> ml = memberService.loadGroups(cmap);
            this.acObjectController = new ACObjectController(
                    this.printer,
                    ml,
                    this, 
                    this.printer.getName());
        } catch(Exception e) {
            this.logger.warn("actionStartAclChange() caught an Exception", (Throwable) e);
        }
    }

    public void applyAclChanges() { 
        this.printer.setACList(printer.getACList());
        this.printer = printerService.save(this.printer);
    }

    public void cancelAclChanges() { 
        // do nothing 
    }

    public ACObjectController getAcObjectController() { 
        return this.acObjectController;
    }

    public String getDriverType() {
        return this.driverType;
    }

    public  Label getLabel() {
        return this.label;
    }

    public String getPage() {
        return this.currentPage; 
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
     * obtain the list of labels
     */
    public List<Label> getLabels() {
        // return labelService.load(new HashMap<String, Object> ());
        return this.labels;
    }

    /**
     * get a list of available Printers
     */
    public List<Printer> getPrinters() {
        // return printerService.load();
        return this.printers;
    }

    /**
     * get an array of available printer states
     */
    public PrinterStatus[] getPrinterStates() {
        return PrinterStatus.values();
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
