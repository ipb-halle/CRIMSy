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

import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.johnzon.mapper.Adapter;

/**
 *
 * @author fmauz
 */
@Stateless
public class SearchService {

    private ServiceAdapter adpater;
    @Inject
    private ItemService itemService;

    @Inject
    private ProjectService projectService;

    @Inject
    private MaterialService materialService;
    @Inject
    private ExperimentService experimentService;
    @Inject
    private DocumentSearchService documentService;
    @Inject
    private ContainerService containerService;
    @Inject
    private MemberService memberService;

    @PostConstruct
    public void init() {
        adpater = new ServiceAdapter(
                itemService,
                materialService,
                projectService,
                experimentService,
                documentService,
                containerService,
                memberService);
    }

    public SearchResult search(SearchRequest request) {
        SearchResult result = new SearchResultImpl();
        if (shouldSearchBeDone(request)) {
            result = doItemSearch(request, result);
        }
        return result;
    }

    private boolean shouldSearchBeDone(SearchRequest request) {
        return request != null && !request.getSearchTargets().isEmpty();
    }

    private SearchResult doItemSearch(SearchRequest request, SearchResult result) {
        for (SearchTarget target : request.getSearchTargets()) {
            SearchResult partialResult = adpater.doSearch(request, target);
            result = mergeResults(result, partialResult);
        }
        return result;
    }

    private SearchResult mergeResults(SearchResult totalResult, SearchResult partialResult) {
        return totalResult;
    }
}
