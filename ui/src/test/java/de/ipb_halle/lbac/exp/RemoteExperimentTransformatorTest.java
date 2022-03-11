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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.Date;
import java.util.UUID;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class RemoteExperimentTransformatorTest {

    @Test
    public void test001_transformExperiment() {

        RemoteExperimentTransformer transformator = new RemoteExperimentTransformer(createExperiment());
        RemoteExperiment remoteExperiment = transformator.transformToRemote();
        Assert.assertEquals("EXP", remoteExperiment.getCode());
        Assert.assertNotNull(remoteExperiment.getCreationTime());
        Assert.assertEquals("EXP-DESC", remoteExperiment.getDescription());
        Assert.assertEquals(100, remoteExperiment.getId());
        Assert.assertEquals("EXP", remoteExperiment.getNameToDisplay());
        Assert.assertEquals("REMOTE-USER", remoteExperiment.getOwner().getNameToDisplay());
        Assert.assertEquals(0, remoteExperiment.getProjectId());
        Assert.assertEquals(SearchTarget.EXPERIMENT, remoteExperiment.getTypeToDisplay().getGeneralType());
        Assert.assertTrue(remoteExperiment.isEqualTo(remoteExperiment));
        RemoteExperiment otherRemoteExp = new RemoteExperiment();
        otherRemoteExp.setId(20000);
        Assert.assertFalse(remoteExperiment.isEqualTo(otherRemoteExp));
        Assert.assertFalse(remoteExperiment.isEqualTo("TEXT"));

    }

    private Experiment createExperiment() {

        Experiment exp = new Experiment(100,
                "EXP",
                "EXP-DESC",
                false,
                new ACList(),
                createRemoteUser(createRemoteNode()),
                new Date());

        return exp;
    }

    private Node createRemoteNode() {
        Node node = new Node();
        node.setBaseUrl("REMOTE-NODE-URL");
        node.setId(UUID.randomUUID());
        node.setInstitution("REMOTE-NODE-INST");
        node.setLocal(false);
        node.setPublicNode(false);
        node.setVersion("1.0");
        return node;
    }

    private User createRemoteUser(Node node) {
        User user = new User();
        user.setEmail("REMOTE-USER-EMAIL");
        user.setPhone("REMOTE-USER-PHONE");
        user.setId(1000);
        user.setName("REMOTE-USER");
        user.setLogin("REMOTE-USER-LOGIN");
        user.setNode(node);
        return user;
    }
}
