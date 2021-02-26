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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.container.entity.ContainerEntity;
import de.ipb_halle.lbac.container.entity.ContainerNestingEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
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
public class ItemEntityGraphBuilder extends EntityGraphBuilder {

    private EntityGraph nestedContainerGraph;
    private EntityGraph materialSubgraph;

    public ItemEntityGraphBuilder() {
        super(ItemEntity.class);
    }

    private void addOwner() {
        EntityGraph owner = addJoinInherit(JoinType.INNER, MemberEntity.class, "owner_id", "id");
//        owner.addAttributeType(AttributeType.DIRECT);
        owner.addAttributeTypeInherit(AttributeType.OWNER);

    }

    private void addContainer() {
        nestedContainerGraph = addJoin(JoinType.LEFT, ContainerNestingEntity.class, "containerid", "sourceid");
        addJoinToChild(JoinType.LEFT, nestedContainerGraph, ContainerEntity.class, "targetid", "id");
        addJoin(JoinType.LEFT, ContainerEntity.class, "containerid", "id");
    }

    private void addProject() {
        addJoinInherit(JoinType.LEFT, ProjectEntity.class, "projectid", "id");
    }

    private void addMaterial() {
        MaterialEntityGraphBuilder matBuilder = new MaterialEntityGraphBuilder();
        materialSubgraph = matBuilder.buildEntityGraph(false);
        materialSubgraph.addLinkField("materialid", "materialid");
        materialSubgraph.setSubSelectAttribute(AttributeType.DIRECT);
        materialSubgraph.setJoinType(JoinType.LEFT);
        graph.addChild(materialSubgraph);
    }

    protected void addStructure() {
        EntityGraph subGraph = addJoin(JoinType.LEFT, StructureEntity.class, "materialid", "id");
        addJoinToChild(JoinType.LEFT, subGraph, MoleculeEntity.class, "moleculeid", "id");
    }

    @Override
    public EntityGraph buildEntityGraph(boolean toplevel) {
        addOwner();
        addContainer();
        addProject();
        addMaterial();

        graph.addAttributeType(AttributeType.DIRECT);
        addACListConstraint(graph, getACESubGraph(), "aclist_id", true);
        if (toplevel) {
            graph.addAttributeType(AttributeType.TOPLEVEL);
        }
        return graph;
    }

    public EntityGraph getMaterialSubgraph() {
        return this.materialSubgraph;
    }
}
