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
import de.ipb_halle.lbac.exp.ExperimentEntity;
import de.ipb_halle.lbac.exp.text.TextEntity;
import de.ipb_halle.lbac.search.EntityGraphBuilder;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import javax.persistence.criteria.JoinType;

/**
 *
 * @author fmauz
 */
public class ExperimentEntityGraphBuilder extends EntityGraphBuilder {

    public ExperimentEntityGraphBuilder() {
        super(ExperimentEntity.class);
    }

    private void addUser() {
        addJoin(JoinType.INNER, MemberEntity.class, "owner_id", "id");
    }

    private void addTextRecord() {
        addJoin(JoinType.LEFT, TextEntity.class, "experimentid", "exprecordid");
    }

    @Override
    public EntityGraph buildEntityGraph(Condition condition) {
        addUser();
        addTextRecord();
        return graph;
    }

}
