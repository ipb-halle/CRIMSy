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
package de.ipb_halle.lbac.device.job;

import static de.ipb_halle.lbac.device.job.JobStatus.BUSY;
import static de.ipb_halle.lbac.device.job.JobStatus.PENDING;
import static de.ipb_halle.lbac.device.job.JobType.COMPUTE;
import static de.ipb_halle.lbac.device.job.JobType.PRINT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class JobServiceTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private JobService jobService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("JobServiceTest.war").addClass(JobService.class);
    }

    @Test
    public void test_saveJob_and_loadAllJobs() {
        Job originalJob1 = new Job().setInput("abc".getBytes()).setJobDate(new Date(100)).setJobType(PRINT)
                .setOutput("def".getBytes()).setOwner(publicUser).setQueue("queue1").setStatus(PENDING);
        Job originalJob2 = new Job().setInput("ghi".getBytes()).setJobDate(new Date(200)).setJobType(COMPUTE)
                .setOutput("jkl".getBytes()).setOwner(adminUser).setQueue("queue2").setStatus(BUSY);

        Job savedJob1 = jobService.saveJob(originalJob1);
        Job savedJob2 = jobService.saveJob(originalJob2);

        List<Job> loadedJobs = jobService.loadAllJobs();
        assertThat(loadedJobs, hasSize(2));
        Job loadedJob1 = loadedJobs.get(0);
        Job loadedJob2 = loadedJobs.get(1);

        assertJobsEqual(originalJob1, savedJob1);
        assertJobsEqual(originalJob2, savedJob2);
        assertJobsEqual(originalJob1, loadedJob1);
        assertJobsEqual(originalJob2, loadedJob2);
        assertEquals(savedJob1.getJobId(), loadedJob1.getJobId());
        assertEquals(savedJob2.getJobId(), loadedJob2.getJobId());
    }

    @Test
    public void test_loadJobs_withConditions() {
        Job job1 = new Job().setJobType(PRINT).setStatus(PENDING).setOwner(adminUser).setQueue("queue1");
        Job job2 = new Job().setJobType(PRINT).setStatus(BUSY).setOwner(adminUser).setQueue("queue1");
        job1 = jobService.saveJob(job1);
        job2 = jobService.saveJob(job2);

        Map<String, Object> conditions = new HashMap<>();
        conditions.put(JobService.CONDITION_JOBTYPE, PRINT);
        conditions.put(JobService.CONDITION_QUEUE, "queue1");
        conditions.put(JobService.CONDITION_STATUS, BUSY);
        conditions.put(JobService.CONDITION_OWNERID, adminUser.getId());

        List<Job> loadedJobs = jobService.loadJobs(conditions);

        assertThat(loadedJobs, hasSize(1));
        assertEquals(job2.getJobId(), loadedJobs.get(0).getJobId());
        assertJobsEqual(job2, loadedJobs.get(0));
    }

    @Test
    public void test_loadJobsOlderThan() {
        Instant now = Instant.now();
        Date oneDayAgo = Date.from(now.minus(Duration.ofDays(1)));
        Date twoDaysAgoPlusOneSecond = Date.from(now.minus(Duration.ofDays(2)).plus(Duration.ofSeconds(1)));
        Date twoDaysAgo = Date.from(now.minus(Duration.ofDays(2)));
        Date threeDaysAgo = Date.from(now.minus(Duration.ofDays(3)));

        Job job = new Job().setJobDate(twoDaysAgo).setJobType(PRINT).setStatus(PENDING).setOwner(adminUser)
                .setQueue("");
        job = jobService.saveJob(job);

        assertThat(jobService.loadJobsOlderThan(oneDayAgo, new HashMap<>()), hasSize(1));
        assertThat(jobService.loadJobsOlderThan(twoDaysAgoPlusOneSecond, new HashMap<>()), hasSize(1));
        assertThat(jobService.loadJobsOlderThan(twoDaysAgo, new HashMap<>()), hasSize(0));
        assertThat(jobService.loadJobsOlderThan(threeDaysAgo, new HashMap<>()), hasSize(0));
    }

    @Test
    public void test_loadJobById() {
        Job job = new Job().setJobType(PRINT).setStatus(PENDING).setOwner(adminUser).setQueue("queue1");
        job = jobService.saveJob(job);

        Job loadedJob = jobService.loadJobById(job.getJobId());

        assertEquals(job.getJobId(), loadedJob.getJobId());
        assertJobsEqual(job, loadedJob);

        assertNull(jobService.loadJobById(-1));
    }

    @Test
    public void test_removeJob_withJob() {
        Job job = new Job().setJobType(PRINT).setStatus(PENDING).setOwner(adminUser).setQueue("queue1");
        job = jobService.saveJob(job);

        jobService.removeJob(job);

        assertThat(jobService.loadAllJobs(), hasSize(0));
    }

    @Test
    public void test_removeJob_withJobId() {
        Job job = new Job().setJobType(PRINT).setStatus(PENDING).setOwner(adminUser).setQueue("queue1");
        job = jobService.saveJob(job);

        jobService.removeJob((Integer) null);
        assertThat(jobService.loadAllJobs(), hasSize(1));

        jobService.removeJob(job.getJobId());
        assertThat(jobService.loadAllJobs(), hasSize(0));
    }

    private void assertJobsEqual(Job job1, Job job2) {
        assertArrayEquals(job1.getInput(), job2.getInput());
        assertEquals(job1.getJobdate(), job2.getJobdate());
        assertEquals(job1.getJobType(), job2.getJobType());
        assertArrayEquals(job1.getOutput(), job2.getOutput());
        assertTrue(job1.getOwner().isEqualTo(job2.getOwner()));
        assertEquals(job1.getQueue(), job2.getQueue());
        assertEquals(job1.getStatus(), job2.getStatus());
    }
}