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
package de.ipb_halle.lbac.exp.search;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.exp.ExperimentEntity;
import de.ipb_halle.lbac.exp.assay.AssayEntity;
import de.ipb_halle.lbac.exp.assay.AssayRecordEntity;
import de.ipb_halle.lbac.exp.text.TextEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialDetailRightEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class ExperimentEntityGraphBuilder extends EntityGraphBuilder {

    private EntityGraph assayEntityGraph;
    private EntityGraph assayRecordEntityGraph;
    private EntityGraph expRecordGraph;
    private EntityGraph materialDetailRightGraph;
    private ACListService aclistService;

    public ExperimentEntityGraphBuilder(ACListService aclistService) {
        super(ExperimentEntity.class);
        this.aclistService = aclistService;
    }

    private void addUser() {
        addJoin(JoinType.INNER, MemberEntity.class, "owner_id", "id");
    }

    private void addExpRecords() {

        expRecordGraph = addJoin(JoinType.LEFT, ExpRecordEntity.class, "experimentid", "experimentid");

        addJoinToChild(JoinType.LEFT, expRecordGraph, TextEntity.class, "exprecordid", "exprecordid");
        assayEntityGraph = addJoinToChild(JoinType.LEFT, expRecordGraph, AssayEntity.class, "exprecordid", "exprecordid");

        assayRecordEntityGraph = addJoinToChild(JoinType.LEFT, assayEntityGraph, AssayRecordEntity.class, "exprecordid", "exprecordid");
        addJoinToChild(JoinType.LEFT, assayRecordEntityGraph, MaterialIndexEntryEntity.class, "materialid", "materialid");
        addJoinToChild(JoinType.LEFT, assayRecordEntityGraph, ItemEntity.class, "itemid", "id");
    }

    @Override
    public EntityGraph buildEntityGraph(Condition condition) {
        addUser();
        addExpRecords();
        addACListContraint(graph,aclistService.getEntityGraph(), "aclist_id");
        return graph;
    }

}
