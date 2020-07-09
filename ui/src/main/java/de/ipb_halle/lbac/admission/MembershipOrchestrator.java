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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Membership;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class MembershipOrchestrator implements Serializable {

    private Logger LOGGER = LogManager.getLogger(MembershipOrchestrator.class);

    @Inject
    protected KeyManager keymanager;

    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;

    final List<CompletableFuture<Boolean>> taskList = new ArrayList<>();

    @Inject
    private MembershipService membershipService;

    @Inject
    private NodeService nodeService;

    @Inject
    private CloudNodeService cloudNodeService;

    public void startMemberShipAnnouncement(User user) {
        for (Node node : nodeService.load(null, false)) {

            List<CloudNode> cnl = cloudNodeService.load(null, node);
            if ((cnl == null) || (cnl.size() == 0)) {
                continue;
            }

            CompletableFuture.supplyAsync(() -> {
                return startAnnouncment(user, cnl.get(0), getGroupsOfUser(user));
            }, managedExecutorService);
        }
    }

    private Boolean startAnnouncment(
            User u,
            CloudNode cn,
            Set<Group> groupsOfUser) {
        try {
            MembershipWebClient client = new MembershipWebClient();
            client.announceUserToRemoteNodes(
                    u,
                    cn,
                    groupsOfUser,
                    nodeService.getLocalNodeId(),
                    keymanager.getLocalPrivateKey(cn.getCloud().getName()));

        } catch (Exception e) {
            LOGGER.error(String.format(
                    "Error at announcing user %s to node %s",
                    u.getId().toString(),
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

    /**
     * Returns the groups of a user which are located at the public node or the
     * local node. All other groups are excluded
     *
     * @param u
     * @return
     */
    private Set<Group> getGroupsOfUser(User u) {
        Set<Membership> groupsOfUser = membershipService.loadMemberOf(u);

        Set<Group> groups = new HashSet<>();
        for (Membership m : groupsOfUser) {
            try {
                if (m.getGroup() instanceof Group) {
                    Group g = (Group) m.getGroup();
                    if (g.getNode().equals(nodeService.getLocalNode())
                            || g.getNode().getPublicNode()) {
                        groups.add(g);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        return groups;
    }

}
