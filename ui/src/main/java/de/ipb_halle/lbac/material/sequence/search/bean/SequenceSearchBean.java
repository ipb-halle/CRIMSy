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

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.SequenceSearchService;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author flange
 */
@Named
@SessionScoped
public class SequenceSearchBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;
    @Inject
    private ProjectService projectService;

    @Inject
    private MemberService memberService;

    @Inject
    private SequenceSearchService sequenceSearchService;

    @Inject
    private transient MessagePresenter messagePresenter;

    private SequenceSearchMaskController searchMaskController;
    private SequenceSearchResultsTableController resultsTableController;

    @PostConstruct
    public void init() {
        resultsTableController = new SequenceSearchResultsTableController(
                materialService,
                sequenceSearchService,
                messagePresenter);
        searchMaskController = new SequenceSearchMaskController(
                resultsTableController,
                materialService,
                projectService,
                memberService,
                messagePresenter);
        resultsTableController.setSearchMaskController(searchMaskController);
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        resultsTableController.setLastUser(evt.getCurrentAccount());
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
