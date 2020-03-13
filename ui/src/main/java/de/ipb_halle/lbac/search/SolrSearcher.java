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
package de.ipb_halle.lbac.search;

/*
 * SolrSearcher
 * Query the local Solr server
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ipb_halle.lbac.search.document.DocumentSearchRequest;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.NodeService;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

@Stateless
public class SolrSearcher {

    private DocumentSearchRequest request;

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    private Logger logger;

    public SolrSearcher() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * extract a single value from a (multivalued) field from the solr document
     *
     * @param clazz the class of the object which should be returned. If the
     * object specified by field and index is not assignable to clazz, null is
     * returned
     * @param sdoc the SolrDocument to extract the object from
     * @param field the field name of the list containing the requested object
     * @param index the list index of the object in the list
     * @return the requested object or null
     */
    @SuppressWarnings("unchecked")
    private Object getObject(Class clazz, SolrDocument sdoc, String field, int index) {
        if (sdoc.containsKey(field)) {
            List<Object> l = (List<Object>) sdoc.get(field);
            Object o = l.get(index);
            if ((o != null) && (clazz.isAssignableFrom(o.getClass()))) {
                return o;
            }
        }
        return null;
    }

    /**
     * Perform a Solr search on a specific collection. This method clones the
     * request object of this class and performs a SolrQuery against it. Results
     * of this query are added to the clone, which is finally returned.
     *
     * @return a SearchRequest object which (ideally) contains the search
     * results.
     */
    public DocumentSearchRequest search() {

        DocumentSearchRequest result = null;

        Collection c = this.collectionService.loadById(this.request.getCollectionId());

        /* do not perform request on non-existing or 
         * or remote collections */
        if ((c == null)
                || (this.nodeService.isRemoteNode(c.getNode()))) {
            return null;
        }

        try {
            result = (DocumentSearchRequest) this.request.clone();
            SolrClient solr = new HttpSolrClient.Builder(c.getIndexPath()).build();

            SolrQuery query = new SolrQuery();

            query.setQuery(convertQueryToLanguages(this.request.getSearchQuery().getQuery()));
            query.setRows((int) this.request.getLimit());
            QueryResponse response = solr.query(query);
            SolrDocumentList list = response.getResults();

            ListIterator<SolrDocument> li = list.listIterator();

            long i = 0;
            while (li.hasNext() && (i < this.request.getLimit())) {
                SolrDocument sdoc = li.next();
                result.add(convertSolrDocToDoc(sdoc, c));
                i++;
            }

            result.setTotalResultCount(list.getNumFound());
            return result;

        } catch (CloneNotSupportedException e) {
            logger.error("Clone not implemented", e);
        } catch (SolrServerException f) {
            logger.error("Error at Searching", f);
        } catch (IOException g) {
            logger.error("IO Exception", g);
        } catch (Exception h) {
            logger.error("Unexpected Exception", h);
        }

        return result;
    }

    public void setRequest(DocumentSearchRequest req) {
        this.request = req;
    }

    /*
     * URL documents need some documentType {FILE, URL} to be set?
     */
    private String stripStoragePath(Collection c, String path) {
        String storagePath = c.getStoragePath();
        if (path.startsWith(storagePath)) {
            return path.substring(storagePath.length());
        }
        return path;
    }

    /**
     * return the normalized terms of the corresponding language.The
     * normalization is defined by the collection, querry Analyser pipeline of
     * the given language. For example:
     * <ol>
     * <li> Tokenizing </li>
     * <li> Removing to stop words </li>
     * <li> Map to lower case </li>
     * <li> ... </li>
     * </ol>
     *
     *
     * @param terms Terms to normalize
     * @param collectionUri uri to the collection in the solr instance
     * @param language language
     * @return List of normalized words. It is not position preserving and can
     * be empty if every words are filtered.
     */
    public Set<String> getNormalizedSearchTerms(
            List<String> terms,
            String collectionUri,
            String language) {
        Set<String> normalizedTerms = new HashSet<>();
        try {
            String termsAsString = terms.get(0);
            for (int i = 1; i < terms.size(); i++) {
                termsAsString += "+" + terms.get(i);
            }
            String fieldName = "text_" + language;
            String restPoint = collectionUri
                    + "/analysis/field?analysis.fieldname="
                    + fieldName + "&analysis.fieldvalue="
                    + termsAsString
                    + "&wt=json";

            WebClient wc = WebClient.create(restPoint);
            String jsonResponse = wc.get(String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(jsonResponse);
            JsonNode lastAnalyzeStep = null;
            Iterator<JsonNode> iter = actualObj.get("analysis").get("field_names").get(fieldName).get("index").elements();
            while (iter.hasNext()) {
                lastAnalyzeStep = iter.next();
            }

            iter = lastAnalyzeStep.elements();
            while (iter.hasNext()) {
                JsonNode node = iter.next();
                normalizedTerms.add(node.get("text").asText());
            }
            normalizedTerms.addAll(terms);
        } catch (Exception e) {
            logger.error(e);
        }
        return normalizedTerms;
    }

    /**
     * Tries to find a Document with a specific ID in a Collection
     *
     * @param documentId
     * @param collectionId
     * @return null if no Document was found
     * @throws SolrServerException
     * @throws IOException
     */
    public Document getDocumentById(UUID documentId, UUID collectionId) throws Exception {
        Collection c = collectionService.loadById(collectionId);
        HttpSolrClient solr = new HttpSolrClient.Builder(c.getIndexPath()).build();
        SolrQuery query = new SolrQuery();
        query.setFilterQueries("id:" + documentId.toString());
        query.setParam("q", "*:*");

        QueryResponse response = solr.query(query);
        SolrDocumentList list = response.getResults();
        if (list != null && !list.isEmpty()) {
            return convertSolrDocToDoc(list.get(0), c);
        }
        return null;
    }

    /**
     * Converts a Repsonse from the solr client to a lbac document
     *
     * @param sdoc solr response document
     * @param c colletion of the document
     * @return lbac document
     */
    private Document convertSolrDocToDoc(SolrDocument sdoc, Collection c) throws Exception {

        Document doc = new Document();

        String ids = (String) sdoc.get("id");
        UUID uuid = UUID.fromString(ids);
        doc.setId(uuid);
        doc.setCollectionId(c.getId());
        doc.setNodeId(c.getNode().getId());
        doc.setNode(nodeService.loadById(c.getNode().getId()));
        String language = null;
        if (sdoc.containsKey("language_s")) {
            language = (String) sdoc.get("language_s");
        }
        doc.setLanguage(language);
        doc.setCollection(collectionService.loadById(doc.getCollectionId()));
        doc.setPath((String) getObject(String.class, sdoc, "storage_location", 0));
        doc.setContentType((String) getObject(String.class, sdoc, "stream_content_type", 0));
        doc.setSize((Long) getObject(Long.class, sdoc, "stream_size", 0));
        doc.setOriginalName((String) getObject(String.class, sdoc, "original_name", 0));
        return doc;
    }

    /**
     * Wraps the search tokens with the field information from solR in all
     * defined languages. If there is more than one word all words are OR
     * concatinated
     *
     * @param rawQuery search terms
     * @return the solR search string
     */
    public String convertQueryToLanguages(String rawQuery) {

        String[] token = rawQuery.trim().split(" ");
        String finalQuery = "";
        for (SupportedLanguages l : SupportedLanguages.values()) {
            finalQuery = finalQuery + "(";
            for (String t : token) {
                if (!t.toLowerCase().equals("and")) {
                    finalQuery = finalQuery.concat("text_" + l.toString() + ":" + t + " ");
                } else {
                    finalQuery = finalQuery + "AND ";
                }
            }
            finalQuery = finalQuery + ")";

        }
        return finalQuery.trim();
    }

    public String getTermPositions(Document d, String collectionUri) {

        Client client = ClientBuilder.newClient();
        return client.target(collectionUri)
                .path("tvrh")
                .queryParam("q", "id:" + d.getId().toString())
                .queryParam("tv.offsets", true)
                .queryParam("tv.fl", "text_" + d.getLanguage())
                .queryParam("wt", "xml")
                .queryParam("indent", "off")
                .queryParam("json.nl", "map")
                .request(MediaType.TEXT_XML)
                .get(String.class);
    }
}
