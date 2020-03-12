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
package de.ipb_halle.lbac.collections.mock;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mocks the CollectionWebClient and simulates an request to a remote Server.
 * Returns the
 *
 * @author fmauz
 */
public class CollectionWebClientMock extends CollectionWebClient {

    private final int waitingDurationInMiliSec;
    private final int amountOfReadableColls;

    /**
     * Creates a WebClient Mock
     *
     * @param duration Provoked lag in milliseconds to simulate asynchrone
     * responses
     * @param amountOfReadableColls number of found, readable collections for
     * user
     */
    public CollectionWebClientMock(int duration, int amountOfReadableColls) {
        waitingDurationInMiliSec = duration;
        this.amountOfReadableColls = amountOfReadableColls;

    }

    /**
     * Simulates an webrequest for a List of readable collections
     *
     * @param n Node from which the collections are requested
     * @param u User for who the collections are requested
     * @return List of readable collections for user u
     */
    @Override
    public List<Collection> getCollectionsFromRemoteNode(CloudNode cn, User u) {
        try {
            Thread.sleep(waitingDurationInMiliSec);
        } catch (InterruptedException e) {
        }
        List<Collection> collections = new ArrayList<>();
        ACList acListReadable = new ACList();
        acListReadable.addACE(u, new ACPermission[]{ACPermission.permREAD});

        for (int i = 0; i < amountOfReadableColls; i++) {
            Collection readable = new Collection();
            readable.setACList(acListReadable);
            readable.setDescription(
                    String.format(
                            "Readable Collection %d for User %s from remote Node",
                            i,
                            u.getName()
                    )
            );
            readable.setIndexPath("/");
            readable.setName(String.format("READ-COL%d", i));
            readable.setNode(cn.getNode());
            readable.setId(UUID.randomUUID());
            readable.setOwner(u);
            collections.add(readable);
        }

        return collections;
    }
}
