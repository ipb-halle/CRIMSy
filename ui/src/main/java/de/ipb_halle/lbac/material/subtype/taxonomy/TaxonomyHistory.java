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
package de.ipb_halle.lbac.material.subtype.taxonomy;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.entity.taxonomy.TaxonomyHistEntity;
import de.ipb_halle.lbac.material.entity.taxonomy.TaxonomyHistEntityId;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class TaxonomyHistory implements DTO {

    private Integer taxonomyId;
    private User actor;
    private Date mdate;
    private Integer level_old;
    private Integer level_new;

    public TaxonomyHistory(TaxonomyHistEntity entity, User actor) {
        taxonomyId = entity.getId().getTaxonomyId();
        this.actor = actor;
        this.mdate = entity.getId().getMdate();
        this.level_new = entity.getLevel_new();
        this.level_old = entity.getLevel_old();
    }

    @Override
    public TaxonomyHistEntity createEntity() {
        TaxonomyHistEntity entity = new TaxonomyHistEntity();
        entity.setId(new TaxonomyHistEntityId(taxonomyId, mdate, actor.getId()));
        entity.setLevel_new(level_new);
        entity.setLevel_old(level_old);
        return entity;

    }

}
