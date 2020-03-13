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
package de.ipb_halle.lbac.globals.health;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.globals.health.HealthState.State;
import de.ipb_halle.lbac.service.CollectionService;
import java.util.UUID;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Tries to repair some discrepancies found by the healtcheck procedere
 *
 * @author fmauz
 */
public class HealthStateRepair {

    private String publicCollectionName;
    private Collection publicCollection;
    private Logger logger = LogManager.getLogger(HealthStateRepair.class);
    private HealthState healthState;
    private Node localNode;
    private CollectionService collectionService;
    private ACList publicReadAcl;
    private User adminAccount;
    private FileService fileService;
    private SolrAdminService solrAdminService;

    public HealthStateRepair(String publicCollectionName,
            HealthState healthState,
            Node localNode,
            CollectionService collectionService,
            ACList publicReadAcl,
            User adminAccount,
            FileService fileService,
            SolrAdminService solrAdminService) {

        this.publicCollectionName = publicCollectionName;
        this.healthState = healthState;
        this.localNode = localNode;
        this.collectionService = collectionService;
        this.publicReadAcl = publicReadAcl;
        this.adminAccount = adminAccount;
        this.fileService = fileService;
        this.solrAdminService = solrAdminService;
        this.publicCollection = collectionService.getPublicCollectionFromDb();
    }

    /**
     * Tries to repair the following discrepancies
     * <ol>
     * <li>if no public collection was found creates one</li>
     * <li>if the local filesystem is not in sync with the information in the
     * database update the information in database</li>
     * <li>if the local solr collection is not in sync with the information in
     * the database update the information in database</li>
     * </ol>
     *
     * @return the updated health state
     */
    public HealthState repairPublicCollection() {

        if (healthState.publicCollectionDbState == State.FAILED) {
            logger.info("Start creation of new public collection");
            publicCollection = createPublicCollectionInDb();
            healthState.publicCollectionSolrSyncState = State.OK;
            healthState.publicCollectionFileSyncState = State.OK;
        }
        try {
            if (healthState.publicCollectionFileState == State.FAILED) {
                repairPublicCollectionInFileSystem();
                if (updatePublicCollectionInDb()) {
                    healthState.publicCollectionFileState = State.OK;
                    healthState.publicCollectionFileSyncState = State.OK;
                }
            }
            if (healthState.publicCollectionSolrState == State.FAILED) {
                repairPublicCollectionInSolR();
                if (updatePublicCollectionInDb()) {
                    healthState.publicCollectionSolrState = State.OK;
                    healthState.publicCollectionSolrSyncState = State.OK;
                }
            }
        } catch (Exception e) {
            logger.error("Could not vreate solr or file sync", e);
        }
        return healthState;
    }

    /**
     * The repair is nessecary if one of the following conditions is met
     * <ul>
     * <li> no public collection was found in db</li>
     * <li> the public collection is not in sync with the filesystem or
     * solr</li>
     * <li> there is no folder for the collection in the filesystem</li>
     * <li> there is no collection in the solr instance for the collection</li>
     * </ul>
     *
     * @return true if repair is needed
     */
    public boolean isRepairOfPublicCollectionNeeded() {
        if (healthState.publicCollectionDbState == State.FAILED
                || healthState.publicCollectionFileState == State.FAILED
                || healthState.publicCollectionFileSyncState == State.FAILED
                || healthState.publicCollectionSolrState == State.FAILED
                || healthState.publicCollectionSolrSyncState == State.FAILED) {
            return true;
        }
        return false;
    }

    private void repairPublicCollectionInFileSystem() {

        if (fileService.createDir(publicCollectionName)) {
            logger.info(String.format(
                    "repository for standard collection  %s under %s created.",
                    publicCollectionName,
                    fileService.getStoragePath(publicCollectionName)));
        } else {
            logger.error(
                    String.format("repository for for standard collection  %s could not created.",
                            publicCollectionName));
        }
    }

    private void repairPublicCollectionInSolR() {
        if (!solrAdminService.collectionExists(publicCollection)) {
            if (solrAdminService.createCollection(publicCollectionName)) {
                logger.info(String.format("solr standard collection  %s created.", publicCollectionName));
            } else {
                logger.error(String.format("solr standard collection  %s could not created.", publicCollectionName));
            }
        }
    }

    private Collection createPublicCollectionInDb() {
        try {
            publicCollection = new Collection();
            publicCollection.setId(UUID.randomUUID());
            publicCollection.setName(publicCollectionName);
            publicCollection.setStoragePath(String.format("%s/%s", FileService.getDefaultPath(), publicCollectionName));
            publicCollection.setIndexPath(String.format("%s/%s", SolrAdminService.getBaseURLSolr(), publicCollectionName));
            publicCollection.setNode(localNode);
            publicCollection.setOwner(adminAccount);
            publicCollection.setACList(publicReadAcl);
            collectionService.save(publicCollection);
            return publicCollection;
        } catch (Exception e) {
            logger.error("Could not create public collection", e);
            return null;
        }

    }

    private boolean updatePublicCollectionInDb() {
        try {
            publicCollection.setName(publicCollectionName);
            publicCollection.setStoragePath(String.format("%s/%s", FileService.getDefaultPath(), publicCollectionName));
            publicCollection.setIndexPath(String.format("%s/%s", SolrAdminService.getBaseURLSolr(), publicCollectionName));
            publicCollection.setNode(localNode);
            publicCollection.setOwner(adminAccount);
            publicCollection.setACList(publicReadAcl);

            collectionService.save(publicCollection);
            return true;

        } catch (Exception e) {
            logger.error("Could not update public collection", e);
            return false;
        }
    }
}
