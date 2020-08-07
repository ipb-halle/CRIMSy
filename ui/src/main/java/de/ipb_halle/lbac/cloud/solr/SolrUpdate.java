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
package de.ipb_halle.lbac.cloud.solr;

/**
 * SolrUpdate Update the local Solr server, i.e. upload new documents.
 */
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.service.FileService;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;

public class SolrUpdate {

    private Logger logger;
    private FileService fs;

    public SolrUpdate() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.fs = new FileService();
    }

    /**
     * Perform an extracting Solr update request for a given document.
     *
     * @param doc the document to be indexed
     * @param params a map with parameters
     */
    public void update(
            Document doc,
            Map<String, String> params) throws Exception {

        this.logger.info(String.format("update(): dump\n"
                + "  canonical path=%s\n"
                + "  collection.name=%s\n"
                + "  collection.indexPath=%s\n"
                + "  params.literal.id=%s\n"
                + "  params.literal.permission=%s\n"
                + "  params.literal.original_name=%s\n",
                doc.getPath(),
                doc.getCollection().getName(),
                doc.getCollection().getIndexPath(),
                params.get("literal.id"),
                params.get("literal.permission"),
                params.get("literal.original_name")));

        HttpSolrClient solr = new HttpSolrClient.Builder(doc.getCollection().getIndexPath()).build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");

        ContentStreamBase cs = new ContentStreamBase.FileStream(
                new File(doc.getPath()));

        req.addContentStream(cs);

        this.logger.info(doc.getPath());

        Iterator<String> iter = params.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            req.setParam(key, params.get(key));
        }

        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);

        solr.setUseMultiPartPost(true);
        NamedList<Object> nl = solr.request(req);
        dumpResults(nl);

    }

    /**
     * dump the content of the NamedList to the log
     *
     * @param nl the named list
     */
    private void dumpResults(NamedList<Object> nl) {
        Iterator<Map.Entry<String, Object>> iter = nl.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> me = iter.next();
            this.logger.info(String.format("dumpResults() %s --> %s",
                    me.getKey(),
                    me.getValue().toString()));
        }
    }

}
