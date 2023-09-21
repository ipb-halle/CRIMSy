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
package de.ipb_halle.lbac.base;

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.service.NodeService;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import org.apache.openejb.loader.Files;

/**
 *
 * @author fmauz
 */
public class DocumentCreator {

    protected Collection col;
    protected AsyncContextMock asynContext;
    protected String exampleDocsRootFolder = "target/test-classes/exampledocs/";

    protected FileObjectService fileObjectService;
    protected CollectionService collectionService;
    protected NodeService nodeService;
    protected TermVectorService termVectorService;
    protected User user;

    public DocumentCreator(
            FileObjectService fileObjectService,
            CollectionService collectionService,
            NodeService nodeService,
            TermVectorService termVectorService) {

        this.fileObjectService = fileObjectService;
        this.collectionService = collectionService;
        this.nodeService = nodeService;
        this.termVectorService = termVectorService;
    }

    public Collection uploadDocuments(User user, String collectionName, String... files) throws FileNotFoundException, InterruptedException {
        Files.delete(Paths.get("target/test-classes/collections").toFile());
        this.user = user;
        createAndSaveNewCol(collectionName);
        for (String file : files) {
            uploadDocument(file);
        }
        return col;
    }

    private void createAndSaveNewCol(String colName) {
        col = new Collection();
        col.setACList(GlobalAdmissionContext.getPublicReadACL());
        col.setDescription("xxx");
        col.setIndexPath("/");
        col.setName(colName);
        col.setNode(nodeService.getLocalNode());
        col.setOwner(user);
        col.setStoragePath("/");
        col = collectionService.save(col);
        col.COLLECTIONS_BASE_FOLDER = "target/test-classes/collections";
    }

    private void uploadDocument(String documentName) throws FileNotFoundException, InterruptedException {
        asynContext = new AsyncContextMock(
                new File(exampleDocsRootFolder + documentName),
                col.getName());
        UploadToColMock upload = new UploadToColMock(
                fileObjectService,
                user,
                asynContext,
                collectionService,
                "target/test-classes/collections");

        upload.run();
        while (!asynContext.isComplete()) {
            Thread.sleep(500);
        }
    }
}
