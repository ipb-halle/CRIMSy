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

import de.ipb_halle.lbac.exp.RemoteExperiment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.material.RemoteMaterial;
import de.ipb_halle.lbac.search.document.Document;
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
    List<Document> remoteDocuments = new ArrayList<>();
    List<RemoteItem> remoteItem = new ArrayList<>();
    List<RemoteMaterial> remoteMaterials = new ArrayList<>();
    List<RemoteExperiment> remoteExperiments = new ArrayList<>();
    private Integer totalDocsInNode;
    private Float averageWordLength;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
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

    public List<RemoteExperiment> getRemoteExperiments() {
        return remoteExperiments;
    }

    public void setRemoteExperiments(List<RemoteExperiment> remoteExperiments) {
        this.remoteExperiments = remoteExperiments;
    }

    public List<Searchable> getAllFoundObjects() {
        List<Searchable> foundSearchables = new ArrayList<>();
        foundSearchables.addAll(getRemoteDocuments());
        foundSearchables.addAll(getRemoteMaterials());
        foundSearchables.addAll(getRemoteItem());
        foundSearchables.addAll(getRemoteExperiments());
        return foundSearchables;
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
            getRemoteExperiments().add((RemoteExperiment) searchable);
        }
    }

    public Integer getTotalDocsInNode() {
        return totalDocsInNode;
    }

    public void setTotalDocsInNode(Integer totalDocsInNode) {
        this.totalDocsInNode = totalDocsInNode;
    }

    public Float getAverageWordLength() {
        return averageWordLength;
    }

    public void setAverageWordLength(Float averageWordLength) {
        this.averageWordLength = averageWordLength;
    }

}
