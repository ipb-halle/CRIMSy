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
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.NodeService;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    @Inject
    private SequenceSearchService sequenceSearchService;

    private SearchQueryStemmer searchQueryStemmer = new SearchQueryStemmer();

    private int AUGMENT_DOC_REQUEST_MAX_MATERIALS = 5;
    private int AUGMENT_DOC_REQUEST_MAX_NAMES_PER_MATERIALS = 5;

    private final Logger logger = LogManager.getLogger(DocumentSearchService.class);

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
                nodeService,
                sequenceSearchService);
    }

    public SearchResult search(
            List<SearchRequest> requests,
            Node node) {
        sortSearchRequestsByPrio(requests);
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        if (requests != null) {
            for (SearchRequest request : requests) {
                User u = request.getUser();
                if (this.nodeService.isRemoteNode(node)) {
                    request.setUser(memberService.mapRemoteUserToLocalUser(u, node));
                }
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
        if (request.getSearchTarget() == SearchTarget.DOCUMENT) {
            Set<String> materialNames = getNamesOfMaterials(result);

            StringBuilder sb = new StringBuilder();
            for (String name : materialNames) {
                sb.append(name);
                sb.append(" ");
            }
            for (String stem : searchQueryStemmer.stemmQuery(sb.toString())) {
                request.addSearchCategory(SearchCategory.WORDROOT, stem);
            }
        }
    }

    private Set<String> getNamesOfMaterials(SearchResult result) {
        Set<String> newNames = new HashSet<>();
        List<Structure> structures = result.getAllFoundObjects(Structure.class, result.getNode());
        int maxMats = Math.min(structures.size(), AUGMENT_DOC_REQUEST_MAX_MATERIALS);
        for (int i = 0; i < maxMats; i++) {
            addNamesOfMaterial(
                    structures.get(i),
                    AUGMENT_DOC_REQUEST_MAX_NAMES_PER_MATERIALS,
                    newNames);
        }
        return newNames;
    }

    private void addNamesOfMaterial(Material m, int maxNamesBorder, Set<String> newNames) {
        int maxNames = Math.min(
                maxNamesBorder,
                m.getNames().size());
        for (int j = 0; j < maxNames; j++) {
            newNames.add(m.getNames().get(j).getValue().toLowerCase());
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
