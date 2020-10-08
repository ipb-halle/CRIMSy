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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class SearchResultImpl implements SearchResult {

    protected HashMap<Node, List<Searchable>> foundObjectsOfNode = new HashMap<>();

    @Override
    public void addResults(Node n, List<Searchable> foundObjects) {
        if (foundObjectsOfNode.get(n) == null) {
            foundObjectsOfNode.put(n, new ArrayList<>());
        }
        foundObjectsOfNode.get(n).addAll(foundObjects);
    }

    @Override
    public List<NetObject> getAllFoundObjects() {
        List<NetObject> foundObjects = new ArrayList<>();
        for (Node node : foundObjectsOfNode.keySet()) {
            for (Searchable s : foundObjectsOfNode.get(node)) {
                foundObjects.add(new NetObjectImpl(s, node));
            }
        }
        return foundObjects;
    }

    @Override
    public List<Searchable> getAllFoundObjects(Node n) {
        if (foundObjectsOfNode.get(n) == null) {
            return new ArrayList<>();
        }
        return foundObjectsOfNode.get(n);
    }

    @Override
    public <T> List<NetObject> getAllFoundObjects(Class T) {
        List<NetObject> netObjects = getAllFoundObjects();
        return fetchAllObjectsOf(netObjects, T);
    }

    @Override
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
    public Set<Node> getNodes() {
       return foundObjectsOfNode.keySet();
    }


    
    
}
