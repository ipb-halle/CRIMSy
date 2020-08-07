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
package de.ipb_halle.lbac.collections.mock;

import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.entity.Collection;

/**
 *
 * @author fmauz
 */
public class SolarAdminServiceMock extends SolrAdminService {

    private boolean deleteSuccess = true;
    private boolean createSuccess = true;
    private boolean updateSuccess = true;
    private boolean collExists = false;

    @Override
    public boolean collectionExists(String collectionName) {
        return collExists;
    }

    @Override
    public boolean collectionExists(Collection collection) {
        return collExists;
    }

    @Override
    public Long countDocuments(Collection collection) {
        return 0L;
    }

    @Override
    public boolean createCollection(Collection collection) {
        return createSuccess;
    }

    @Override
    public boolean createCollection(Collection collection, String configset) {
        return createSuccess;
    }

    @Override
    public boolean createCollection(String collectionName) {
        return createSuccess;
    }

    @Override
    public boolean createCollection(String collectionName, String configset) {
        return createSuccess;
    }

    @Override
    public boolean deleteAllDocuments(Collection collection) {
        return deleteSuccess;
    }

    @Override
    public boolean deleteDocumentbyID(Collection collection, String id) {
        return deleteSuccess;
    }

    @Override
    public String getSolrIndexPath(Collection collection) {
        return "/";
    }

    @Override
    public boolean unloadCollection(Collection collection) {
        return deleteSuccess;
    }

    public void setDeleteSuccess(boolean deleteSuccess) {
        this.deleteSuccess = deleteSuccess;
    }

    public void setCreateSuccess(boolean createSuccess) {
        this.createSuccess = createSuccess;
    }

    public void setUpdateSuccess(boolean updateSuccess) {
        this.updateSuccess = updateSuccess;
    }

    public void setCollExists(boolean collExists) {
        this.collExists = collExists;
    }

}
