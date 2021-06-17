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
 * SearchQuery getting documents from a solr instance
 *
 *
 * This class encapsulates information on how to query the backend database
 */
import java.io.Serializable;

public class DocumentSearchQuery
        implements Cloneable, Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * a simple query string
     */
    private String query;

    //*** getter and setter
    public String getQuery() {
        return this.query;
    }

    public void setQuery(String q) {
        this.query = q;
    }

    //*** implementation ***
    /**
     * default constructor
     */
    public DocumentSearchQuery() {
    }

    public DocumentSearchQuery(String query) {
        this.query = query;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DocumentSearchQuery q = (DocumentSearchQuery) super.clone();
        q.setQuery(this.query);
        return q;
    }

}
