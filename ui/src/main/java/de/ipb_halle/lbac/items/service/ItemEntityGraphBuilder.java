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
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.project.ProjectEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class ItemEntityGraphBuilder extends EntityGraphBuilder {

    private EntityGraph nestedContainerGraph;

    public ItemEntityGraphBuilder() {
        super(ItemEntity.class);
    }

    private void addUser() {
        addJoin(JoinType.INNER, MemberEntity.class, "owner_id", "id");
    }

    private void addContainer() {
        nestedContainerGraph = addJoin(JoinType.LEFT, ContainerNestingEntity.class, "containerid", "sourceid");
        addJoinToChild(JoinType.LEFT, nestedContainerGraph, ContainerEntity.class, "targetid", "id");
        addJoin(JoinType.LEFT, ContainerEntity.class, "containerid", "id");
    }

    private void addProject() {
        addJoin(JoinType.LEFT, ProjectEntity.class, "projectid", "id");
    }

    private void addMaterialName() {
        addJoin(JoinType.INNER, MaterialIndexEntryEntity.class, "materialid", "materialid");
    }

    @Override
    public EntityGraph buildEntityGraph(Condition condition) {
        addUser();
        addContainer();
        addProject();
        addMaterialName();
        //TO DO: remove the conditions;
        return graph;
    }

}
