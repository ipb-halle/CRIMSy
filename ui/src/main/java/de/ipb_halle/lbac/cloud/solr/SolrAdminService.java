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
package de.ipb_halle.lbac.cloud.solr;

import de.ipb_halle.lbac.entity.Collection;

import java.io.IOException;
import javax.ejb.Stateless;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;

/**
 * api functions to handle solr collections (crud)
 *
 * @author hteuscher
 * @version 1.0
 */
@Stateless
public class SolrAdminService {

    private final static String baseURLSolr = "http://lbacsolr:8983/solr";
    private final static String defaultPath = "/data/solr/";
    private final static String defaultConfigSet = "lbac";

    private Logger logger;

    //*** getter and setter ***
    public static String getBaseURLSolr() {
        return baseURLSolr;
    }

    public static String getDefaultPath() {
        return defaultPath;
    }

    public SolrAdminService() {
        logger = LogManager.getLogger(SolrAdminService.class);
    }

    public String getSolrIndexPath(Collection collection) {
        if (collectionExists(collection)) {
            return baseURLSolr + "/" + collection.getName();
        }
        return "";
    }

    //*** implementation ***
    /**
     * @param collection create a new solr collection based on default configset
     * @return true if ok
     */
    public boolean createCollection(Collection collection) {
        return doCreateCollection(collection.getName(), defaultConfigSet);
    }

    /**
     * @param collection create a new solr collection
     * @param configset alternative config set
     * @return true if ok
     */
    public boolean createCollection(Collection collection, String configset) {
        return doCreateCollection(collection.getName(), configset);
    }

    /**
     * create a new solr collection based on default configset
     *
     * @param collectionName - String name
     * @return true, false
     */
    public boolean createCollection(String collectionName) {
        return doCreateCollection(collectionName, defaultConfigSet);
    }

    /**
     * create a new solr collection
     *
     * @param collectionName String name
     * @param configset - solr configset
     * @return true ok
     */
    public boolean createCollection(String collectionName, String configset) {
        return doCreateCollection(collectionName, configset);
    }

    /**
     * internal create collection
     *
     * @param collectionName - String collectionName
     * @param configSet existing named config set (see
     * /data/solr/configsets/{name}/conf/...)
     * @return true if ok
     */
    private boolean doCreateCollection(String collectionName, String configSet) {
        try {
            HttpSolrClient solr = new HttpSolrClient.Builder(baseURLSolr).build();
            CoreAdminRequest.Create createRequest = new CoreAdminRequest.Create();

            createRequest.setCoreName(collectionName);
            createRequest.setConfigSet(configSet);
            CoreAdminResponse coreAdminResponse = createRequest.process(solr);

            if (coreAdminResponse.getStatus() == 0) {
                logger.info(String.format("createCollection new solr collection %s done.", collectionName));
                return true;
            } else {
                logger.info(String.format("create collection %s failed. Response Status: %s.", collectionName, coreAdminResponse.getStatus()));
                return false;
            }
        } catch (SolrServerException e) {
            logger.info(String.format("SolrServerException creating collection %s", collectionName), e);
            return false;
        } catch (IOException e) {
            logger.info(String.format("IO-Exception creating collection %s", collectionName), e);
            return false;
        }

    }

    /**
     * @param collection delete solr collection
     * @return true if ok
     */
    public boolean unloadCollection(Collection collection) {
        try {
            HttpSolrClient solr = new HttpSolrClient.Builder(baseURLSolr).build();

            CoreAdminRequest.Unload unloadRequest = new CoreAdminRequest.Unload(true);

            unloadRequest.setCoreName(collection.getName());
            unloadRequest.setDeleteDataDir(true);
            unloadRequest.setDeleteIndex(true);
            unloadRequest.setDeleteInstanceDir(true);

            CoreAdminResponse coreAdminResponse = unloadRequest.process(solr);
            logger.info("unload collection response:" + coreAdminResponse.getStatus());
            logger.info(String.format("unloadCollection collection %s done.", collection.getName()));
            return true;
        } catch (SolrServerException e) {
            logger.info(String.format("SolrServerException unload collection %s", collection.getName()), e);
            return false;
        } catch (IOException e) {
            logger.info(String.format("IO-Exception unload collection %s", collection.getName()), e);
            return false;
        }
    }

    /**
     *
     * @param collectionName
     * @return true if exists
     */
    public boolean collectionExists(String collectionName) {
        try {
            HttpSolrClient solr = new HttpSolrClient.Builder(baseURLSolr).build();
            CoreAdminResponse response = CoreAdminRequest.getStatus(collectionName, solr);
            return response.getCoreStatus(collectionName).get("instanceDir") != null;
        } catch (SolrServerException e) {
            logger.info(String.format("SolrServerException getting %s collection status", collectionName), e);
            return false;
        } catch (IOException e) {
            logger.info(String.format("IO-Exception getting %s collection status", collectionName), e);
            return false;
        }
    }

    /**
     * @param collection check, if solr collection exists
     * @return true if exists
     */
    public boolean collectionExists(Collection collection) {
        return collectionExists(collection.getName());
    }

    /**
     * delete all documents in a collection
     *
     * @param collection delete all docs in
     * @return true if ok
     */
    public boolean deleteAllDocuments(Collection collection) {
        HttpSolrClient solr = new HttpSolrClient.Builder(getSolrIndexPath(collection)).build();
        try {
            solr.deleteByQuery("*:*");
            solr.commit();
            return true;
        } catch (SolrServerException e) {
            logger.info(String.format("SolrServerException deleting all docs in collection %s ", collection.getName()), e);
            return false;
        } catch (IOException e) {
            logger.info(String.format("IO-Exception  deleting all docs in collection %s", collection.getName()), e);
            return false;
        }
    }

    /**
     * @param collection count all docs in
     * @return count documents
     */
    public Long countDocuments(Collection collection) {
        HttpSolrClient solr = new HttpSolrClient.Builder(getSolrIndexPath(collection)).build();
        try {
            SolrQuery solrQuery = new SolrQuery("*:*");
            solrQuery.setRows(0);
            return solr.query(solrQuery).getResults().getNumFound();
        } catch (SolrServerException e) {
            logger.info(String.format("SolrServerException count docs in collection %s ", collection.getName()), e);
            return 0L;
        } catch (IOException e) {
            logger.info(String.format("IO-Exception count docs in in collection %s", collection.getName()), e);
            return 0L;
        }
    }

    /**
     * delete a document by id in a collection
     *
     * @param collection
     * @param id
     * @return
     */
    public boolean deleteDocumentbyID(Collection collection, String id) {
        HttpSolrClient solr = new HttpSolrClient.Builder(getBaseURLSolr()).build();
        try {
            solr.deleteById(collection.getName(), id);
            return true;
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
