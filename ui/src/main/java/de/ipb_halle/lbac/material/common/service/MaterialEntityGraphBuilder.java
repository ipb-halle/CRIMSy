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

import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.structure.MoleculeEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.project.ProjectEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class MaterialEntityGraphBuilder extends EntityGraphBuilder {

    public MaterialEntityGraphBuilder() {
        super(MaterialEntity.class);
    }

    protected void addProject() {
        addJoin(JoinType.LEFT, ProjectEntity.class, "projectid", "id");
    }

    protected void addIndex() {
        addJoin(JoinType.LEFT, MaterialIndexEntryEntity.class, "materialid", "materialid");
    }

    protected void addUser() {
        addJoin(JoinType.INNER, MemberEntity.class, "ownerid", "id");
    }

    protected void addStructure() {
        EntityGraph subGraph = addJoin(JoinType.INNER, StructureEntity.class, "materialid", "id");
        addJoinToChild(JoinType.INNER, subGraph, MoleculeEntity.class, "moleculeid", "id");
    }

    @Override
    public EntityGraph buildEntityGraph(Condition condition) {
        addProject();
        addIndex();
        addUser();
        addStructure();
        return graph;
    }

}
