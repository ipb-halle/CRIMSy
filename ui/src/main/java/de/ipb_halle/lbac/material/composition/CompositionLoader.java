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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author fmauz
 */
public class CompositionLoader implements MaterialLoader {

    public final String SQL_SELECT_COMPONENTS
            = "SELECT materialid, componentid,concentration "
            + "FROM material_compositions "
            + "WHERE materialid=:mid";

    private static final long serialVersionUID = 1L;

    @Override
    public Material loadMaterial(MaterialEntity entity, EntityManager em, MaterialService materialService, TaxonomyService taxoService, TissueService tissueService) {
        CompositionEntity compositionEntity = em.find(CompositionEntity.class, entity.getMaterialid());
        CompositionType compositionType = CompositionType.valueOf(compositionEntity.getType());
        MaterialComposition composition = new MaterialComposition(
                entity.getMaterialid(),
                entity.getProjectid(),
                compositionType);

        List<MaterialCompositionEntity> entities
                = (List<MaterialCompositionEntity>) em.createNativeQuery(SQL_SELECT_COMPONENTS, MaterialCompositionEntity.class)
                        .setParameter("mid", entity.getMaterialid())
                        .getResultList();
        for (MaterialCompositionEntity mce : entities) {
            if (mce.getId().getComponentid() != entity.getMaterialid()) {
                composition.addComponent(materialService.loadMaterialById(mce.getId().getComponentid()), mce.getConcentration());
            }
        }
        return composition;
    }

}
