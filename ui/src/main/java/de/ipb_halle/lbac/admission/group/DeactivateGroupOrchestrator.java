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
package de.ipb_halle.lbac.admission.group;

import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class DeactivateGroupOrchestrator implements Serializable {

    private Logger logger = LogManager.getLogger(DeactivateGroupOrchestrator.class);

    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;
    @Inject
    private NodeService nodeService;

    @Inject
    private CloudNodeService cloudNodeService;
    
    @Inject
    protected KeyManager keymanager;
    
      public void startGroupDeactivation(Group group,User u) {
           for (Node node : nodeService.load(null, false)) {
            List<CloudNode> cnl = cloudNodeService.load(null, node);
            if ((cnl == null) || (cnl.isEmpty())) {
                continue;
            }
            CompletableFuture.supplyAsync(() -> {
                return deactivateGroupAnNode(u, cnl.get(0), group);
            }, managedExecutorService);
        }
      }
      
       private Boolean deactivateGroupAnNode(
            User u,
            CloudNode cn,
           Group groupToDeactivate) {
        try {
            DeactivateGroupWebClient client = new DeactivateGroupWebClient();
            client.deactivateGroupAtRemoteNodes(
                    u,
                    cn,
                    groupToDeactivate,
                    nodeService.getLocalNodeId(),
                    keymanager.getLocalPrivateKey(cn.getCloud().getName()));

        } catch (Exception e) {
            logger.error(String.format(
                    "Error at deactivating group %s at node %s",
                    groupToDeactivate.getName(),
                    cn.getNode().getId().toString()),
                    e
            );
            cn.fail();
            this.cloudNodeService.save(cn);
            return false;
        }
        cn.recover();
        this.cloudNodeService.save(cn);
        return true;
    }


}
