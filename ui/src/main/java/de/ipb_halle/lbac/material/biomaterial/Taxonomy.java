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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyEntity;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Taxonomy extends Material {

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    private TaxonomyLevel level;
    private List<Taxonomy> taxHierachy = new ArrayList<>();

    public Taxonomy(int id,
            List<MaterialName> names,
            HazardInformation hazards,
            StorageClassInformation storageInformation,
            List<Taxonomy> hierarchy,
            UUID ownerID,
            Date creationDate) {
        super(id, names, null, hazards, storageInformation);
        this.ownerID = ownerID;
        this.creationTime = creationDate;
        this.type = MaterialType.TAXONOMY;
        this.taxHierachy = hierarchy;

    }

    @Override
    public Taxonomy copyMaterial() {
        List<MaterialName> copiedNames = new ArrayList<>();
        for (MaterialName mn : names) {
            copiedNames.add(new MaterialName(mn.getValue(), mn.getLanguage(), mn.getRank()));
        }
        Taxonomy copiedTaxonomy = new Taxonomy(id, copiedNames, hazards, storageInformation, taxHierachy, ownerID, creationTime);
        copiedTaxonomy.setTaxHierachy(new ArrayList<>());
        for (Taxonomy t : taxHierachy) {
            copiedTaxonomy.getTaxHierachy().add(t);
        }
        copiedTaxonomy.setLevel(new TaxonomyLevel(level.getId(), level.getName(), level.getRank()));
        return copiedTaxonomy;

    }

    @Override
    public TaxonomyEntity createEntity() {
        TaxonomyEntity entity = new TaxonomyEntity();
        entity.setId(id);
        entity.setLevel(level.getId());
        return entity;
    }

    public TaxonomyLevel getLevel() {
        return level;
    }

    public List<Taxonomy> getTaxHierachy() {
        return taxHierachy;
    }

    public void setTaxHierachy(List<Taxonomy> taxHierachy) {
        this.taxHierachy = taxHierachy;
    }

    public void setLevel(TaxonomyLevel level) {
        this.level = level;
    }

}
