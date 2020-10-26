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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.webservice.WebRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WordCloudWebRequest extends WebRequest {

    private List<Document> documentsWithTerms = new ArrayList<>();
    private Set<String> terms = new HashSet<>();
    private Set<Integer> idsOfReadableCollections = new HashSet<>();
    private Integer maxTerms = Integer.MAX_VALUE;

    public List<Document> getDocumentsWithTerms() {
        return documentsWithTerms;
    }

    public void setDocumentsWithTerms(List<Document> documentsWithTerms) {
        this.documentsWithTerms = documentsWithTerms;
    }

    public Set<String> getTerms() {
        return terms;
    }

    public void setTerms(Set<String> terms) {
        this.terms = terms;
    }

    public Set<Integer> getIdsOfReadableCollections() {
        return idsOfReadableCollections;
    }

    public void setIdsOfReadableCollections(Set<Integer> idsOfReadableCollections) {
        this.idsOfReadableCollections = idsOfReadableCollections;
    }

    public Integer getMaxTerms() {
        return maxTerms;
    }

    public void setMaxTerms(Integer maxTerms) {
        this.maxTerms = maxTerms;
    }

}
