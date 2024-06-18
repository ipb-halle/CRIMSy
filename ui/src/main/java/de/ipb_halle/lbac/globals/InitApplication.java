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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.globals.health.HealthState;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MembershipWebService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.collections.CollectionWebService;
import de.ipb_halle.lbac.webservice.CloudNodeWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebService;
import de.ipb_halle.lbac.globals.health.HealthStateCheck;
import de.ipb_halle.lbac.globals.health.HealthStateRepair;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.service.MaterialService;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;

@Singleton
@Startup
@DependsOn("globalAdmissionContext")
public class InitApplication {

    private final static long serialVersionUID = 1L;
    private final static String PUBLIC_COLLECTION_NAME = "public";

    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    @Inject
    private FileService fs;
    private Logger logger = LogManager.getLogger(InitApplication.class);

    @Inject
    private KeyManager keyManager;

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;

    @PostConstruct
    public void init() {

        logInititaliseStart();
        try {
            healthCheck();
        } catch (Exception e) {
            logger.error("init(): error at healthcheck:", (Throwable) e);
        }
        restCheck();
        initialiseKeyManager();
        logger.info("---Finished initialisation.---");

    }

    private void logInititaliseStart() {
        logger.info("-----------------------");
        logger.info("-- LBAC startup init --");
        logger.info("-----------------------");
        logger.info("--- 1. check LBAC basics  -------------");
    }

    private void healthCheck() {
        HealthStateCheck healthChecker = new HealthStateCheck(
                infoObjectService,
                fs,
                PUBLIC_COLLECTION_NAME,
                nodeService,
                collectionService,
                taxonomyService);
        //*** check DB connection and db schema ***
        HealthState healthState = healthChecker.checkHealthState();
        HealthStateRepair healthRepairer = new HealthStateRepair(
                PUBLIC_COLLECTION_NAME,
                healthState,
                nodeService.getLocalNode(),
                collectionService,
                infoObjectService,
                this.globalAdmissionContext.getAdminOnlyACL(),
                this.globalAdmissionContext.getPublicReadACL(),
                this.globalAdmissionContext.getAdminAccount(),
                fs,
                materialService);

        if (healthRepairer.isRepairOfPublicCollectionNeeded()) {
            healthRepairer.repairPublicCollection();
        }

        if (healthRepairer.isTaxonomyRepairNeeded()) {
            healthRepairer.repairRootTaxonomy();
        }

        if (healthRepairer.isRepairOfJobSecretNeeded()) {
            healthRepairer.repairJobSecret();
        }
    }

    private void restCheck() {
        logger.info("--- 2. check rest api ---");
        logger.info("REST CollectionWebService: " + getRestApiDefaultPath(CollectionWebService.class));
        logger.info("REST CloudNodeWebService: " + getRestApiDefaultPath(CloudNodeWebService.class));
        logger.info("REST MembershipWebService: " + getRestApiDefaultPath(MembershipWebService.class));
        logger.info("REST TermVectorWebService: " + getRestApiDefaultPath(WordCloudWebService.class));

    }

    private void initialiseKeyManager() {
        try {
            if (keyManager == null) {
                logger.error("Could not initialise KeyManager in " + InitApplication.class.getName());
            } else {
                keyManager.updatePublicKeyOfLocalNode();
            }
        } catch (Exception e) {
            logger.error("Error at initialising keymanager:", (Throwable) e);
        }
    }
}
