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
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.util.jsf.SendFileBean;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
    private SendFileBean sendFileBean;

    @Inject
    private SequenceSearchService sequenceSearchService;

    @Inject
    private MaterialOverviewBean overviewBean;

    @Inject
    private LoggingProfiler loggingProfiler;
    
    @Inject
    private transient MessagePresenter messagePresenter;

    private SequenceSearchMaskController searchMaskController;
    private SequenceSearchResultsTableController resultsTableController;

    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("SequenceSearchBean");
        resultsTableController = new SequenceSearchResultsTableController(
                materialService,
                sequenceSearchService,
                sendFileBean,
                messagePresenter);
        searchMaskController = new SequenceSearchMaskController(
                overviewBean,
                resultsTableController,
                materialService,
                projectService,
                memberService,
                messagePresenter);
        resultsTableController.setSearchMaskController(searchMaskController);
        loggingProfiler.profilerStop("SequenceSearchBean");
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("SequenceSearchBean.setCurrentAccount");

        resultsTableController.setLastUser(evt.getCurrentAccount());
        loggingProfiler.profilerStop("SequenceSearchBean.setCurrentAccount");

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
