/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence;

import javax.persistence.EntityManager;

import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;
import de.ipb_halle.lbac.material.common.service.MaterialService;

/**
 * 
 * @author flange
 */
public class SequenceLoader implements MaterialLoader {
    private static final long serialVersionUID = 1L;

    @Override
    public Sequence loadMaterial(MaterialEntity materialEntity, EntityManager em,
            MaterialService materialService, TaxonomyService taxoService,
            TissueService tissueService) {
        SequenceEntity sequenceEntity = em.find(SequenceEntity.class,
                materialEntity.getMaterialid());
        return Sequence.fromEntities(materialEntity, sequenceEntity);
    }
}