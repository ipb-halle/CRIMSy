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

import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.ACListService;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class CollectionPermissionAnalyser implements Serializable {

    private final String PUBLIC_COLLECTION_NAME;
    private final ACListService acListService;
    private final Logger LOGGER = Logger.getLogger(CollectionPermissionAnalyser.class);

    public CollectionPermissionAnalyser(String publicColName, ACListService acListService) {
        this.PUBLIC_COLLECTION_NAME = publicColName;
        this.acListService = acListService;
    }

    /**
     * Edit is only allowed if the user got the permission right, the node is a
     * local node and the collection is not the public collection.The owner of a
     * collection has always the right of reindexing.
     *
     * @param col
     * @param currentAccount
     * @return
     */
    public boolean isEditAllowed(Collection col, User currentAccount) {
        boolean permissionEdit = isOperationOnCollectionAllowed(
                col,
                ACPermission.permEDIT.toString(),
                currentAccount);
        return permissionEdit
                && col.getNode().getLocal()
                && !col.getName().equals(PUBLIC_COLLECTION_NAME);
    }

    /**
     * Clear is only allowed if the user got the permission right and the node
     * is a local node .The owner of a collection has always the right of
     * clearing.
     *
     * @param col
     * @param currentAccount
     * @return
     */
    public boolean isClearAllowed(Collection col, User currentAccount) {
        boolean permissionEdit = isOperationOnCollectionAllowed(
                col,
                ACPermission.permDELETE.toString(),
                currentAccount);
        return permissionEdit
                && col.getNode().getLocal();

    }

    /**
     * Deleting the whole collection is only on local collections allowed . The
     * public collection must not be deleted. The owner of a collection has
     * always the right of reindexing.
     *
     * @param col
     * @param currentAccount
     * @return
     */
    public boolean isDeleteAllowed(Collection col, User currentAccount) {
        boolean permissionAllowed = isOperationOnCollectionAllowed(
                col,
                ACPermission.permDELETE.toString(),
                currentAccount);

        return permissionAllowed
                && col.getNode().getLocal()
                && !col.getName().equals(PUBLIC_COLLECTION_NAME);

    }

    /**
     * Reindexing is only allowed on local nodes.The owner of a collection has
     * always the right of reindexing.
     *
     * @param col
     * @param currentAccount
     * @return
     */
    public boolean isReindexingAllowed(Collection col, User currentAccount) {
        boolean permissionReindex = isOperationOnCollectionAllowed(
                col,
                ACPermission.permEDIT.toString(),
                currentAccount);

        return permissionReindex
                && col.getNode().getLocal();

    }

    /**
     * Permission edit is only allowd on local nodes.The owner of a collection
     * has always the right of granting.
     *
     * @param col
     * @param currentAccount
     * @return
     */
    public boolean isPermissionEditAllowed(Collection col, User currentAccount) {
        boolean permissionGranted = isOperationOnCollectionAllowed(
                col,
                ACPermission.permGRANT.toString(),
                currentAccount);

        return permissionGranted
                && col.getNode().getLocal();

    }

    /**
     * Checks, if the requested action on the collection is allowed by the
     * current user.
     *
     * @param col collection to check
     * @param permissionString permission to check for. String represatation of
     * @param currentAccount
     * @see ACPermission
     * @return true if action is allowed
     */
    private boolean isOperationOnCollectionAllowed(
            Collection col,
            String permissionString,
            User currentAccount) {

        boolean isOwner = col.getOwner().equals(currentAccount);

        return isOwner || acListService.isPermitted(
                ACPermission.valueOf(permissionString),
                col,
                currentAccount);

    }
}
