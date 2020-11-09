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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.material.RemoteMaterial;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.DocumentStatistic;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchWebResponse {

    private String statusCode = "200";
    private DocumentStatistic documentStatistic = new DocumentStatistic();
    List<Document> remoteDocuments = new ArrayList<>();
    List<RemoteItem> remoteItem = new ArrayList<>();
    List<RemoteMaterial> remoteMaterials = new ArrayList<>();
    List<Experiment> remoteExperiments = new ArrayList<>();

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public DocumentStatistic getDocumentStatistic() {
        return documentStatistic;
    }

    public void setDocumentStatistic(DocumentStatistic documentStatistic) {
        this.documentStatistic = documentStatistic;
    }

    public List<Document> getRemoteDocuments() {
        return remoteDocuments;
    }

    public void setRemoteDocuments(List<Document> remoteDocuments) {
        this.remoteDocuments = remoteDocuments;
    }

    public List<RemoteItem> getRemoteItem() {
        return remoteItem;
    }

    public void setRemoteItem(List<RemoteItem> remoteItem) {
        this.remoteItem = remoteItem;
    }

    public List<RemoteMaterial> getRemoteMaterials() {
        return remoteMaterials;
    }

    public void setRemoteMaterials(List<RemoteMaterial> remoteMaterials) {
        this.remoteMaterials = remoteMaterials;
    }

    public List<Experiment> getRemoteExperiments() {
        return remoteExperiments;
    }

    public void setRemoteExperiments(List<Experiment> remoteExperiments) {
        this.remoteExperiments = remoteExperiments;
    }

    public List<Searchable> getAllFoundObjects() {
        List<Searchable> foundDocs = new ArrayList<>();
        foundDocs.addAll(getRemoteDocuments());
        foundDocs.addAll(getRemoteMaterials());
        foundDocs.addAll(getRemoteItem());
        return foundDocs;
    }

    public void addFoundObject(Searchable searchable) {
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.ITEM) {
            getRemoteItem().add((RemoteItem) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.MATERIAL) {
            getRemoteMaterials().add((RemoteMaterial) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
            getRemoteDocuments().add((Document) searchable);
        }
        if (searchable.getTypeToDisplay().getGeneralType() == SearchTarget.EXPERIMENT) {
            getRemoteExperiments().add((Experiment) searchable);
        }
    }

}
