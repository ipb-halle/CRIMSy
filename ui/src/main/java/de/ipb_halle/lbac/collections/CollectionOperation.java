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

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
public class CollectionOperation implements Serializable {

    private final FileService fileService;
    private final FileObjectService fileObjectService;
    private final GlobalAdmissionContext globalAdmissionContext;
    private final Logger LOGGER = LogManager.getLogger(CollectionOperation.class);
    private final NodeService nodeService;
    private final CollectionService collectionService;
    private final TermVectorService termVectorService;
    private final String PUBLIC_COLLECTION_NAME;
    private final String COLL_NOT_UNIQUE = "name %s for a new collection  is not unique. (user: %s)";
    private final String COLL_RESERVED = "name %s for a new collection  is a reserved name. (user: %s)";
    private final String COLL_CREATE = "start creating collection: %s. (user: %s)";
    private final String COLL_DELETE_FORBIDDEN = "deleting collection %s forbidden! unauthorized access detected by user: %s";
    private final String COLL_DELETED = "collection deleted: %s  by %s";
    private final String COLL_START_DELETE = "start collection delete: %s  by %s";

    public CollectionOperation(
            FileService fileService,
            FileObjectService fileObjectService,
            GlobalAdmissionContext globalAdmissionContext,
            NodeService nodeService,
            CollectionService collectionService,
            String publicCollectionName,
            TermVectorService termVectorService) {

        this.fileService = fileService;
        this.fileObjectService = fileObjectService;
        this.globalAdmissionContext = globalAdmissionContext;
        this.nodeService = nodeService;
        this.collectionService = collectionService;
        this.PUBLIC_COLLECTION_NAME = publicCollectionName;
        this.termVectorService = termVectorService;
    }

    public enum OperationState {
        OPERATION_SUCCESS, //Operation was successfull
        CREATION_RESERVED_NAME, // name of collection can not be choosen
        CREATION_DUPLICATE_NAME, // collectionname already exists
        CLEAR_ERROR, // undefined error at clearing a collection
        DELETE_FORBIDDEN

    }

    /**
     * Removes all documents from the instance and deletes all files from the
     * local filesystem. Removes all file entries from database.
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

            if (fileService.storagePathExists(activeCollection)) {
                fileService.deleteDir(activeCollection);
            }

            termVectorService.deleteTermVectorsOfCollection(activeCollection.getId());
            
            fileObjectService.deleteCollectionFiles(activeCollection.getId());

            LOGGER.info(String.format("collection delete all file entities: %s:%s by %s", activeCollection.getName(), activeCollection.getStoragePath(), currentAccount.getLogin()));
            return OperationState.OPERATION_SUCCESS;

        } catch (Exception e) {
            LOGGER.error("clearCollection() caught an exception:", (Throwable) e);
            return OperationState.CLEAR_ERROR;
        }
//        return OperationState.OPERATION_SUCCESS;
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
        if (!fileService.storagePathExists(activeCollection)) {
            fileService.createDir(activeCollection);
        }
        activeCollection.setStoragePath(fileService.getCollectionPath(activeCollection).toString());

        collectionService.save(activeCollection);
        return OperationState.OPERATION_SUCCESS;
    }

    public Collection updateCollection(Collection activeCollection, User currentAccount) {
        activeCollection = collectionService.save(activeCollection);
        return activeCollection;
    }

    private void log(String message, Collection activeCollection, User currentAccount) {
        LOGGER.warn(
                String.format(message, activeCollection.getName(), currentAccount.getLogin())
        );
    }

}
