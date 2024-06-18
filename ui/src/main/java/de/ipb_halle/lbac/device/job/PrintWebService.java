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
package de.ipb_halle.lbac.device.job;

import de.ipb_halle.job.NetJob;
import de.ipb_halle.job.JobService;
import de.ipb_halle.job.JobType;
import de.ipb_halle.job.TokenGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/print")
@Stateless
public class PrintWebService {

    @Inject
    private PrintJobService jobService;

    private Logger logger;

    
    /**
     * default constructor
     */
    public PrintWebService() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     *
     *
     * @param request the current node object
     * @return a serialized CloudNodeMessage object containing a list of nodes
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getJobs(NetJob request) {

        NetJob result = processRequest(request);
        if (result != null) {
            return Response.ok(result).build();
        } 
        return Response.serverError().build();
    }

    /**
     * obtain the secret from system settings
     */
    private String obtainJobSecret() {
        return jobService.obtainJobSecret();
    }

    /**
     * process a QUERY request
     */
    private NetJob processQuery(NetJob request) {
        
        // query for concrete Id
        if (request.getJobId() != null) {
            return jobService.loadJobById(request.getJobId()).createNetJob();
        }

        NetJob netjob = new NetJob();
        List<NetJob> joblist = new ArrayList<NetJob> ();
        Map<String, Object> cmap = new HashMap<String, Object> ();

        if (request.getJobType() != JobType.PRINT) {
            throw new IllegalArgumentException("Query for invalid job type");
        }
        cmap.put(JobService.CONDITION_JOBTYPE, JobType.PRINT);

        if (request.getQueue() != null) {
            cmap.put(JobService.CONDITION_QUEUE, request.getQueue());
        }

        if (request.getStatus() != null) {
            cmap.put(JobService.CONDITION_STATUS, request.getStatus());
        }

        for(PrintJob job : jobService.loadJobs(cmap)) {
            joblist.add(job.createNetJob());
        }
        return netjob.setJobList(joblist);
    }

    /**
     * checks request validity and delegates for 
     * further processing
     * @param request the NetJob request object
     * @return a NetJob response object or null
     */
    private NetJob processRequest(NetJob request) {

        if (request == null)  {
            this.logger.warn("getCloudNodeMessage() recceived empty request");
            return null;
        }

        String secret = obtainJobSecret();
        if ((secret == null) || (secret.length() < 8)) {
            this.logger.info("No secret configured or secret to short");
            return null;
        }

        if (! TokenGenerator.checkToken(request.getToken(), secret)) {
            this.logger.info("Invalid access token: {}", request.getToken());
            return null;
        }

        try {
            switch(request.getRequestType()) {
                case QUERY :
                    return processQuery(request)
                        .setToken(TokenGenerator.getToken(secret));

                case UPDATE :
                    return processUpdate(request)
                        .setToken(TokenGenerator.getToken(secret));
            }
        } catch(Exception e) {
            this.logger.warn("processRequest caught an exception: ", (Throwable) e);
        }
        return null;
    }

    /**
     * process an update request
     */
    private NetJob processUpdate(NetJob request) {
        PrintJob job = jobService.loadJobById(request.getJobId());
        if (job != null) {
            job.setStatus(request.getStatus());
            job.setOutput(request.getOutput());
            job = jobService.saveJob(job);
            return job.createNetJob();
        }
        return null;
    }

}
