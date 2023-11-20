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
import de.ipb_halle.lbac.material.biomaterial.BioMaterialEntity;
import de.ipb_halle.lbac.material.composition.MaterialCompositionEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialDetailRightEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.sequence.SequenceEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.project.ProjectEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import jakarta.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class MaterialEntityGraphBuilder extends EntityGraphBuilder {

    public final static String COMPONENT_MATERIAL_SUBGRAPHNAME = "componentMaterials";
    protected ACListService aclistService;
    protected EntityGraph detailRightSubGraph;
    private EntityGraph componentIndexGraph;
    private EntityGraph materialsGraph;
    private EntityGraph componentsGraph;

    public MaterialEntityGraphBuilder() {
        super(MaterialEntity.class);
    }

    protected void addProject() {
        addJoinToChildInherit(JoinType.LEFT, materialsGraph, ProjectEntity.class, "projectid", "id");
    }

    protected void addComponents() {
        componentsGraph = addJoinInherit(JoinType.LEFT, MaterialCompositionEntity.class, "materialid", "materialid");
        materialsGraph = addJoinToChildInherit(JoinType.LEFT, componentsGraph, MaterialEntity.class, "componentid", "materialid");
        materialsGraph.setGraphName(COMPONENT_MATERIAL_SUBGRAPHNAME);
        materialsGraph.setSubSelectAttribute(AttributeType.DIRECT);
        materialsGraph.addAttributeType(AttributeType.DIRECT);

    }

    protected void addIndex() {
        componentIndexGraph = addJoinToChild(JoinType.LEFT, materialsGraph, MaterialIndexEntryEntity.class, "materialid", "materialid");
    }

    protected void addOwner() {
        addJoinToChildInherit(JoinType.INNER, materialsGraph, MemberEntity.class, "owner_id", "id");
    }

    protected void addDetailRights() {
        detailRightSubGraph = addJoinToChild(JoinType.INNER, materialsGraph, MaterialDetailRightEntity.class, "materialid", "materialid");
    }

    protected void addStructure() {
        EntityGraph subGraph = addJoinToChild(JoinType.LEFT, materialsGraph, StructureEntity.class, "materialid", "id");
        addJoinToChild(JoinType.LEFT, subGraph, MoleculeEntity.class, "moleculeid", "id");
    }

    protected void addSequences() {
        addJoinToChild(JoinType.LEFT, materialsGraph, SequenceEntity.class, "materialid", "id");

    }

    protected void addAcls() {
        addACListConstraint(materialsGraph, getACESubGraph(), "aclist_id", true);
        addACListConstraint(graph, getACESubGraph(), "aclist_id", true);

    }

    protected void addBioMaterial() {
        EntityGraph subGraph = addJoin(JoinType.LEFT, BioMaterialEntity.class, "materialid", "id");
        EntityGraph taxoGraph = addJoinToChild(JoinType.LEFT, subGraph, MaterialIndexEntryEntity.class, "materialid", "taxoid");
        subGraph.addAttributeType(AttributeType.TOPLEVEL);
        subGraph.setSubSelectAttribute(AttributeType.DIRECT);
        taxoGraph.addAttributeType(AttributeType.TOPLEVEL);
        taxoGraph.setSubSelectAttribute(AttributeType.DIRECT);

    }

    @Override
    public EntityGraph buildEntityGraph(boolean toplevel) {
        addComponents();
        addIndex();
        addOwner();
        addStructure();
        addSequences();
        addProject();
        addAcls();
        //addBioMaterial();
        graph.addAttributeType(AttributeType.DIRECT);
        if (toplevel) {
            componentIndexGraph.addAttributeType(AttributeType.TOPLEVEL);
            graph.addAttributeType(AttributeType.TOPLEVEL);
        }
        return graph;
    }
}
