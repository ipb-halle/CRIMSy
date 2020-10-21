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

/**
 *
 * @author fmauz
 */
public class ServiceAdapter {

    private ItemService itemService;
    private MaterialService materialService;
    private ProjectService projectService;
    private ExperimentService experimentService;
    private DocumentSearchService documentService;
    private ContainerService containerService;
    private MemberService memberService;

    public ServiceAdapter(
            ItemService itemService,
            MaterialService materialService,
            ProjectService projectService,
            ExperimentService experimentService,
            DocumentSearchService documentService,
            ContainerService containerService,
            MemberService memberService) {
        this.itemService = itemService;
        this.materialService = materialService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.documentService = documentService;
        this.containerService = containerService;
        this.memberService = memberService;
    }

    public SearchResult doSearch(SearchRequest request) {
        SearchTarget target = request.getSearchTarget();
        if (target == SearchTarget.EXPERIMENT) {
            return experimentService.load(request);
        }
        if (target == SearchTarget.ITEM) {
            return itemService.loadItems(request);
        }
        if (target == SearchTarget.MATERIAL) {
            return materialService.getReadableMaterials(request);
        }
        if (target == SearchTarget.PROJECT) {
            return projectService.loadProjects(request);
        }
        if (target == SearchTarget.DOCUMENT) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
        if (target == SearchTarget.USER) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
        if (target == SearchTarget.CONTAINER) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
        return new SearchResultImpl();
    }

}
