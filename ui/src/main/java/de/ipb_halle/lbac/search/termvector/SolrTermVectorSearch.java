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
package de.ipb_halle.lbac.search.termvector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Document;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@Stateless
public class SolrTermVectorSearch {
    
    private static final String TERM_VECTORS = "termVectors";
    
    private Logger logger;
    
    public SolrTermVectorSearch() {
        logger = LogManager.getLogger(SolrTermVectorSearch.class);
    }

    /**
     * wrapper for doGetTermVector see below
     *
     * @param doc - document
     * @return - termvector in json
     * @throws IOException
     */
    public String getTermVector(Document doc) throws IOException {
        try {
            return doGetTermVector(
                    doc.getCollection().getIndexPath(),
                    doc.getId().toString(),
                    getFullTextField(doc));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    /**
     * wrapper for doGetTermVector see below
     *
     * @param c - collection
     * @param id - document id
     * @param targetField
     * @return - termvector in json
     */
    public String getTermVector(
            Collection c,
            String id,
            String targetField) {
        try {
            return doGetTermVector(c.getIndexPath(), id, targetField);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    /**
     *
     * @param indexPath - url collection
     * @param id - document id
     * @return termvector in json format
     * @throws Exception
     */
    private String doGetTermVector(String indexPath, String id, String targetField) throws Exception {
        Client client = ClientBuilder.newClient();
        String termVectorResponse = client.target(indexPath)
                .path("tvrh")
                .queryParam("q", "id:" + id)
                .queryParam("fl", "id")
                .queryParam("tv.tf", true)
                .queryParam("tv.fl", targetField)
                .queryParam("wt", "json")
                .queryParam("json.nl", "map")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        //*** select subTree "termVectors"->"<docId>"->"_general_" ***
        ObjectMapper mapper = new ObjectMapper();
        JsonNode tv = mapper.readTree(termVectorResponse).get(TERM_VECTORS).get(id).get(targetField);
        return mapper.writeValueAsString(tv);
    }
    
    public String getFullTextField(Document d) {
        return "text_" + d.getLanguage();
    }
}
