/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.subtype.biomaterial;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.entity.biomaterial.BioMaterialEntity;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import de.ipb_halle.lbac.material.subtype.tissue.Tissue;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class BioMaterial extends Material {

    private Taxonomy taxonomy;
    private Tissue tissue;

    public BioMaterial(
            int id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageClassInformation storageInfos,
            Taxonomy taxonomy,
            Tissue tissue) {
        super(id, names, projectId, hazards, storageInfos);
        this.type = MaterialType.BIOMATERIAL;
        this.tissue = tissue;
        this.taxonomy = taxonomy;
    }

    @Override
    public String getNumber() {
        return "";
    }

    @Override
    public Material copyMaterial() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BioMaterialEntity createEntity() {
        BioMaterialEntity entity = new BioMaterialEntity();
        entity.setId(id);
        entity.setTaxoid(taxonomy.getId());
        if (tissue != null) {
            entity.setTissueid(tissue.getId());
        }
        return entity;
    }

}
