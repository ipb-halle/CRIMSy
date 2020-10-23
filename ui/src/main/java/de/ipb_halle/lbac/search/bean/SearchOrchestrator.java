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

import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.document.DocumentSearchRequest;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
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
public class SearchOrchestrator implements Serializable {

    private final String REST_PATH = "/rest/search";
    private final Logger logger = LogManager.getLogger(this.getClass());
    final List<CompletableFuture<DocumentSearchRequest>> taskList = new ArrayList<>();
    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private KeyManager keyManager;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private NodeService nodeService;
    
     public void orchestrate(
            List<SearchRequest> requests,
            SearchState documentState) {
         
        
         
     }
}
