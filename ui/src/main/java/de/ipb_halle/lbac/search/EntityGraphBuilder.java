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

import de.ipb_halle.lbac.admission.ACEntryEntity;
import de.ipb_halle.lbac.admission.MembershipEntity;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import jakarta.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public abstract class EntityGraphBuilder {

    protected EntityGraph graph;

    public EntityGraphBuilder(Class mainEntity) {
        graph = new EntityGraph(mainEntity);
    }

    protected EntityGraph addJoin(JoinType type, Class joinedEntity, String leftJoinField, String rightJoinField) {
        EntityGraph child = new EntityGraph(joinedEntity)
                .addLinkField(leftJoinField, rightJoinField)
                .setJoinType(type);
        graph.addChild(child);
        return child;
    }
    protected EntityGraph addJoinInherit(JoinType type, Class joinedEntity, String leftJoinField, String rightJoinField) {
        EntityGraph child = new EntityGraph(joinedEntity)
                .addLinkField(leftJoinField, rightJoinField)
                .setJoinType(type);
        graph.addChildInherit(child);
        return child;
    }

    protected EntityGraph addJoinToChild(JoinType type, EntityGraph subGraph, Class joinedEntity, String leftJoinField, String rightJoinField) {
        EntityGraph child = new EntityGraph(joinedEntity)
                .addLinkField(leftJoinField, rightJoinField)
                .setJoinType(type);
        subGraph.addChild(child);
        return child;
    }

        protected EntityGraph addJoinToChildInherit(JoinType type, EntityGraph subGraph, Class joinedEntity, String leftJoinField, String rightJoinField) {
        EntityGraph child = new EntityGraph(joinedEntity)
                .addLinkField(leftJoinField, rightJoinField)
                .setJoinType(type);
        subGraph.addChildInherit(child);
        return child;
    }

    
    public void addACListConstraint(
            EntityGraph mainGraph,
            EntityGraph aclSubGraph,
            String linkField,
            boolean direct) {
        aclSubGraph.addLinkField(linkField, "aclist_id");
        if (direct) {
            aclSubGraph.addAttributeTypeInherit(AttributeType.DIRECT);
        }
        mainGraph.addChildInherit(aclSubGraph);
    }

    public abstract EntityGraph buildEntityGraph(boolean toplevel);

    /**
     * @return an entity subgraph suitable for access control and relating  
     * to the acentries and memberships tables. The returned 
     * <code>EntityGraph</code> object requires its link field to be set via 
     * <code>.setLinkField(..., "aclist_id")</code>.
     */
    public EntityGraph getACESubGraph() {
        return new EntityGraph(ACEntryEntity.class)
            .addChild(new EntityGraph(MembershipEntity.class)
                .addLinkField("member_id", "group_id")
            );
    }

}
