/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.bean;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * 
 * @author flange
 */
@Named
@SessionScoped
public class SequenceSearchBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private SequenceSearchMaskController searchMaskController;
    private SequenceSearchResultsTableController resultsTableController;

    @PostConstruct
    public void init() {
        searchMaskController = new SequenceSearchMaskController();
        resultsTableControllerableController = new SequenceSearchResultsTableController();
    }

    /*
     * Getters/setters for fields
     */
    public SequenceSearchMaskController getSearchMaskController() {
        return searchMaskController;
    }

    public SequenceSearchResultsTableController getResultsTableController() {
        return resultsTableController;
    }
}