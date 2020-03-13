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
package de.ipb_halle.lbac.collections;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.ACListService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Controller for view templates/collectionManagement.xhtml This bean displays
 * the list of collections and manages local collections the usual CRUD (create,
 * read, update, delete) way. Additionally, it supports creating (also
 * re-creating aka reindexing), cleaning, deleting the SOLR indexes and managing
 * the underlying filesystem storage.
 *
 * Some of these actions will require asynchronous processing. To prevent
 * interferenc by other threads (or remote accessses) during such asynchronous
 * processing, some kind of locking is necessary for the collection objects.
 *
 * This bean also computes also, wether the currently active user is permitted
 * to upload documents into local collections. Generally, uploading is only
 * supported for local collections.
 *
 * NOTE: Contrary to earlier behaviour, it is being considered to look up remote
 * collections at runtime.
 *
 * lbac-api: inject collection-, node-, fs- and solr-services
 *
 * @author hteuscher, fbroda
 */
@SessionScoped
@Named("collectionBean")
public class CollectionBean implements Serializable {

    private static final String PUBLIC_COLLECTION_NAME = "public";
    private final static long serialVersionUID = 1L;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private final static String COLLECTIONS_PERM_KEY = "COLLECTIONS_MGR_ENABLE";

    private final int POLLING_INTERVALL_ACTIVE = 1000;
    private final int POLLING_INTERVALL_INACTIVE = 1000 * 60 * 60;

    private boolean showLocalCollectionsOnly = false;
    private Collection activeCollection;
    private User currentAccount;
    private MODE mode;
    private CollectionOperation collectionOperation;
    private CollectionPermissionAnalyser collPermAnalyser;

    private int shownCollections = -1;

    @Inject
    private DocumentSearchBean documentSearchBean;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private enum MODE {
        CREATE, //Creates a new collection
        READ, // Default mode 
        UPDATE, // Changes the description of a collection
        DELETE, // Deletes the complete collection
        REINDEX, // reindex all documents in collection in solr
        CLEAR // removes all documents from collection
    };

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    @Inject
    private SolrAdminService solrAdminService;

    @Inject
    private FileService fileService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;
    
    @Inject
    private SolrTermVectorSearch solrTermVectorSearch;

    private CollectionSearchState collectionSearchState = new CollectionSearchState();

    @Inject
    private CollectionOrchestrator collectionOrchestrator;

    @Inject
    private ACListService acListService;

    @Inject
    private SolrSearcher solrSearcher;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @PostConstruct
    public void initCollectionBean() {
        this.mode = MODE.READ;

        collectionOperation = new CollectionOperation(
                fileService,
                fileEntityService,
                globalAdmissionContext,
                solrAdminService,
                nodeService,
                collectionService,
                PUBLIC_COLLECTION_NAME,
                solrTermVectorSearch,
                termVectorEntityService,
                solrSearcher);

        collPermAnalyser = new CollectionPermissionAnalyser(
                PUBLIC_COLLECTION_NAME,
                acListService
        );

        shownCollections = -1;
        initCollection();
    }

    public void refreshCollectionSearch() {
        if (collectionSearchState.getCollections().size() != shownCollections
                && shownCollections != -1) {

            shownCollections = collectionSearchState.getCollections().size();
        } else {
            collectionOrchestrator.startCollectionSearch(
                    collectionSearchState,
                    currentAccount);
            shownCollections = collectionSearchState.getCollections().size();
        }

    }

    public void logCollectionState(ActionEvent event) {
        logger.info(("Amount of found collections " + collectionSearchState.getCollections().size()));
    }

    /**
     * Triggers the clearing of a collection. All Documents in the solR core
     * will be deleted and the files in the filesystem will be removed. Needs
     * the permission to DELETE or the current user must be the owner of the
     * collection.
     */
    public void actionClear() {
        boolean isOwner = currentAccount.equals(activeCollection.getOwner());
        if (isOwner || acListService.isPermitted(ACPermission.permDELETE, activeCollection, currentAccount)) {
            collectionOperation.clearCollection(activeCollection, currentAccount);
            fileService.createDir(activeCollection.getName());
            documentSearchBean.clearBean();
            refreshCollectionList();
        } else {
            UIMessage.info(MESSAGE_BUNDLE, "collMgr_clear_no_permission");
        }
    }

    /**
     * Creates a new collection. Creates a local file folder and a new core in
     * solr. Needs the permission to CREATE. ToDo: How is the permission of
     * CREATE granted to users
     *
     */
    public void actionCreate() {

        collectionOperation.createCollection(activeCollection, currentAccount);
        collectionSearchState.getCollections().add(activeCollection);

    }

    /**
     * Triggers the delete of a complete collection. The solR core with all
     * documents will be deleted as well as all files on the filesystem. If the
     * deletion fails there will be a popUp message to inform the user.
     */
    public void actionDelete() {
        if (collPermAnalyser.isDeleteAllowed(activeCollection, currentAccount)) {
            CollectionOperation.OperationState state = collectionOperation.deleteCollection(activeCollection, currentAccount);
            documentSearchBean.clearBean();
            if (state == CollectionOperation.OperationState.OPERATION_SUCCESS) {

                collectionSearchState.getCollections().remove(activeCollection);
            } else if (state == CollectionOperation.OperationState.DELETE_FORBIDDEN) {
                UIMessage.info(MESSAGE_BUNDLE, "collMgr_delete_no_permission_public");
            }
        } else {
            logger.error("User " + currentAccount.getLogin() + " not allowed to delete collection " + activeCollection.getName());
            UIMessage.info(MESSAGE_BUNDLE, "collMgr_delete_no_permission");
        }
    }

    /**
     * Triggers the reindexing of all documents in the solR core of the
     * collection. Needs the permission to EDIT the collection or the current
     * user must be the owner.
     */
    public void actionReindex() {
        boolean isOwner = currentAccount.equals(activeCollection.getOwner());
        if (isOwner || acListService.isPermitted(ACPermission.permEDIT, activeCollection, currentAccount)) {
            collectionOperation.reindexCollection(activeCollection, currentAccount, solrSearcher);
        } else {
            UIMessage.info(MESSAGE_BUNDLE, "collMgr_reindex_no_permission");
        }
    }

    /**
     * Triggers the edit of the description of a collection. Needs the
     * permission to EDIT the collection or the current user must be the owner.
     */
    public void actionUpdate() {
        boolean isOwner = currentAccount.equals(activeCollection.getOwner());
        if (isOwner || acListService.isPermitted(ACPermission.permEDIT, activeCollection, currentAccount)) {
            collectionOperation.updateCollection(activeCollection, currentAccount);
        } else {
            UIMessage.info(MESSAGE_BUNDLE, "collMgr_update_no_permission");
        }
    }

    public Collection getActiveCollection() {
        return activeCollection;
    }

    /**
     * Gets the collections which are to be shown in the view. If the variable
     * showLocalCollectionsOnly is true, the raw list of collections is filtered
     * so that only local collections are shown.
     *
     * @return collections to show
     */
    public List<Collection> getLocalCollectionList() {
        List<Collection> collsToShow = new ArrayList<>();
        if (!showLocalCollectionsOnly) {
            return collectionSearchState.getCollections();
        } else {
            for (Collection c : collectionSearchState.getCollections()) {
                if (c.getNode().getId().equals(nodeService.getLocalNodeId())) {
                    collsToShow.add(c);
                }
            }
            return collsToShow;
        }
    }

    public List<Collection> getOnlyLocalCollections() {
        List<Collection> collsToShow = new ArrayList<>();
        for (Collection c : collectionSearchState.getCollections()) {
            if (c.getNode().getId().equals(nodeService.getLocalNodeId())) {
                collsToShow.add(c);
            }
        }
        return collsToShow;
    }

    public String getMode() {
        return this.mode.toString();
    }

    /**
     * set defined values for input buffer in view
     */
    private void initCollection() {
        this.activeCollection = new Collection();
        activeCollection.setStoragePath("");
        activeCollection.setName("");
        activeCollection.setIndexPath("");
        activeCollection.setDescription("");
        activeCollection.setNode(nodeService.getLocalNode());
        activeCollection.setCountDocs(0L);
        activeCollection.setACList(new ACList());
    }

    public boolean isShowLocalCollectionsOnly() {
        return showLocalCollectionsOnly;
    }

    public void setActiveCollection(Collection collection) {
        this.activeCollection = collection;
    }

    /**
     * set the current account that determines, which collections are visible,
     * can be uploaded to etc.
     *
     * @param evt the LoginEvent scheduled by UserBean
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        this.currentAccount = evt.getCurrentAccount();
        this.collectionOrchestrator.startCollectionSearch(this.collectionSearchState, this.currentAccount);
        initCollectionBean();
    }

    public String getEditMode() {
        return this.mode.toString();
    }

    public void setEditMode(String editModeString) {
        this.mode = MODE.valueOf(editModeString);
        if (this.mode == null) {
            this.mode = MODE.READ;
        }
    }

    /**
     * Returns the modal title depending on the choosen mode of alterating a
     * collection
     *
     * @return
     */
    public String getModalDialogTitle() {
        switch (this.mode) {
            case CREATE:
                return Messages.getString(MESSAGE_BUNDLE, "collMgr_mode_createCollection", null);
            case DELETE:
                return Messages.getString(MESSAGE_BUNDLE, "collMgr_mode_deleteCollection", null);
            case UPDATE:

                return Messages.getString(MESSAGE_BUNDLE, "collMgr_mode_updateCollection", null);
            case REINDEX:
                return Messages.getString(MESSAGE_BUNDLE, "collMgr_mode_reindexCollection", null);
            case CLEAR:
                return Messages.getString(MESSAGE_BUNDLE, "collMgr_mode_clearCollection", null);
        }
        return "No mode found";
    }

    /**
     * Checks if the creation button should be rendered. Only the public Account
     * is unable to create new collections.
     *
     * @return
     */
    public boolean isCollectionCreationAllowed() {
        return !currentAccount.isPublicAccount();
    }

    /**
     * Checks if the refresh button should be rendered. Only the public Account
     * is unable to refresh the collections
     *
     * @return
     */
    public boolean isCollectionRefreshAllowed() {
        return !currentAccount.isPublicAccount();
    }

    /**
     * Puts the found collections from remote nodes into the datatable
     */
    public void refreshCollectionList() {
        collectionOrchestrator.startCollectionSearch(
                collectionSearchState,
                currentAccount);
    }

    public static String getMessageBundle() {
        return CollectionBean.MESSAGE_BUNDLE;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public static String getCollectionsPermKey() {
        return COLLECTIONS_PERM_KEY;
    }

    public boolean isEditAllowed(Collection col) {
        return collPermAnalyser.isEditAllowed(col, currentAccount);
    }

    public boolean isDeleteAllowed(Collection col) {
        return collPermAnalyser.isDeleteAllowed(col, currentAccount);
    }

    public boolean isClearAllowed(Collection col) {
        return collPermAnalyser.isClearAllowed(col, currentAccount);
    }

    public boolean isReindexingAllowed(Collection col) {
        return collPermAnalyser.isReindexingAllowed(col, currentAccount);
    }

    public boolean isPermissionEditAllowed(Collection col) {
        return collPermAnalyser.isPermissionEditAllowed(col, currentAccount);
    }

    public User getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(User currentAccount) {
        this.currentAccount = currentAccount;
    }

    public CollectionSearchState getCollectionSearchState() {
        return collectionSearchState;
    }

    public void setCollectionSearchState(CollectionSearchState collectionSearchState) {
        this.collectionSearchState = collectionSearchState;
    }

    public CollectionOrchestrator getCollectionOrchestrator() {
        return collectionOrchestrator;
    }

    public void setCollectionOrchestrator(CollectionOrchestrator collectionOrchestrator) {
        this.collectionOrchestrator = collectionOrchestrator;
    }

    public void setShowLocalCollectionsOnly(boolean showLocalCollectionsOnly) {
        this.showLocalCollectionsOnly = showLocalCollectionsOnly;
    }

    public int getNewDocumentsToShow() {
        if (shownCollections < 0) {
            return 0;
        } else {
            return collectionSearchState.getCollections().size() - shownCollections;
        }
    }

    public String getToolTipForRefresh() {
        if (getNewDocumentsToShow() == 0) {
            return Messages.getString(MESSAGE_BUNDLE, "collMgr_tooltip_refreshButton_triggerSearch", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "collMgr_tooltip_refreshButton_showResults", null) + getNewDocumentsToShow();
        }
    }

    public int getPollIntervall() {
        if (!collectionSearchState.getUnfinishedNodeRequests().isEmpty()) {
            return POLLING_INTERVALL_ACTIVE;
        } else {
            return POLLING_INTERVALL_INACTIVE;
        }
    }

}
