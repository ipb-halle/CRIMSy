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

import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.service.InfoObjectService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Path("/jobs")
@Stateless
public class JobWebService {

    @Inject
    private JobService jobService;

    @Inject
    private InfoObjectService infoObjectService;

    private Logger logger;

    
    /**
     * default constructor
     */
    public JobWebService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
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
    private String obtainSecret() {
        InfoObject obj = infoObjectService.loadByKey(SystemSettings.SETTING_AGENCY_SECRET);
        if (obj != null) {
            return obj.getValue();
        }
        return null;
    }

    /**
     * process a QUERY request
     */
    private NetJob processQuery(NetJob request) {
        
        // query for concrete Id
        if (request.getJobId() != null) {
            return jobService.loadById(request.getJobId()).createNetJob();
        }

        NetJob netjob = new NetJob();
        List<NetJob> joblist = new ArrayList<NetJob> ();
        Map<String, Object> cmap = new HashMap<String, Object> ();

        if (request.getJobType() != null) {
            cmap.put(JobService.CONDITION_JOBTYPE, request.getJobType());
        }

        if (request.getQueue() != null) {
            cmap.put(JobService.CONDITION_QUEUE, request.getQueue());
        }

        if (request.getStatus() != null) {
            cmap.put(JobService.CONDITION_STATUS, request.getStatus());
        }

        for(Job job : jobService.load(cmap)) {
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

        String secret = obtainSecret();
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
        Job job = jobService.loadById(request.getJobId());
        if (job != null) {
            job.setStatus(request.getStatus());
            job.setOutput(request.getOutput());
            job = jobService.save(job);
            return job.createNetJob();
        }
        return null;
    }

}
