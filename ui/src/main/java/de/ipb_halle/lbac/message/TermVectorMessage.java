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
package de.ipb_halle.lbac.message;

import de.ipb_halle.lbac.search.document.DocumentSearchRequest;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * message class for rest-api calls
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TermVectorMessage implements Serializable {

    private final static long serialVersionUID = 1L;

    private MessageType          messageType      = MessageType.TERMVECTOR;
    private List<String>         docIds           = new ArrayList<>();
    private Map<String, Integer> termVectorResult = new HashMap<>();
    private Integer              maxResult        = 50;
    private DocumentSearchRequest        searchRequest;

    //*** getter and setter ***


    public DocumentSearchRequest getSearchRequest() {
        return searchRequest;
    }

    public void setSearchRequest(DocumentSearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public List<String> getDocIds() {
        return docIds;
    }

    public void setDocIds(List<String> docIds) {
        this.docIds = docIds;
    }

    public Integer getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(Integer maxResult) {
        this.maxResult = maxResult;
    }

    public Map<String, Integer> getTermVectorResult() {
        return termVectorResult;
    }

    public void setTermVectorResult(Map<String, Integer> termVectorResult) {
        this.termVectorResult = termVectorResult;
    }

    //*** implementation ***
    public TermVectorMessage() {
    }

    /**
     * constructor
     * @param docIds
     * @param maxResult
     * @param searchRequest
     */
    public TermVectorMessage(List<String> docIds, Integer maxResult, DocumentSearchRequest searchRequest) {
        this.docIds = Objects.requireNonNull(docIds);
        this.maxResult = maxResult;
        this.searchRequest = searchRequest;
    }
}
