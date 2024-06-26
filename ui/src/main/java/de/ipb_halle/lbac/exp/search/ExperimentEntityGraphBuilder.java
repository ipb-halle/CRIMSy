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

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.exp.ExperimentEntity;
import de.ipb_halle.lbac.exp.assay.AssayEntity;
import de.ipb_halle.lbac.datalink.LinkedDataEntity;
import de.ipb_halle.lbac.exp.text.TextEntity;
import de.ipb_halle.lbac.items.service.ItemEntityGraphBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import jakarta.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class ExperimentEntityGraphBuilder extends EntityGraphBuilder {

    public final static String itemSubGraphName = "expItems";
    public final static String materialSubGraphName = "expMaterials";
    public final static String linkedDataGraphPath = "exp_records/linked_data";
    public final static String itemSubGraphPath = linkedDataGraphPath + "/" + itemSubGraphName;
    public final static String materialSubGraphPath = linkedDataGraphPath + "/" + materialSubGraphName;

    private EntityGraph linkedDataEntityGraph;
    private EntityGraph expRecordGraph;

    public ExperimentEntityGraphBuilder() {
        super(ExperimentEntity.class);
    }

    private void addUser() {
        addJoin(JoinType.INNER, MemberEntity.class, "owner_id", "id");
    }

    private void addExpRecords() {
        expRecordGraph = addJoin(JoinType.LEFT, ExpRecordEntity.class, "experimentid", "experimentid");
        // LinkedData
        linkedDataEntityGraph = addJoinToChild(JoinType.LEFT, expRecordGraph, LinkedDataEntity.class, "exprecordid", "exprecordid");
        addMaterialSubGraph(linkedDataEntityGraph);
        addItemSubGraph(linkedDataEntityGraph);
        // Text
        addJoinToChild(JoinType.LEFT, expRecordGraph, TextEntity.class, "exprecordid", "exprecordid");
        // Assay
        addJoinToChild(JoinType.LEFT, expRecordGraph, AssayEntity.class, "exprecordid", "exprecordid");
    }

    private void addMaterialSubGraph(EntityGraph linkDataGraph) {
        MaterialEntityGraphBuilder materialBuilder = new MaterialEntityGraphBuilder();
        EntityGraph materialSubgraph = materialBuilder.buildEntityGraph(false);
        materialSubgraph.addLinkField("materialid", "materialid");
        materialSubgraph.setSubSelectAttribute(AttributeType.DIRECT);
        materialSubgraph.setJoinType(JoinType.LEFT);
        materialSubgraph.setGraphName(materialSubGraphName);
        linkDataGraph.addChild(materialSubgraph);
    }

    private void addItemSubGraph(EntityGraph linkDataGraph) {
        ItemEntityGraphBuilder itemBuilder = new ItemEntityGraphBuilder();
        EntityGraph itemSubgraph = itemBuilder.buildEntityGraph(false);
        itemSubgraph.addLinkField("itemid", "id");
        itemSubgraph.setSubSelectAttribute(AttributeType.DIRECT);
        itemSubgraph.setJoinType(JoinType.LEFT);
        itemSubgraph.setGraphName(itemSubGraphName);
        linkDataGraph.addChild(itemSubgraph);
    }

    @Override
    public EntityGraph buildEntityGraph(boolean toplevel) {
        addUser();
        addExpRecords();
        addACListConstraint(graph, getACESubGraph(), "aclist_id", true);
        graph.addAttributeType(AttributeType.TOPLEVEL);
        graph.addAttributeType(AttributeType.DIRECT);
        return graph;
    }

}
