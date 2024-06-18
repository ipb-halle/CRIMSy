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

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;

import java.io.File;
import java.io.InputStream;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Webservice for download of documents.
 * 
 * @author flange
 */
@Path("/download")
public class DocumentWebService extends LbacWebService {
    private static final Response FILE_NOT_FOUND = Response.status(Status.NOT_FOUND).build();
    private static final Response FORBIDDEN = Response.status(Status.FORBIDDEN).build();

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private FileObjectService fileObjectService;
    
    @Inject
    private CollectionService collectionService;

    @Inject
    private ACListService acListService;

    @Inject
    private MemberService memberService;

    /**
     * Provides a download of a document (identified by its fileObject id).
     * 
     * @param request
     * @return {@link Response} with {@link InputStream} of the file
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(DocumentWebRequest request) {
        Node node;
        try {
            node = checkAuthenticityOfRequest(request);
        } catch (NotAuthentificatedException e) {
            return FORBIDDEN;
        }

        FileObject fileObject = fileObjectService.loadFileObjectById(request.getFileObjectId());
        if ((fileObject == null) || (fileObject.getFileLocation() == null)) {
            return FILE_NOT_FOUND;
        }

        Collection collection = collectionService.loadById(fileObject.getCollectionId());
        User localUser = memberService.mapRemoteUserToLocalUser(request.getUser(), node);
        if (!acListService.isPermitted(ACPermission.permREAD, collection, localUser)) {
            return FORBIDDEN;
        }

        return responseWithLocalFile(fileObject);
    }

    private Response responseWithLocalFile(FileObject fileObject) {
        String location = fileObject.getFileLocation();
        File file = new File(location);
        if (!file.exists()) {
            logger.error("Requested file with id={} does not exist at location={}", fileObject.getId(), location);
            return FILE_NOT_FOUND;
        }
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
    }
}
