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
package de.ipb_halle.lbac.webservice;

import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.CloudNodeMessage;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Path("/nodes")
@Stateless
public class CloudNodeWebService {

    @Inject
    private CloudService cloudService;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private NodeService nodeService;

    private Logger logger;

    /**
     * default constructor
     */
    public CloudNodeWebService() {
    }

    @PostConstruct
    public void NodeServiceInit() {
        logger = LogManager.getLogger(this.getClass().getName());
        if (cloudService == null) {
            logger.error("Injection failed for cloudService.");
        }
        if (cloudNodeService == null) {
            logger.error("Injection failed for cloudNodeService.");
        }
        if (nodeService == null) {
            logger.error("Injection failed for nodeService.");
        }
    }

    /**
     * save a remote node in the database and return a list of all nodes known
     * locally.
     *
     * ToDo: CAVEAT / xxxxx / authorization: There is no means of checking
     * the authenticity (and authorization) of the request but the mutual 
     * certificate based authentication by the proxy node. Malicious nodes 
     * could attempt to overwrite elements (e.g. the public key) in the list 
     * of nodes and cloudnodes. This could result denial of service.
     *
     * @param request the current node object
     * @return a serialized CloudNodeMessage object containing a list of nodes
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getCloudNodeMessage(CloudNodeMessage request) {

        CloudNodeMessage result = processRequest(request);
        if (result != null) {
            return Response.ok(result).build();
        } 
        return Response.serverError().build();
    }


    /**
     * encapsulates the request processing 
     * @param request the CloudNodeMessage request object
     * @return a CloudNodeMessage response object or null
     */
    private CloudNodeMessage processRequest(CloudNodeMessage request) {

        if ((request == null) || (request.getCloudName() == null)) {
            this.logger.warn("getCloudNodeMessage() recceived empty request");
            return null;
        }

        String cloudName = request.getCloudName();
        Cloud cloud = this.cloudService.loadByName(cloudName);

        if (cloud == null) {
            this.logger.warn(String.format("getCloudNodeMessage() received request for unknown cloud %", cloudName));
            return null;
        }

        Node node = request.getOrigin();
        if (node == null) {
            this.logger.warn("getCloudNodeMessage() received request with empty originating node");
            return null;
        }

        if (node.equals(this.nodeService.getLocalNode())) {
            this.logger.warn("getCloudNodeMessage() received request for local node Object");
            return null;
        }

        node.setLocal(Boolean.FALSE);
        node = this.nodeService.save(node);

        CloudNode cn = this.cloudNodeService.loadCloudNode(cloud, node);
        cn = (cn == null) ? new CloudNode(cloud, node) : cn;
        cn.setRank(1);
        cn.setPublicKey(request.getPublicKey());
        this.cloudNodeService.save(cn);

        CloudNodeMessage msg = new CloudNodeMessage(this.cloudNodeService.load(cloud, null));
        return msg;
    }
}
