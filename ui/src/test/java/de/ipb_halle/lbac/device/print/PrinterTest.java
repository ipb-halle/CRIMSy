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

import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import static org.junit.Assert.assertEquals;

import de.ipb_halle.job.JobService;
import de.ipb_halle.job.JobStatus;
import de.ipb_halle.job.JobType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.job.PrintJob;
import de.ipb_halle.lbac.device.job.PrintJobService;
import de.ipb_halle.lbac.util.pref.Preference;
import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * This test class covers substantial code portions of the job, label and
 * printing classes.
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class PrinterTest extends TestBase {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private PrintBean printBean;

    @Inject
    private PrintJobService jobService;

    @Inject
    private LabelService labelService;

    @Inject
    private PrinterService printerService;

    @Inject
    private PreferenceService preferenceService;

    private User publicUser;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("PrinterTest.war");
        deployment = UserBeanDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }

    @BeforeEach
    public void init() {
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(this.globalAdmissionContext.getPublicAccount());
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        this.printBean.setUserBean(userBean);
    }

    private Printer createPrinter(String queue) {
        Printer p = new Printer(new PrinterEntity(),
                GlobalAdmissionContext.getPublicReadACL(),
                globalAdmissionContext.getAdminAccount());
        p.setQueue(queue);
        p.setConfig("prologue=49382c412c3030310a513230332c3032\\\n"
                + "         340a713530360a724e0a53310a443135\\\n"
                + "         0a5a540a4a460a4f440a4f43310a5232\\\n"
                + "         32302c300a6639300a4e0a\n"
                + "epilogue=50310a\n");
        p.setContact("testContact");
        p.setDriver(ZebraE2Driver.DRIVER_NAME);
        p.setModel("testModel");
        p.setName("testName");
        p.setPlace("testPlace");
        p.setStatus(PrinterStatus.READY);
        return p;
    }

    private Label createLabel(String name) {
        Label l = new Label(new LabelEntity());
        l.setName(name);
        l.setPrinterModel("testModel");
        l.setDescription("testDescription");
        l.setLabelType("printerTestMock");
        l.setConfig(" { \"form\" : {"
                + "   \"w\" : 50.0,"
                + "   \"h\" : 25.0,"
                + "   \"elements\" : [ {"
                + "   \"type\" : \"LABEL\","
                + "   \"x\" : 2.0,"
                + "   \"y\" : 20.0,"
                + "   \"data\" : \"CRIMSy Label TEST\""
                + "}, {"
                + "   \"type\": \"LABEL\","
                + "   \"x\" : 7.5,"
                + "   \"y\": 12.0,"
                + "   \"style\": \"BOLD\","
                + "   \"font\" : \"MONOSPACED\","
                + "   \"field\" :\"testText\""
                + "}, {"
                + "   \"type\" : \"PICTURE\","
                + "   \"x\" : 40.0,"
                + "   \"y\" : 15.0,"
                + "   \"raster\" : { \"w\":4, \"h\":4, \"data\": \"ffffffffff0000ffff0000ffffffffff\" }"
                + "}, {"
                + "   \"type\": \"INTERLEAVE25\","
                + "   \"x\" : 5.0,"
                + "   \"y\": 2.0,"
                + "   \"w\":28.0,"
                + "   \"h\":7.0,"
                + "   \"field\" :\"testBarcode\""
                + "} ]"
                + "} "
                + "}");
        return l;
    }

    /**
     */
    @Test
    public void testPrinting() {
        String queue = "testQueue";
        String labelName = "testLabel";

        Printer p = this.printerService.save(createPrinter(queue));
        Label l = this.labelService.save(createLabel(labelName));
        assertEquals("testPrinting() mismatch in queue name", queue, this.printBean.getPrinters().get(0).getValue());

        this.printBean.setPrinterQueue(queue);
        this.printBean.setLabelDataObject(new PrinterTestObject());
        SelectItem labelItem = this.printBean.getLabels().get(0);
        assertEquals("testPrinting() mismatch in label name", labelName, labelItem.getLabel());

        this.printBean.setLabelId((Integer) labelItem.getValue());
        this.printBean.actionPrintLabel();

        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(JobService.CONDITION_QUEUE, queue);
        cmap.put(JobService.CONDITION_STATUS, JobStatus.PENDING);
        cmap.put(JobService.CONDITION_JOBTYPE, JobType.PRINT);
        List<PrintJob> jobs = this.jobService.loadJobs(cmap);

        assertEquals("testPrinting() active job count", 1, jobs.size());
        assertEquals("testPrinting() job queue", queue, jobs.get(0).getQueue());

        this.labelService.delete(l);
        this.printerService.delete(p);
        this.jobService.removeJob(jobs.get(0));
    }

    @Test
    public void test_selectPrinterQueue() {
        Printer p1 = createPrinter("testPrinter_01");
        printerService.save(p1);
        Printer p2 = createPrinter("testPrinter_02");
        printerService.save(p2);
        Printer p3 = createPrinter("testPrinter_03");
        printerService.save(p3);

        Preference preference = new Preference(publicUser, "LABEL_PRINTER", "testPrinter_01");
        preferenceService.save(preference);

        this.printBean.getPrinters();
        Assert.assertEquals("testPrinter_01", this.printBean.getPrinterQueue());

        this.printBean.setPrinterQueue("testPrinter_02");
        Assert.assertEquals("testPrinter_02", this.printBean.getPrinterQueue());

        this.printBean.getPrinters();
        Assert.assertEquals("testPrinter_02", this.printBean.getPrinterQueue());

    }

}
