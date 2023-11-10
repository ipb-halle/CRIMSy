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
package de.ipb_halle.lbac.reporting;

import static de.ipb_halle.job.JobStatus.BUSY;
import static de.ipb_halle.job.JobStatus.COMPLETED;
import static de.ipb_halle.job.JobType.REPORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import de.ipb_halle.job.JobService;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.job.JobStatus;
import de.ipb_halle.lbac.reporting.ReportJobService;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ReportingJobsBeanTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private ReportingJobsBean bean;

    @Inject
    private UserBeanMock userBeanMock;

    @Inject
    private SendFileBeanMock sendFileBeanMock;;

    @Inject
    private ReportJobService jobService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ReportingJobsBeanTest.war").addClasses(ReportingJobsBean.class,
                ReportJobService.class, JobService.class, SendFileBeanMock.class);
        return UserBeanDeployment.add(deployment);
    }

    @BeforeEach
    private void before() {
        userBeanMock.setCurrentAccount(adminUser);
        sendFileBeanMock.reset();
    }

    @Test
    public void test_actionReloadTable() {
        bean.actionReloadTable();

        assertThat(bean.getReportingJobs(), is(empty()));

        ReportJob job1 = new ReportJob().setJobType(REPORT).setStatus(BUSY).setOwner(adminUser).setQueue("");
        ReportJob job2 = new ReportJob().setJobType(REPORT).setStatus(BUSY).setOwner(publicUser).setQueue("");
        job1 = jobService.saveJob(job1);
        job2 = jobService.saveJob(job2);

        bean.actionReloadTable();

        assertThat(bean.getReportingJobs(), hasSize(1));
        assertEquals(job1.getJobId(), bean.getReportingJobs().get(0).getJobId());
    }

    @Test
    public void test_actionDownloadReport(@TempDir File tempDir) throws IOException {
        File tempFile = File.createTempFile("ReportingJobsBeanTest", "test", tempDir);
        tempFile.deleteOnExit();
        FileUtils.write(tempFile, "abc");
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        bean.actionDownloadReport(job);

        assertEquals("abc", new String(sendFileBeanMock.getContent()));
        assertEquals(tempFile.getName(), sendFileBeanMock.getFilename());
    }
    
    @Test
    public void test_actionDeleteReport(@TempDir File tempDir) throws IOException {
        File tempFile = File.createTempFile("ReportingJobsBeanTest", "test2", tempDir);
        tempFile.deleteOnExit();
        ReportJob job = new ReportJob().setJobType(REPORT).setStatus(COMPLETED).setOwner(adminUser).setQueue("")
                .setOutput(tempFile.getAbsolutePath().getBytes());
        job = jobService.saveJob(job);

        bean.actionDeleteReport(job);

        assertThat(bean.getReportingJobs(), is(empty()));
        assertFalse(tempFile.exists());
    }

    @Test
    public void test_getJobStatusI18n() {
        ReportJob job = new ReportJob();

        for (JobStatus status : JobStatus.values()) {
            job.setStatus(status);
            assertEquals("jobStatus_" + status.toString(), bean.getJobStatusI18n(job));
        }
    }
}
