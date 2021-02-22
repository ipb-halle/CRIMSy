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
package de.ipb_halle.lbac.material.common.service;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialDetailRightEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.project.ProjectEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class MaterialEntityGraphBuilder extends EntityGraphBuilder {

    protected ACListService aclistService;
    protected EntityGraph detailRightSubGraph;

    @Deprecated
    public MaterialEntityGraphBuilder(ACListService aclistService) {
        super(MaterialEntity.class);
        this.aclistService = aclistService;
    }

    public MaterialEntityGraphBuilder() {
        super(MaterialEntity.class);
    }

    protected void addProject() {
        addJoinInherit(JoinType.LEFT, ProjectEntity.class, "projectid", "id");
    }

    protected void addIndex() {
        addJoin(JoinType.INNER, MaterialIndexEntryEntity.class, "materialid", "materialid");
    }

    protected void addOwner() {
        EntityGraph owner = addJoinInherit(JoinType.INNER, MemberEntity.class, "ownerid", "id");
        owner.addAttributeTypeInherit(AttributeType.OWNER);
    }

    protected void addDetailRights() {
        detailRightSubGraph = addJoin(JoinType.INNER, MaterialDetailRightEntity.class, "materialid", "materialid");
    }

    protected void addStructure() {
        EntityGraph subGraph = addJoin(JoinType.LEFT, StructureEntity.class, "materialid", "id");
        addJoinToChild(JoinType.LEFT, subGraph, MoleculeEntity.class, "moleculeid", "id");
    }

    protected void addAcls() {
        addACListConstraint(graph, getACESubGraph(), "aclist_id", true);
    }

    @Override
    public EntityGraph buildEntityGraph() {
        addIndex();
        addOwner();
        addStructure();
        addDetailRights();
        addProject();
        addAcls();
        graph.addAttributeType(AttributeType.DIRECT);
        return graph;
    }

}
