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
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.service.NodeService;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.Value;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

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
    @Inject
    private NodeService nodeService;

    private int AUGMENT_DOC_REQUEST_MAX_MATERIALS = 5;
    private int AUGMENT_DOC_REQUEST_MAX_NAMES_PER_MATERIALS = 5;

    @PostConstruct
    public void init() {
        adpater = new ServiceAdapter(
                itemService,
                materialService,
                projectService,
                experimentService,
                documentService,
                containerService,
                memberService,
                nodeService);
    }

    public SearchResult search(List<SearchRequest> requests) {
        sortSearchRequestsByPrio(requests);
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        if (requests != null) {
            for (SearchRequest request : requests) {
                result = handleSingleSearch(request, result);
            }
        }
        return result;
    }

    private SearchResult handleSingleSearch(SearchRequest request, SearchResult result) {
        if (shouldSearchBeDone(request)) {
            augmentDocumentSearchRequest(request, result);
            SearchResult partialResult = adpater.doSearch(request);
            result = mergeResults(result, partialResult);
        }
        return result;
    }

    private boolean shouldSearchBeDone(SearchRequest request) {
        return request != null && request.getSearchTarget() != null;
    }

    private SearchResult mergeResults(SearchResult totalResult, SearchResult partialResult) {
        Node node = partialResult.getNode();
        totalResult.addResults(partialResult.getAllFoundObjects(node));
        totalResult.getDocumentStatistic().merge(partialResult.getDocumentStatistic());
        return totalResult;
    }

    private void augmentDocumentSearchRequest(
            SearchRequest request,
            SearchResult result) {
        List<Structure> structures = result.getAllFoundObjects(Structure.class, result.getNode());
        int maxMats = Math.min(structures.size(), AUGMENT_DOC_REQUEST_MAX_MATERIALS);
        if (request.getSearchTarget() == SearchTarget.DOCUMENT) {
            for (int i = 0; i < maxMats; i++) {
                Structure struc = structures.get(i);
                for (int j = 0; j < struc.getNames().size(); j++) {
                    Set<String> valueSet = new HashSet<String>();
                    valueSet.add(struc.getNames().get(j).getValue());
                    Condition con = new Condition(
                            new Attribute(AttributeType.WORDROOT),
                            Operator.IN,
                            new Value(valueSet));
                    request.setCondition(con);
                    //hier neuen namen rein. Dieser muss allerdings gestemmt werden
                }
            }
        }

    }

    private void sortSearchRequestsByPrio(List<SearchRequest> requests) {
        if (requests != null) {
            Collections.sort(requests,
                    (SearchRequest sr1, SearchRequest sr2)
                    -> sr1.getSearchTarget().getSearchPrio()
                            .compareTo(sr2.getSearchTarget().getSearchPrio()));
        }
    }

}
