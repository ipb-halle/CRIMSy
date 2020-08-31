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
package de.ipb_halle.lbac.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Holds the current collections from local and remote nodes and has information
 * on which node a search for collections is active.
 *
 * @author fmauz
 */
public class CollectionSearchState implements Serializable {

    private List<Collection> readableCollections = Collections.synchronizedList(new ArrayList<>());
    private Set<UUID> unfinishedNodeRequests = Collections.synchronizedSet(new HashSet<>());
    private boolean currentCollectionsShown = false;

    /**
     * Returns the list of found local and remote readable collections for user
     *
     * @return
     */
    public List<Collection> getCollections() {
        return readableCollections;
    }

    /**
     * Sets list of readable collections
     *
     * @param collections
     */
    public void setCollections(List<Collection> collections) {
        this.readableCollections = collections;
    }

    /**
     * Returns IDs of Nodes for which a seach is still in progress
     *
     * @return
     */
    public Set<UUID> getUnfinishedNodeRequests() {
        return unfinishedNodeRequests;
    }

    /**
     * Sets the list of IDs of nodes for which a search is still in progress
     *
     * @param unfinishedNodeRequests
     */
    public void setUnfinishedNodeRequests(Set<UUID> unfinishedNodeRequests) {
        this.unfinishedNodeRequests = unfinishedNodeRequests;
    }

    /**
     * Signal that all searches for collections are complete.
     *
     * @return
     */
    public boolean isSearchComplete() {
        return unfinishedNodeRequests.isEmpty();
    }

    public void clearState() {
        this.readableCollections.clear();
        this.unfinishedNodeRequests.clear();
        currentCollectionsShown = false;
    }

    public boolean isCurrentCollectionsShown() {
        return currentCollectionsShown;
    }

    public void setCurrentCollectionsShown(boolean currentCollectionsShown) {
        this.currentCollectionsShown = currentCollectionsShown;
    }

    /**
     * Adds a new collection to the searchstate if it is not already present
     *
     * @param collsToAdd
     */
    public void addCollections(List<Collection> collsToAdd) {
        for (Collection c : collsToAdd) {
            boolean isAlreadyPresent = false;
            for (Collection c2 : readableCollections) {
                boolean sameID = c2.getId().equals(c.getId());
                boolean sameNode = true;
                if (c2.getNode() != null && c.getNode() != null) {
                    sameNode = c2.getNode().getId().equals(c.getNode().getId());
                }

                if (sameID && sameNode) {
                    isAlreadyPresent = true;
                }
            }
            if (!isAlreadyPresent) {
                readableCollections.add(c);
            }
        }
    }

}
