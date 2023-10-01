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

import static de.ipb_halle.lbac.device.job.JobStatus.BUSY;
import static de.ipb_halle.lbac.reporting.report.ReportMgr.DEFAULT_NAME;
import static de.ipb_halle.lbac.reporting.report.ReportType.PDF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.test.ManagedExecutorServiceMock;
import de.ipb_halle.lbac.reporting.job.ReportJobPojo;
import de.ipb_halle.lbac.reporting.job.ReportJobService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportMgrTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportMgr reportMgr;

    @Inject
    private ReportJobService reportJobService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ReportMgrTest.war").addClasses(ReportMgr.class, ReportService.class,
                ReportJobService.class, JobService.class);
    }

    private ManagedExecutorServiceMock managedExecutorService;

    @BeforeEach
    private void before() {
        managedExecutorService = new ManagedExecutorServiceMock(2);
        reportJobService.setManagedExecutorService(managedExecutorService);
    }

    @Test
    public void test_getAvailableReports() {
        insertReport("context1", "no{Valid:Json}", "source1a");
        insertReport("context1", "parsableJson", "source1b");
        insertReport("context1", "{\"en\":\"english name\",\"de\":\"deutscher Name\"}", "source1c");
        insertReport("context1", "{\"de\":\"deutscher Name\"}", "source1d");
        insertReport("context1", "{\"en\":\"\"}", "source1e");
        insertReport("context2", "name2", "source2");

        assertThat(reportMgr.getAvailableReports(null), is(empty()));
        assertThat(reportMgr.getAvailableReports("context"), is(empty()));

        List<Report> reports = reportMgr.getAvailableReports("context1");
        assertThat(reports, hasSize(5));
        reports.sort(Comparator.comparing(Report::getSource));

        // check correct identity of Report objects
        assertEquals("source1a", reports.get(0).getSource());
        assertEquals("source1b", reports.get(1).getSource());
        assertEquals("source1c", reports.get(2).getSource());
        assertEquals("source1d", reports.get(3).getSource());
        assertEquals("source1e", reports.get(4).getSource());

        // check correct localization via JSON in name field
        assertEquals(DEFAULT_NAME, reports.get(0).getName());
        assertEquals(DEFAULT_NAME, reports.get(1).getName());
        assertEquals("english name", reports.get(2).getName());
        assertEquals(DEFAULT_NAME, reports.get(3).getName());
        assertEquals(DEFAULT_NAME, reports.get(4).getName());
    }

    @Test
    public void test_submitReport() {
        ReportEntity entity = new ReportEntity();
        entity.setId(42).setContext("abc").setName("def").setSource("ghi");
        Report report = new Report(entity);
        Map<String, Object> params = new HashMap<>();
        params.put("param", "value");

        reportMgr.submitReport(report, params, PDF, adminUser);

        // reporting job arrived in the database
        List<Job> jobs = reportJobService.loadJobsForUser(adminUser);
        assertThat(jobs, hasSize(1));

        Job job = jobs.get(0);
        ReportJobPojo pojo = deserializeToReportJobPojo(job.getInput());
        assertEquals(BUSY, job.getStatus());
        assertEquals("ghi", pojo.getReportURI());
        assertEquals(PDF, pojo.getType());
        assertEquals(params, pojo.getParameters());

        // reporting job was submitted to the managedExecutorService
        assertThat(managedExecutorService.getSubmittedTasks(), hasSize(1));
    }

    private ReportJobPojo deserializeToReportJobPojo(byte[] bytes) {
        Object o = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            o = ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return (ReportJobPojo) o;
    }
}
