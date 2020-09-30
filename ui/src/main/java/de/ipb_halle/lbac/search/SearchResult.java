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
import java.util.List;

/**
 *
 * @author fmauz
 */
public interface SearchResult {

    public List<NetObject> getAllFoundObjects();

    public List<Searchable> getAllFoundObjects(Node n);

    public <T> List<NetObject> getAllFoundObjects(Class T); //T nur Searchable

    public <T> List<T> getAllFoundObjects(Class T, Node n); //T nur Searchable

    public void addResults(Node n, List<Searchable> foundObjects);

}