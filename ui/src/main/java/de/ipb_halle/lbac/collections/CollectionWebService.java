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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Provides a webservice endpoint for getting collections.
 */
@Path("/collections")
@Stateless
public class CollectionWebService extends LbacWebService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private CollectionService collectionService;

    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private ACListService aclistService;

    /**
     * Returns a list of collections for a given user. The returned collections
     * are at least readable by the user. By the signature of the webRequest the
     * sending node is evaluated.
     *
     * @param request Request including a user, a nodesignature.
     * @return The List of found, readable collections.
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response getReadableCollections(CollectionWebRequest request) {
        try {
            checkAuthenticityOfRequest(request);
        } catch (NotAuthentificatedException e) {
            logger.error("Error at athentificating request", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        CollectionList result = new CollectionList();

        try {

            Map<String, Object> cmap = new HashMap<>();
            cmap.put("local", Boolean.TRUE);
            List<Collection> collections = this.collectionService.load(cmap);

            List<Collection> filteredColls = collections
                    .stream()
                    .filter(c -> aclistService.isPermitted(ACPermission.permREAD, c, request.getUser())) //remove Collections without read permission
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Collection c : filteredColls) {
                c.obfuscate();
            }
            result.setCollectionList(filteredColls);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Error at fetching local collections", e);
            return Response.serverError().build();
        }
    }
}
