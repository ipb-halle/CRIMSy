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
package de.ipb_halle.lbac.globals.health;

import static de.ipb_halle.job.JobService.SETTING_JOB_SECRET;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.globals.health.HealthState.State;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.collections.CollectionService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Checks some basic preconditions for start of the programm.
 *
 * @author fmauz
 */
public class HealthStateCheck {

    private final String DB_SCHEMA_KEY = "DBSchema Version";

    private final Logger logger = LogManager.getLogger(HealthStateCheck.class);
    private InfoObjectService infoObjectService;
    private FileService fileService;
    private String PUBLIC_COLLECTION;
    private NodeService nodeService;
    private HealthState healthState;
    private CollectionService collectionService;
    private Collection publicCollection;
    private TaxonomyService taxonomyService;

    public HealthStateCheck(
            InfoObjectService infoObjectService,
            FileService fileService,
            String PUBLIC_COLLECTION,
            NodeService nodeService,
            CollectionService collectionService,
            TaxonomyService taxonomyService) {
        this.infoObjectService = infoObjectService;
        this.fileService = fileService;
        this.PUBLIC_COLLECTION = PUBLIC_COLLECTION;
        this.nodeService = nodeService;
        this.collectionService = collectionService;
        this.healthState = new HealthState();
        this.publicCollection = collectionService.getPublicCollectionFromDb();
        this.taxonomyService = taxonomyService;

    }

    /**
     * Checks the db state of the local node and the public collection. It also
     * checks if the local collections are in synchronity with filesysten. If
     * the public collection is out of sync the information in the database will
     * be updated based on the information from filesystem. If no root taxonomy
     * is present one will be created
     *
     * @return The state of the preconditions
     */
    public HealthState checkHealthState() {
        checkDbState();
        checkFileSystemState();
        checkJobSecret();
        checkPublicCollectionSync();
        checkSyncOfLocalCollections();
        checkRootTaxonomy();
        return reportHealthState();
    }

    /**
     * Checks the version of the db shemata and the precence of the local node
     * and the public collection in the database.
     */
    private void checkDbState() {
        //Get Shemata Version
        try {
            InfoObject infoDBSchema = infoObjectService.loadByKey(DB_SCHEMA_KEY);
            if (infoDBSchema != null) {
                healthState.dbVersion = infoDBSchema.getKey() + ":" + infoDBSchema.getValue();
            }
        } catch (Exception e) {
            logger.error(String.format("Exception in LBAC DBSchema Version check."));
        }
        //*** check local node entry in DB ***
        if (!checkLocalNode()) {
            healthState.localNodeDbState = HealthState.State.FAILED;
        } else {
            healthState.localNodeDbState = HealthState.State.OK;
        }

        if (publicCollection == null) {
            healthState.publicCollectionDbState = HealthState.State.FAILED;
        } else {
            healthState.publicCollectionDbState = HealthState.State.OK;
        }

    }

    /**
     * Checks if there is a local directory for the public collection
     */
    private void checkFileSystemState() {
        Collection c = new Collection();
        c.setName("public");
        boolean localFileSystemCollectionCheck = Files.exists(Paths.get(c.getStoragePath()));
        if (!localFileSystemCollectionCheck) {
            healthState.publicCollectionFileState = HealthState.State.FAILED;
            logger.error(String.format("local file repository '%s' not found.", c.getStoragePath()));
        } else {
            healthState.publicCollectionFileState = HealthState.State.OK;
        }

    }

    private void checkJobSecret() {
        try {
            if (infoObjectService.loadByKey(SETTING_JOB_SECRET).getValue() != null) {;
                healthState.jobSecretState = HealthState.State.OK;
            }
        } catch (Exception e) {
            healthState.jobSecretState = HealthState.State.FAILED;
        }
    }

    private void checkRootTaxonomy() {
        if (taxonomyService.checkRootTaxonomy() == 0) {
            healthState.rootTaxonomy = HealthState.State.FAILED;
        } else {
            healthState.rootTaxonomy = HealthState.State.OK;
        }
    }

    /**
     * Checks if the information of the public collection in the database are in
     * sync with the information in the filesystem.
     */
    private void checkPublicCollectionSync() {
        if (publicCollection != null) {
            String collNameInDb = publicCollection.getStoragePath();
            if (collNameInDb != null && healthState.publicCollectionDbState == HealthState.State.OK) {

                //Check if the name of the public collection in db is in sync with the
                //name in the local file system
                if (healthState.publicCollectionFileState == HealthState.State.OK
                        && collNameInDb.equals(fileService.getUploadPath(publicCollection).toString())) {
                    healthState.publicCollectionFileSyncState = HealthState.State.OK;
                } else {
                    healthState.publicCollectionFileSyncState = HealthState.State.FAILED;
                }

            } else {
                healthState.publicCollectionFileSyncState = HealthState.State.FAILED;

            }
        } else {
            healthState.publicCollectionFileState = HealthState.State.FAILED;
            healthState.publicCollectionFileSyncState = HealthState.State.FAILED;

        }

    }

    /**
     * Checks if the local node exists in the database.
     *
     * @return
     */
    private boolean checkLocalNode() {
        try {
            Node node = nodeService.getLocalNode();
            if (node != null) {
                logger.info(String.format("LBAC local node id:%s  location: %s found.", node.getId(), node.getInstitution()));
                return true;
            } else {
                logger.error(String.format("no local node entry found in table nodes."));
                return false;
            }
        } catch (Exception e) {
            logger.error(String.format("Exception in LBAC node check."));
            return false;
        }
    }

    /**
     * Logs the information of the healt state in the current log.
     *
     * @return
     */
    private HealthState reportHealthState() {
        //*** summary ***
        logger.info("--- start of health check report ---");
        logger.info(String.format("* DB version check: %sOK", healthState.dbVersion != null ? "" : "NOT "));
        logger.info(String.format("* local node check: %sOK", healthState.localNodeDbState == State.OK ? "" : "NOT "));

        logger.info(String.format("* local public collection in db: %s", healthState.publicCollectionDbState));
        logger.info(String.format("* local public collection in sync with file: %s", healthState.publicCollectionFileState));
        logger.info(String.format("* job secret configured: %s", healthState.jobSecretState));

        boolean publicColInSync = healthState.publicCollectionFileSyncState == State.OK;
        logger.info(String.format("* local public collection sync check: %sOK", publicColInSync ? "" : "NOT "));

        for (String name : healthState.collectionFileSyncList.keySet()) {

            State stateOfFile = healthState.collectionFileSyncList.get(name);
            logger.info("** name: " + name + " File: " + stateOfFile);
        }

        logger.info("--- end of health check report ---");
        return healthState;
    }

    /**
     * Checks if information of the local collections are in sync with the
     * information in the filesystem.
     *
     */
    private void checkSyncOfLocalCollections() {
        try {
            Map<String, Object> cmap = new HashMap<>();
            cmap.put("local", true);
            List<Collection> collectionList = collectionService.load(cmap);

            for (Iterator<Collection> collectionIterator = collectionList.iterator(); collectionIterator.hasNext();) {

                Collection collection = collectionIterator.next();
                if (fileService.storagePathExists(collection)) {
                    healthState.collectionFileSyncList.put(collection.getName(), State.OK);
                } else {
                    healthState.collectionFileSyncList.put(collection.getName(), State.FAILED);
                }

            }
        } catch (Exception e) {
            logger.error(String.format("Exception in LBAC all collection check."));
        }
    }

}
