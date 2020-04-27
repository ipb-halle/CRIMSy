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

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.entity.taxonomy.TaxonomyEntity;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class Taxonomy extends Material {

    private TaxonomyLevel level;
    private List<Taxonomy> taxHierachy = new ArrayList<>();

    public Taxonomy(int id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageClassInformation storageInformation,
            List<Taxonomy> hierarchy) {
        super(id, names, projectId, hazards, storageInformation);
        this.type = MaterialType.TAXONOMY;

    }

    @Override
    public Material copyMaterial() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TaxonomyEntity createEntity() {
        TaxonomyEntity entity = new TaxonomyEntity();
        entity.setId(id);
        entity.setLevel(level.getId());
        return entity;
    }

    public void setLevel(TaxonomyLevel level) {
        this.level = level;
    }

    

}
