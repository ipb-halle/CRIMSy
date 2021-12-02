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

import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.search.document.DocumentStatistic;
import java.io.Serializable;
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
public class SearchResultImpl implements SearchResult, Serializable {

    private final static long serialVersionUID = 1L;
    private List<String> errorMessages = new ArrayList<>();

    private Node node;
    protected List<Searchable> foundObjectsOfNode = new ArrayList<>();
    private DocumentStatistic documentStatistic;

    public SearchResultImpl() {
        documentStatistic = new DocumentStatistic();
    }

    public SearchResultImpl(Node n) {
        this();
        node = n;

    }

    @Override
    public void addResult(Searchable foundObject) {
        foundObjectsOfNode.add(foundObject);
    }

    @Override
    public void addResults(List<Searchable> foundObjects) {
        foundObjectsOfNode.addAll(foundObjects);
    }

    @Override
    public List<NetObject> getAllFoundObjects() {
        List<NetObject> foundObjects = new ArrayList<>();
        for (Searchable s : foundObjectsOfNode) {
            foundObjects.add(new NetObjectImpl(s, node));
        }
        return foundObjects;
    }

    @Override
    public List<Searchable> getAllFoundObjects(Node n) {
        return foundObjectsOfNode;
    }

    @Override
    public <T> List<NetObject> getAllFoundObjects(Class T) {
        List<NetObject> netObjects = getAllFoundObjects();
        return fetchAllObjectsOf(netObjects, T);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllFoundObjects(Class T, Node n) {
        List<Searchable> netObjects = getAllFoundObjects(n);
        List<T> objectsOfType = new ArrayList<>();
        for (Searchable s : netObjects) {

            if (s.getClass().equals(T)) {
                objectsOfType.add((T) s);
            }
        }
        return objectsOfType;
    }

    private List<NetObject> fetchAllObjectsOf(List<NetObject> rawList, Class T) {
        List<NetObject> objectsOfType = new ArrayList<>();
        for (NetObject s : rawList) {
            if (s.getSearchable().getClass().equals(T)) {
                objectsOfType.add(s);
            }
        }
        return objectsOfType;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public DocumentStatistic getDocumentStatistic() {
        return documentStatistic;
    }

    public void setDocumentStatistic(DocumentStatistic documentStatistic) {
        this.documentStatistic = documentStatistic;
    }

    @Override
    public void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    @Override
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @Override
    public <T> List<T> getAllFoundObjectsAsSearchable(Class T) {
        return getAllFoundObjects(T, node);
    }

}
