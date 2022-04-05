/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document.download;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.webclient.LbacWebClient;

/**
 * REST client for {@link DocumentWebService}.
 * 
 * @author flange
 */
public class DocumentWebClient extends LbacWebClient {
    private static final long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * Requests a file download of the given document from the given cloud node.
     * 
     * @param cn
     * @param user
     * @param document
     * @return
     */
    public InputStream downloadDocument(CloudNode cn, User user, Document document) {
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(document.getId());

        try {
            signWebRequest(request, cn.getCloud().getName(), user);

            WebClient wc = createWebclient(cn, DocumentWebService.class);
            wc.accept(MediaType.APPLICATION_OCTET_STREAM);
            Response response = wc.post(request);

            if (response.getStatus() == Status.OK.getStatusCode()) {
                return response.readEntity(InputStream.class);
            } else {
                logger.error("Received response {} from node with id={}", response.getStatus(), cn.getNode().getId());
                return null;
            }
        } catch (Exception e) {
            cn.fail();
            logger.error(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}