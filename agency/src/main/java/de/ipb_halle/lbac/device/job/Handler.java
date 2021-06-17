/*
 * CRIMSy Agency
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
package de.ipb_halle.lbac.device.job;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class Handler {

    private final static int TIMEOUT = 3;
    private final static int SLEEP_MILLIS = 5000;

    private String secret;
    private String script;
    private List<String> queues;
    private List<JobType> jobtypes;
    private String url;

    private JobWebClient jobwebclient;
    private Logger logger;

    /** 
     * default constructor
     */
    public Handler() {
        this.jobwebclient = new JobWebClient();
        this.logger = LoggerFactory.getLogger(this.getClass().getName());
    }

    public Handler setJobTypes(List<JobType> jobtypes) {
        this.jobtypes = jobtypes;
        return this;
    }

    public Handler setQueues(List<String> queues) {
        this.queues = queues;
        return this;
    }

    public Handler setScript(String script) {
        this.script = script;
        return this;
    }

    public Handler setSecret(String secret) {
        this.secret = secret;
        return this;
    }

    public Handler setURL(String url) {
        this.url = url;
        return this;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch(InterruptedException e) {
            // ignore - we do not depend on sleeping exactly
        }
    }

    private void startQueues() throws IOException, InterruptedException {
        while (true) {
            ListIterator<String> iter = this.queues.listIterator();
            while (iter.hasNext()) {
                NetJob netjob = new NetJob()
                    .setQueue(iter.next())
                    .setRequestType(RequestType.QUERY);
                // this.logger.info("startQueues(): queue={}",  netjob.getQueue());
                start(netjob);
            }
            sleep();
        }
    }

    private void startTypes() throws IOException, InterruptedException {
        while (true) {
            ListIterator<JobType> iter = this.jobtypes.listIterator();
            while (iter.hasNext()) {
                NetJob netjob = new NetJob()
                    .setJobType(iter.next())
                    .setRequestType(RequestType.QUERY);
                start(netjob);
            }
            sleep();
        }
    }

    private void start(NetJob netjob) throws IOException, InterruptedException {
        String[] cmd = new String[4];
        cmd[0] = this.script;

        netjob.setToken(TokenGenerator.getToken(this.secret));
        // this.logger.info("Token: {}", netjob.getToken());
        netjob.setStatus(JobStatus.PENDING);
        NetJob result = jobwebclient.processRequest(netjob, this.url);
        if (result != null) {
            for (NetJob job : result.getJobList()) {
                cmd[1] = job.getJobType().toString();
                cmd[2] = job.getQueue();
                cmd[3] = job.getJobId().toString();
                Process proc = Runtime.getRuntime().exec(cmd);
                // this.logger.info("input size: {}", job.getInput().length);
                proc.getOutputStream().write(job.getInput());
                proc.getOutputStream().close();
                boolean procStatus = proc.waitFor(TIMEOUT, TimeUnit.SECONDS);
                
                job.setStatus(JobStatus.BUSY);
                job.setRequestType(RequestType.UPDATE);
                job.setToken(TokenGenerator.getToken(this.secret));
                jobwebclient.processRequest(job, this.url);
            }
        }
    }

    public void start() throws IOException, InterruptedException {
        if (queues.size() > 0) {
            this.logger.info("start() --> Queues");
            startQueues();
        }

        if (jobtypes.size() > 0) {
            this.logger.info("start() --> JobTypes");
            startTypes();
        }

        this.logger.info("start() --> ALL");
        NetJob netjob = new NetJob()
            .setRequestType(RequestType.QUERY);
        while (true) {
            start(netjob);
            sleep();
        }
    }
}
