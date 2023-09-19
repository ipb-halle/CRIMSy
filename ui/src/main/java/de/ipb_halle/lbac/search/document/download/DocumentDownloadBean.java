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
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author flange
 */
@RequestScoped
@Named
public class DocumentDownloadBean {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private UserBean userBean;

    @Inject
    private NodeService nodeService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private DocumentWebClient client;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private ACListService acListService;

    @Inject
    private SendFileBean sendFileBean;

    public void actionDownloadDocument(Document document) throws IOException {
        InputStream is = null;
        if (nodeService.isRemoteNode(document.getNode())) {
            is = downloadRemote(document);
        } else {
            is = downloadLocal(document);
        }

        if (is != null) {
            sendFileBean.sendFile(is, document.getOriginalName());
        }
    }

    /**
     * Request a download of a local/remote document. The response is offered as
     * file download to the user.
     *
     * @param netObject
     * @throws IOException
     */
    public void actionDownload(NetObject netObject) throws IOException {
        if (netObject.getSearchable().getTypeToDisplay().getGeneralType() != SearchTarget.DOCUMENT) {
            return;
        }
        Document document = (Document) netObject.getSearchable();
        actionDownloadDocument(document);
    }

    private InputStream downloadLocal(Document document) throws IOException {
        Collection collection = document.getCollection();
        if (!acListService.isPermitted(ACPermission.permREAD, collection, userBean.getCurrentAccount())) {
            return null;
        }

        FileObject fileObject = fileEntityService.getFileEntity(document.getId());
        if ((fileObject == null) || (fileObject.getFileLocation() == null)) {
            return null;
        }
        return streamLocalFile(fileObject);
    }

    private InputStream streamLocalFile(FileObject fileObject) throws IOException {
        String location = fileObject.getFileLocation();
        File file = new File(location);
        if (!file.exists()) {
            logger.error("Requested file with id={} does not exist at location={}", fileObject.getId(), location);
            return null;
        }
        return new FileInputStream(file);
    }

    private InputStream downloadRemote(Document document) throws IOException {
        CloudNode cn = getCloudNodeForNode(document.getNode());
        User user = userBean.getCurrentAccount();
        return client.downloadDocument(cn, user, document);
    }

    private CloudNode getCloudNodeForNode(Node node) {
        return cloudNodeService.load(null, node).get(0);
    }
}
