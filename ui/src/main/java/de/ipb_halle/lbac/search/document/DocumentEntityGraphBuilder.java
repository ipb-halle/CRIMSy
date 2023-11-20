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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.kx.file.FileObjectEntity;
import de.ipb_halle.kx.termvector.TermVectorEntity;
import de.ipb_halle.lbac.collections.CollectionEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import jakarta.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class DocumentEntityGraphBuilder extends EntityGraphBuilder {

    public DocumentEntityGraphBuilder() {
        super(FileObjectEntity.class);
    }

    private void addTermVector() {
        addJoin(JoinType.INNER, TermVectorEntity.class, "id", "file_id");
    }

    private void addCollections() {
        EntityGraph collectionGraph = addJoin(JoinType.INNER, CollectionEntity.class, "collection_id", "id");
        collectionGraph.addAttributeType(AttributeType.DIRECT);
        collectionGraph.setGraphName("collections");
        addACListConstraint(collectionGraph, getACESubGraph(), "aclist_id", true);
    }

    @Override
    public EntityGraph buildEntityGraph(boolean toplevel) {
        addTermVector();
        addCollections();
        return graph;
    }
}
