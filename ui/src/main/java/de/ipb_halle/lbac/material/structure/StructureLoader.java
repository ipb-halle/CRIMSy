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
package de.ipb_halle.lbac.material.structure;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 *
 * @author fmauz
 */
public class StructureLoader implements MaterialLoader {

    private static final long serialVersionUID = 1L;
    private final String SQL_GET_STRUCTURE_INFOS = "SELECT id,sumformula,molarmass,exactmolarmass,moleculeid FROM structures WHERE id=:mid";
    private final String SQL_GET_MOLECULE = "SELECT id,molecule FROM molecules WHERE id=:mid";

    @Override
    public Material loadMaterial(
            MaterialEntity entity,
            EntityManager em,
            MaterialService materialService,
            TaxonomyService taxoService,
            TissueService tissueService,
            ACListService aclistService,
            User currentUser) {

        Query q5 = em.createNativeQuery(SQL_GET_STRUCTURE_INFOS, StructureEntity.class);
        q5.setParameter("mid", entity.getMaterialid());

        StructureEntity sE = (StructureEntity) q5.getSingleResult();
        String molecule = "";
        String moleculeFormat = "";
        int moleculeId = 0;
        if (sE.getMoleculeid() != null && sE.getMoleculeid() != 0) {
            Query q6 = em.createNativeQuery(SQL_GET_MOLECULE);
            q6.setParameter("mid", sE.getMoleculeid());
            Object[] result = (Object[]) q6.getSingleResult();
            moleculeId = (int) result[0];
            molecule = (String) result[1];
        }

        Structure s = Structure.createInstanceFromDB(
                entity,
                new HazardInformation(),
                new StorageInformation(),
                new ArrayList<>(),
                sE,
                molecule,
                moleculeId,
                moleculeFormat);
        return s;
    }

}
