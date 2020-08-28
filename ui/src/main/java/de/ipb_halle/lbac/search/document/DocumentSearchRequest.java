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
package de.ipb_halle.lbac.search.document;

/**
 * SearchRequest for communication with a remote node This class will combine
 * all information to perform a query in the backend. The idea is to set up an
 * instance of SearchRequest in the ui and to transmit it in xml serialized form
 * to the backend.
 */
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.message.LocalUUIDConverter;
import de.ipb_halle.lbac.webservice.WebRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.*;
import org.apache.johnzon.mapper.JohnzonConverter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentSearchRequest extends WebRequest implements Cloneable, Serializable {

    private final static long serialVersionUID = 1L;
    private int totalDocsInCollection = 0;

    /**
     * collection to which this request addresses or which provided the result
     * data
     */
    private Integer collectionId;

    /**
     * return limit elements at maximum
     */
    private long limit = Long.MAX_VALUE;

    /**
     * node to which this request is addressed or from which the result was
     * submitted
     */
    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID nodeId;

    /**
     * return resources and start with element number offset as the first
     * element
     */
    private long offset = 0;

    private long sumOfWordsOfNode = 0;

    /**
     * the list of results.
     */
    @XmlElements(
            @XmlElement(name = "doc", type = Document.class))
    private ArrayList<Document> resultList = new ArrayList<>();

    /**
     * The query for resources (documents in a data set)
     */
    private DocumentSearchQuery searchQuery;

    /**
     * total number of results
     */
    private long totalResultCount = 0;

    public DocumentSearchRequest() {
        // readResolve obviously not called
        this.resultList = new ArrayList<>();
    }

    public void add(Document d) {
        this.resultList.add(d);
    }

    public void addAll(List<Document> doclist) {
        this.resultList.addAll(doclist);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DocumentSearchRequest req = (DocumentSearchRequest) super.clone();

        assert resultList != null : "resultList must not be null";
        req.addAll(getResultList());

        assert collectionId != null : "collectionId must not be null";
        req.setCollectionId(this.collectionId);

        assert nodeId != null : "nodeId must not be null";
        req.setNodeId(this.nodeId);

        assert searchQuery != null : "searchQuery must not be null";
        assert searchQuery.getQuery() != null : "searchQueryString must not be null";

        req.setSearchQuery(new DocumentSearchQuery(searchQuery.getQuery()));

        req.setLimit(this.limit);
        req.setOffset(this.offset);
        req.setTotalResultCount(this.totalResultCount);
        return req;
    }

    public Integer getCollectionId() {
        return this.collectionId;
    }

    public long getLimit() {
        return this.limit;
    }

    public UUID getNodeId() {
        return this.nodeId;
    }

    public long getOffset() {
        return this.offset;
    }

    public List<Document> getResultList() {
        return this.resultList;
    }

    public DocumentSearchQuery getSearchQuery() {
        return this.searchQuery;
    }

    public long getTotalResultCount() {
        return this.totalResultCount;
    }

    public void setCollectionId(Integer id) {
        this.collectionId = id;
    }

    public void setLimit(long l) {
        this.limit = l;
    }

    public void setNodeId(UUID id) {
        this.nodeId = id;
    }

    public void setOffset(long l) {
        this.offset = l;
    }

    public void setSearchQuery(DocumentSearchQuery q) {
        this.searchQuery = q;
    }

    public void setTotalResultCount(long l) {
        this.totalResultCount = l;
    }

    public int getTotalDocsInCollection() {
        return totalDocsInCollection;
    }

    public void setTotalDocsInCollection(int totalDocsInCollection) {
        this.totalDocsInCollection = totalDocsInCollection;
    }

    public long getSumOfWordsOfNode() {
        return sumOfWordsOfNode;
    }

    public void setSumOfWordsOfNode(long sumOfWordsOfNode) {
        this.sumOfWordsOfNode = sumOfWordsOfNode;
    }

}
