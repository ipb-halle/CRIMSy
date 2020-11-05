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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.search.NetObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class SearchState {
    
    private List<NetObject> foundObjects = new ArrayList<>();
    private Set<UUID> activeSearches = new HashSet<>();
    private float averageDocLength;
    private int totalDocs;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    
    public synchronized void addNetObjects(List<NetObject> objectsToAdd) {
        for (NetObject noToAdd : objectsToAdd) {
            boolean alreadyIn = false;
            for (NetObject no : foundObjects) {
                if (no.isEqualTo(noToAdd)) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                logger.info("Remove node " + noToAdd.getNode().getId());
                activeSearches.remove(noToAdd.getNode().getId());
                foundObjects.add(noToAdd);
            }
        }
    }
    
    public void addNoteToSearch(UUID nodeId) {
        activeSearches.add(nodeId);
    }
    
    public List<NetObject> getFoundObjects() {
        return foundObjects;
    }
    
    public boolean isSearchActive() {
        return !activeSearches.isEmpty();
    }
    
    public float getAverageDocLength() {
        return averageDocLength;
    }
    
    public int getTotalDocs() {
        return totalDocs;
    }
    
    public synchronized void addNewStats(int newTotalDocs, float newAvgLength) {
        averageDocLength = (newAvgLength * newTotalDocs + averageDocLength * totalDocs) / (totalDocs + newTotalDocs);
        totalDocs += newTotalDocs;
    }
    
}
