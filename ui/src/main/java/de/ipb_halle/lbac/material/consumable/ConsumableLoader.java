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
package de.ipb_halle.lbac.material.consumable;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.ArrayList;
import javax.persistence.EntityManager;

/**
 *
 * @author fmauz
 */
public class ConsumableLoader implements MaterialLoader {

    private static final long serialVersionUID = 1L;

    @Override
    public Material loadMaterial(
            MaterialEntity entity,
            EntityManager em,
            MaterialService materialService,
            TaxonomyService taxoService,
            TissueService tissueService,
            ACListService aclistService,
            User currentUser) {
        Consumable c = new Consumable(
                entity.getMaterialid(),
                new ArrayList<>(),
                entity.getProjectid(),
                new HazardInformation(),
                new StorageInformation());
        return c;
    }

}
