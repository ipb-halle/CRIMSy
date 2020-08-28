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
package de.ipb_halle.lbac.file.mock;

import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.search.SolrSearcher;
import java.io.IOException;
import java.util.UUID;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author fmauz
 */
public class SolrSearcherMock extends SolrSearcher {

    @Override
    public Document getDocumentById(Integer documentId, Integer collectionId) throws SolrServerException, IOException {
        Document d = new Document();
        d.setLanguage("en");
        return d;
    }

    @Override
    public String getTermPositions(Document d, String collectionUri) {
        return "";
    }

}
