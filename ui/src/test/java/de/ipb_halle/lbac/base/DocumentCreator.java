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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FilterDefinitionInputStreamFactory;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.FileUploadCollectionMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
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

    protected FileEntityService fileEntityService;
    protected CollectionService collectionService;
    protected NodeService nodeService;
    protected TermVectorEntityService termVectorEntityService;
    protected User user;

    public DocumentCreator(
            FileEntityService fileEntityService,
            CollectionService collectionService,
            NodeService nodeService,
            TermVectorEntityService termVectorEntityService) {

        this.fileEntityService = fileEntityService;
        this.collectionService = collectionService;
        this.nodeService = nodeService;
        this.termVectorEntityService = termVectorEntityService;
    }

    public Collection uploadDocuments(User user, String collectionName, String... files) throws FileNotFoundException, InterruptedException {
        Files.delete(Paths.get(FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER).toFile());
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
        col.COLLECTIONS_BASE_FOLDER = FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER;
    }

    private void uploadDocument(String documentName) throws FileNotFoundException, InterruptedException {
        asynContext = new AsyncContextMock(
                new File(exampleDocsRootFolder + documentName),
                col.getName());
        UploadToColMock upload = new UploadToColMock(
                FilterDefinitionInputStreamFactory.getFilterDefinition(),
                fileEntityService,
                user,
                asynContext,
                collectionService,
                termVectorEntityService,
                FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER);

        upload.run();
        while (!asynContext.isComplete()) {
            Thread.sleep(500);
        }
    }
}
