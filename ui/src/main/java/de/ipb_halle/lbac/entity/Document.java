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
package de.ipb_halle.lbac.entity;

/**
 * This class stores information about a document.
 */
import de.ipb_halle.lbac.message.LocalUUIDConverter;
import org.apache.johnzon.mapper.JohnzonConverter;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;

public class Document implements Serializable, Comparable<Document> {

    private final static long serialVersionUID = 1L;
    private final static String UTF8 = "UTF-8";
    private final static String UI_PATH = "/ui";
    private Logger LOGGER = Logger.getLogger(Document.class);

    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID id;
    private transient Node node;

    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID nodeId;
    private transient Collection collection;

    @JohnzonConverter(LocalUUIDConverter.class)
    private UUID collectionId;
    private String contentType;
    private String path;
    private Long size;
    private String originalName;
    private double relevance = 0;
    private TermFrequencyList termFreqList = new TermFrequencyList();
    private Integer wordCount = 0;
    private String language;
    private Set<String> normalizedSearchTerms = new HashSet<>();

    /**
     * default constructor
     */
    public Document() {
    }

    //*** getter & setter ***
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Collection getCollection() {
        return this.collection;
    }

    public UUID getCollectionId() {
        return this.collectionId;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Node getNode() {
        return this.node;
    }

    public UUID getNodeId() {
        return this.nodeId;
    }

    public String getPath() {
        return this.path;
    }

    public Long getSize() {
        return this.size;
    }

    public String getLink() throws UnsupportedEncodingException {
        return getLink(UI_PATH);
    }

    /**
     * Creates the REST API link to fetch the document from a remote server
     *
     * @param appPath location of the remote note
     * @return Rest api link
     * @throws UnsupportedEncodingException
     */
    public String getLink(String appPath) throws UnsupportedEncodingException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(appPath);
            sb.append("/servlet/document/GET");
            sb.append("?nodeId=");
            sb.append(URLEncoder.encode(this.nodeId.toString(), UTF8));
            sb.append("&collectionId=");
            sb.append(URLEncoder.encode(this.collectionId.toString(), UTF8));
            sb.append("&contentType=");
            sb.append(URLEncoder.encode(this.contentType, UTF8));
            sb.append("&originalName=");
            sb.append(URLEncoder.encode(this.originalName, UTF8));
            sb.append("&path=");
            sb.append(URLEncoder.encode(this.path, UTF8));
            return sb.toString();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Auflösen der URL", e);
        }
        return "n.a.";
    }

    public void setCollection(Collection c) {
        this.collection = c;
    }

    public void setCollectionId(UUID id) {
        this.collectionId = id;
    }

    public void setContentType(String st) {
        this.contentType = st;
    }

    public void setNode(Node n) {
        this.node = n;
    }

    public void setNodeId(UUID id) {
        this.nodeId = id;
    }

    public void setPath(String st) {
        this.path = st;
    }

    public void setSize(Long s) {
        this.size = s;
    }

    public double getRelevance() {
        return relevance;
    }

    public String getFormatedRelevance() {
        if (relevance > 0) {
            return String.format("%.3f", relevance);
        } else {
            return "n.A.";
        }
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public String getRichSourceInfo() {
        return collection.getName() + "@" + node.getInstitution();
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public void debugInfo() {
        LOGGER.info("-----");
        LOGGER.info("Infos of doc " + getOriginalName());
        LOGGER.info("Total words " + getWordCount());
        LOGGER.info("Language " + language);
        for (TermFrequency s : termFreqList.getTermFreq()) {
            LOGGER.info(s.getTerm() + " - > " + s.getFrequency());
        }
    }

    public TermFrequencyList getTermFreqList() {
        return termFreqList;
    }

    public void setTermFreqList(TermFrequencyList termFreqList) {
        this.termFreqList = termFreqList;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<String> getNormalizedSearchTerms() {
        return normalizedSearchTerms;
    }

    public void setNormalizedSearchTerms(Set<String> normalizedSearchTerms) {
        this.normalizedSearchTerms = normalizedSearchTerms;
    }

    @Override
    public int compareTo(Document o) {
        if (relevance <= o.getRelevance()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.node);
        hash = 37 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Document other = (Document) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.node, other.node)) {
            return false;
        }
        return true;
    }

}
