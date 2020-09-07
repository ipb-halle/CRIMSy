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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.StemmedWordOrigin;
import de.ipb_halle.lbac.file.TermVectorParser;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.ContentStreamBase;

/**
 *
 * @author fmauz
 */
public class CollectionOperation implements Serializable{

    private final FileService fileService;
    private final FileEntityService fileEntityService;
    private final GlobalAdmissionContext globalAdmissionContext;
    private final Logger LOGGER = LogManager.getLogger(CollectionOperation.class);
    private final SolrAdminService solrAdminService;
    private final NodeService nodeService;
    private final CollectionService collectionService;
    private final SolrTermVectorSearch solrTermVectorSearch;
    private final TermVectorEntityService termVectorEntityService;
    private final SolrSearcher solrSearcher;
    private final String PUBLIC_COLLECTION_NAME;
    private final String COLL_NOT_UNIQUE = "name %s for a new collection  is not unique. (user: %s)";
    private final String COLL_RESERVED = "name %s for a new collection  is a reserved name. (user: %s)";
    private final String COLL_CREATE = "start creating collection: %s. (user: %s)";
    private final String COLL_DELETE_FORBIDDEN = "deleting collection %s forbidden! unauthorized access detected by user: %s";
    private final String COLL_DELETED = "collection deleted: %s  by %s";
    private final String COLL_START_DELETE = "start collection delete: %s  by %s";

    public CollectionOperation(
            FileService fileService,
            FileEntityService fileEntityService,
            GlobalAdmissionContext globalAdmissionContext,
            SolrAdminService solrAdminService,
            NodeService nodeService,
            CollectionService collectionService,
            String publicCollectionName,
            SolrTermVectorSearch solrTermVectorSearch,
            TermVectorEntityService termVectorEntityService,
            SolrSearcher solrSearcher) {

        this.fileService = fileService;
        this.fileEntityService = fileEntityService;
        this.globalAdmissionContext = globalAdmissionContext;
        this.solrAdminService = solrAdminService;
        this.nodeService = nodeService;
        this.collectionService = collectionService;
        this.PUBLIC_COLLECTION_NAME = publicCollectionName;
        this.solrTermVectorSearch = solrTermVectorSearch;
        this.termVectorEntityService = termVectorEntityService;
        this.solrSearcher = solrSearcher;
    }

    public enum OperationState {
        OPERATION_SUCCESS, //Operation was successfull
        CREATION_RESERVED_NAME, // name of collection can not be choosen
        CREATION_DUPLICATE_NAME, // collectionname already exists
        CLEAR_ERROR, // undefined error at clearing a collection
        DELETE_FORBIDDEN

    }

    /**
     * Removes all documents from the solr instance and deletes all files from
     * the local filesystem. Removes all file entries from database.
     *
     * @param activeCollection
     * @param currentAccount
     * @return state of the operation
     */
    public OperationState clearCollection(
            Collection activeCollection,
            User currentAccount) {

        if (!activeCollection.getNode().getLocal()) {
            return OperationState.DELETE_FORBIDDEN;
        }
        try {

            if (fileService.storagePathExists(activeCollection.getName())) {
                fileService.deleteDir(activeCollection.getName());
            }

            termVectorEntityService.deleteTermVectorOfCollection(activeCollection);
            fileEntityService.delete(activeCollection);
            LOGGER.info(String.format("collection delete all file entities: %s:%s by %s", activeCollection.getName(), activeCollection.getIndexPath(), currentAccount.getLogin()));

            if (solrAdminService.collectionExists(activeCollection)) {
                solrAdminService.deleteAllDocuments(activeCollection);
                LOGGER.info(String.format("collection delete all documents: %s:%s by %s", activeCollection.getName(), activeCollection.getIndexPath(), currentAccount.getLogin()));
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OperationState.CLEAR_ERROR;
        }
        return OperationState.OPERATION_SUCCESS;
    }

    /**
     * Deletes a collection from filesystem and in the solr instance
     *
     * @param activeCollection
     * @param currentAccount
     * @return state of the operation
     */
    public OperationState deleteCollection(
            Collection activeCollection,
            User currentAccount) {
        log(COLL_START_DELETE, activeCollection, currentAccount);

        if (activeCollection.getName().equalsIgnoreCase(PUBLIC_COLLECTION_NAME)) {
            log(COLL_DELETE_FORBIDDEN, activeCollection, currentAccount);
            return OperationState.DELETE_FORBIDDEN;
        }

        if (activeCollection.getNode().getLocal() && solrAdminService.collectionExists(activeCollection)) {
            solrAdminService.unloadCollection(activeCollection);

        }

        clearCollection(activeCollection, currentAccount);
        collectionService.delete(activeCollection);

        log(COLL_DELETED, activeCollection, currentAccount);
        return OperationState.OPERATION_SUCCESS;
    }

    /**
     * Create a collection in solr and on filesystem and saves it to the db.
     *
     * @param activeCollection
     * @param currentAccount Owner of the collection
     * @return final state of operation @see OperationState
     */
    public OperationState createCollection(Collection activeCollection, User currentAccount) {
        log(COLL_CREATE, activeCollection, currentAccount);
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", activeCollection.getName());
        cmap.put("local", true);
        List<Collection> collList = collectionService.load(cmap);
        if (!collList.isEmpty()) {
            log(COLL_NOT_UNIQUE, activeCollection, currentAccount);
            return OperationState.CREATION_DUPLICATE_NAME;
        }
        if (activeCollection.getName().equalsIgnoreCase("configsets")) {
            log(COLL_RESERVED, activeCollection, currentAccount);
            return OperationState.CREATION_RESERVED_NAME;
        }

        activeCollection.setNode(nodeService.getLocalNode());
        activeCollection.setCountDocs(0L);
        activeCollection.setOwner(currentAccount);
        activeCollection.setACList(this.globalAdmissionContext.getOwnerAllPermACL());

        //*** create storagePath and check path ***
        if (!fileService.storagePathExists(activeCollection.getName())) {
            fileService.createDir(activeCollection.getName());
        }
        activeCollection.setStoragePath(fileService.getStoragePath(activeCollection.getName()));
        //*** create solr collection and check ***
        if (!solrAdminService.collectionExists(activeCollection)) {
            solrAdminService.createCollection(activeCollection);
        }
        activeCollection.setIndexPath(solrAdminService.getSolrIndexPath(activeCollection));

        collectionService.save(activeCollection);
        return OperationState.OPERATION_SUCCESS;
    }

    public OperationState reindexCollection(
            Collection activeCollection,
            User currentAccount,
            SolrSearcher documentSearchService) {
        try {

            boolean cleanedSolrIndex = false;

            if (activeCollection.getNode().getLocal() && solrAdminService.collectionExists(activeCollection)) {
                cleanedSolrIndex = solrAdminService.deleteAllDocuments(activeCollection);
                LOGGER.info(String.format("reIndex collection: delete all documents %s:%s by %s", activeCollection.getName(), activeCollection.getIndexPath(), currentAccount.getLogin()));
            }

//            if (cleanedSolrIndex) {
//                List<FileObject> fileEntityList = fileEntityService.getAllFilesInCollection(activeCollection);
//                termVectorEntityService.deleteTermVectorOfCollection(activeCollection);
//
//                fileEntityList.stream().parallel().forEach(c -> {
//                    HttpSolrClient solr = new HttpSolrClient.Builder(c.getCollection().getIndexPath()).build();
//                    ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
//                    ContentStreamBase cs = new ContentStreamBase.FileStream(new File(c.getFilename()));
//                    req.addContentStream(cs);
//                    req.setParam("literal.id", c.getId().toString());
//                    req.setParam("literal.permission", "PERMISSION ALLES ERLAUBT");
//                    req.setParam("literal.original_name", c.getName());
//                    req.setParam("literal.upload_date", c.getCreated().toString());
//                    req.setParam("literal.storage_location", c.getFilename());
//                    req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
//                    solr.setUseMultiPartPost(true);
//                    try {
//                        solr.request(req);
//
//                        //*** update termvector ***
//                        FileObject fileEntity = fileEntityService.getFileEntity(c.getId());
//                        Document d = documentSearchService.getDocumentById(c.getId(), activeCollection.getId());
//
//                        fileEntityService.save(fileEntity);
//                        String tvJsonString = solrTermVectorSearch.getTermVector(d);
//                        TermVectorParser tvParser = new TermVectorParser();
//                        fileEntityService.saveTermVectors(tvParser.parseTermVectorJson(tvJsonString, fileEntity.getId()));
//
//                        try {
//                            String x = solrSearcher.getTermPositions(d, activeCollection.getIndexPath());
//                            TermVectorParser termVectorParser = new TermVectorParser();
//                            List<StemmedWordOrigin> wordOrigins = termVectorParser.parseTermVectorXmlToWordOrigins(d, x);
//                            termVectorEntityService.saveUnstemmedWordsOfDocument(wordOrigins, d.getId());
//                        } catch (Exception unstemmedWordException) {
//                            LOGGER.error(
//                                    "Error of getting unstemmed words for " + d.getOriginalName(),
//                                    unstemmedWordException);
//                        }
//
//                    } catch (Exception e) {
//                        LOGGER.error(e.getMessage());
//                    }
//                });
//            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

        }
        return OperationState.OPERATION_SUCCESS;
    }

    public Collection updateCollection(Collection activeCollection, User currentAccount) {
        activeCollection=collectionService.save(activeCollection);
        return activeCollection;
    }

    private void log(String message, Collection activeCollection, User currentAccount) {
        LOGGER.warn(
                String.format(message, activeCollection.getName(), currentAccount.getLogin())
        );
    }

}
