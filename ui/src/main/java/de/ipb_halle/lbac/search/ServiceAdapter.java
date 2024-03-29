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
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.NodeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class ServiceAdapter {

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    private ItemService itemService;
    private MaterialService materialService;
    private ProjectService projectService;
    private ExperimentService experimentService;
    private DocumentSearchService documentService;
    private ContainerService containerService;
    private MemberService memberService;
    private NodeService nodeService;
    private SequenceSearchService sequenceSearchService;

    public ServiceAdapter(
            ItemService itemService,
            MaterialService materialService,
            ProjectService projectService,
            ExperimentService experimentService,
            DocumentSearchService documentService,
            ContainerService containerService,
            MemberService memberService,
            NodeService nodeService,
            SequenceSearchService sequenceSearchService) {
        this.itemService = itemService;
        this.materialService = materialService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.documentService = documentService;
        this.containerService = containerService;
        this.memberService = memberService;
        this.nodeService = nodeService;
        this.sequenceSearchService = sequenceSearchService;
    }

    public SearchResult doSearch(SearchRequest request) {
        SearchTarget target = request.getSearchTarget();
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());

        if (target == SearchTarget.EXPERIMENT) {
            result = experimentService.load(request);
        }
        if (target == SearchTarget.ITEM) {
            result = itemService.loadItems(request);
        }
        if (target == SearchTarget.MATERIAL) {
            if (isSequenceAlignmentSearch(request)) {
                result = sequenceSearchService.searchSequences(request);
            } else {
                result = materialService.loadReadableMaterials(request);
            }
        }
        if (target == SearchTarget.PROJECT) {
            result = projectService.loadProjects(request);
        }
        if (target == SearchTarget.DOCUMENT) {
            result = documentService.loadDocuments(request);
        }
        if (target == SearchTarget.USER) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
        if (target == SearchTarget.CONTAINER) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        return result;
    }

    private boolean isSequenceAlignmentSearch(SearchRequest request) {
        return request.getSearchValues().containsKey(SearchCategory.SEQUENCE_QUERY_STRING);
    }

}
