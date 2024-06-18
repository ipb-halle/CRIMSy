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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
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
        User obfuscatedUser = new User(user.createEntity(), user.getNode());
        obfuscatedUser.obfuscate();
        Set<Group> obfuscatedGroups = getGroupsOfUser(user);

        for (Node node : nodeService.load(null, false)) {

            List<CloudNode> cnl = cloudNodeService.load(null, node);
            if ((cnl == null) || (cnl.size() == 0)) {
                continue;
            }

            CompletableFuture.supplyAsync(() -> {
                return startAnnouncment(obfuscatedUser, cnl.get(0), obfuscatedGroups);
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
     * Returns an obfuscated set of groups for a given user. The returned groups
     * are located at the local node. Groups of subsystem type
     * <code>BUILTIN</code> and <code>LBAC_REMOTE</code> are excluded.
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
                            && (g.getSubSystemType() != AdmissionSubSystemType.BUILTIN)
                            && (g.getSubSystemType() != AdmissionSubSystemType.LBAC_REMOTE)) {
                        g.obfuscate();
                        groups.add(g);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("getGroupsOfUser() caught an exception:", (Throwable) e);
            }
        }
        return groups;
    }

}
