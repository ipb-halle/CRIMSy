/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;

/**
 * NodeWebClient This class is used to query the complete list of nodes from the
 * master node. The local database must therefore contain a record for the
 * master node. (In case of need, any node can temporarily assume the master
 * role and provide a new node with updated data.)
 */
@Startup
@Singleton
@DependsOn({"Updater", "CloudService", "CloudNodeService", "NodeService"})
public class CloudNodeWebClient implements IUpdateWebClient {
    
    private final static String REST_PATH = getRestApiDefaultPath(CloudNodeWebService.class);
    
    @Inject
    private NodeService nodeService;
    
    @Inject
    private CloudService cloudService;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private Updater updater;
    
    private Logger logger;
    
    public CloudNodeWebClient() {
        logger = Logger.getLogger(this.getClass().getName());
    }
    
    @PostConstruct
    public void CloudNodeWebClientInit() {
        if (this.cloudService == null) {            
            this.logger.error("Injection of CloudService failed!");
        }
        if (this.cloudNodeService == null) {
            this.logger.error("Injection of CloudNodeService failed!");
        }
        if (this.nodeService == null) {
            this.logger.error("Injection of NodeService failed!");
        }
        if (this.updater == null) {
            this.logger.error("Injection of Updater failed!");
        } else {
            this.updater.register(this);
            
            this.update();
        }
    }

    /**
     * query the list of nodes from the master node
     *
     * @param masterNode the master node to query
     * @param localNode the Node object to send to the master to update the
     * database on the master node
     * @return list of nodes. The Nodes in the list are detached entities which
     * need to be merged.
     */
    @SuppressWarnings("unchecked")
    private void query(Node masterNode, Node localNode, Cloud cloud) throws Exception {
        // the CloudNode object does not need to be persisted
        CloudNode localCloudNode = this.cloudNodeService.loadCloudNode(cloud,  localNode);
        CloudNode masterCloudNode = this.cloudNodeService.loadCloudNode(cloud, masterNode);
        WebClient wc = SecureWebClientBuilder.createWebClient(masterCloudNode, REST_PATH);
        
        CloudNodeMessage msg = new CloudNodeMessage(cloud.getName(), localNode, localCloudNode.getPublicKey());
/*
        JAXBContext jc = JAXBContext.newInstance( new Class[] { Cloud.class, Node.class, CloudNode.class, CloudNodeMessage.class }, null); 
        Marshaller m = jc.createMarshaller();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        m.marshal(msg, bao);
        this.logger.info(String.format("============ XML Marshal =================\n%s\n=======================", bao.toString()));
*/
        wc.accept(MediaType.APPLICATION_XML);
        wc.type(MediaType.APPLICATION_XML);

        logger.info("Start CloudNodeWebClient REST call " + masterNode.getBaseUrl() + REST_PATH);
        CloudNodeMessage result = wc.post(msg, CloudNodeMessage.class);

        if ((result != null) && (result.getCloudNodeList() != null)) {
            save(result, localNode, cloud);
        }
    }

    /**
     * persist the content of a CloudNodeMessage reply object
     * @param msg the CloudNodeMessage reply object from the master node
     * @param cloud the cloud object
     */
    private void save(CloudNodeMessage msg, Node localNode, Cloud cloud) throws Exception {
        ListIterator<CloudNode> li = msg.getCloudNodeList().listIterator();
        while (li.hasNext()) {
            CloudNode cn = li.next();
            Node n = cn.getNode();
            /* 
             * do not update the local node 
             * do not allow another node with local == true
             */
            if (! localNode.equals(n)) {
                int rank = cn.getRank();
                String pubkey = cn.getPublicKey();
                n.setLocal(Boolean.FALSE);
                n = this.nodeService.save(cn.getNode());
                cn = this.cloudNodeService.loadCloudNode(cloud, n);
                cn = (cn == null) ? new CloudNode(cloud, n) : cn;
                cn.setRank(rank);
                cn.setPublicKey(pubkey);
                cn.recover();
                this.cloudNodeService.save(cn);
            }
        }
    }

    /**
     * query the list of clouds and for each cloud request an update 
     * from the master node of the respective cloud.
     */
    @Override
    public void update() {
        try {
            Node localNode = nodeService.getLocalNode();

            List<Cloud> cloudList = cloudService.load();
            ListIterator<Cloud> li = cloudList.listIterator();
            while (li.hasNext()) {
                Cloud cloud = li.next();
                Node masterNode = cloudNodeService.loadMasterNode(cloud);
/*
                this.logger.info("update() processing cloud " + cloud.getName());
                this.logger.info("update()   master node " + masterNode.getId().toString());
                this.logger.info("update()    local node " + localNode.getId().toString());
*/
                // skip update for master node!
                if (! masterNode.equals(localNode)) {
                    
                    query(masterNode, localNode, cloud);
                }
            }
        } catch (Exception e) {
                this.logger.warn("update() caught an exception:", e);
        }
    }
}
