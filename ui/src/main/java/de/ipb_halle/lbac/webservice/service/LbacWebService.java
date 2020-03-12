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
package de.ipb_halle.lbac.webservice.service;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.webservice.WebRequest;
import javax.inject.Inject;

/**
 * Generalized WebService which implements basic functionality e.g.
 * authentification
 *
 * @author fmauz
 */
public abstract class LbacWebService {

    @Inject
    protected WebRequestAuthenticator authenticator;

    @Inject
    protected CloudNodeService cloudNodeService;

    public void checkAuthenticityOfRequest(WebRequest webReq) throws NotAuthentificatedException {
        try {
            CloudNode cloudNode = this.cloudNodeService.loadCloudNode(webReq.getCloudName(), webReq.getNodeIdOfRequest());

            if (!authenticator.isAuthentificated(
                    webReq.getSignature(),
                    cloudNode)) {
                throw new NotAuthentificatedException("Node of Webrequest ist not authentificated.");
            }
        } catch (Exception e) {
            if (webReq == null) {
                throw new NotAuthentificatedException("Webrequest was null", e);
            }
            if (webReq.getNodeIdOfRequest() == null) {
                throw new NotAuthentificatedException("Nodeid of webrequest was null", e);
            }
            throw new NotAuthentificatedException("Could note authentificate request from node " + webReq.getNodeIdOfRequest().toString(), e);
        }
    }

    public void setAuthenticator(WebRequestAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setCloudNodeService(CloudNodeService cns) {
        this.cloudNodeService = cns;
    }
}
