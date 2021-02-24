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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.EntityGraph;

/**
 *
 * @author fmauz
 */
public class ProjectEntityGraphBuilder extends EntityGraphBuilder {

    private ACListService acListService;

    public ProjectEntityGraphBuilder(ACListService acListService) {
        super(ProjectEntity.class);
        this.acListService = acListService;
    }

    public EntityGraph buildEntityGraph(boolean toplevel) {
        addACListContraint(graph, acListService.getEntityGraph(), "aclist_id");
        return graph;
    }

}
