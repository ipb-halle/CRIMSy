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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.jsf.SendFileBean;

/**
 * 
 * @author flange
 */
@RequestScoped
@Named
public class DocumentDownloadBean {
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
    private SendFileBean sendFileBean;

    /**
     * Request a download of a local/remote document. The response is offered as
     * file download to the user.
     * 
     * @param no
     * @throws IOException
     */
    public void actionDownload(NetObject no) throws IOException {
        if (no.getSearchable().getTypeToDisplay().getGeneralType() != SearchTarget.DOCUMENT) {
            return;
        }
        Document document = (Document) no.getSearchable();

        if (isLocalNode(document.getNode())) {
            downloadLocal(document);
        } else {
            downloadRemote(document);
        }
    }

    private boolean isLocalNode(Node node) {
        return nodeService.getLocalNode().equals(node);
    }

    private void downloadLocal(Document document) throws IOException {
        FileObject file = fileEntityService.getFileEntity(document.getId());
        InputStream is = new FileInputStream(new File(file.getFileLocation()));
        sendFileBean.sendFile(is, document.getOriginalName());
    }

    private void downloadRemote(Document document) throws IOException {
        CloudNode cn = getCloudNodeForNode(document.getNode());
        User user = userBean.getCurrentAccount();
        InputStream is = client.downloadDocument(cn, user, document);
        sendFileBean.sendFile(is, document.getOriginalName());
    }

    private CloudNode getCloudNodeForNode(Node node) {
        return cloudNodeService.load(null, node).get(0);
    }
}