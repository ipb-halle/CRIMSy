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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author fmauz
 */
public class Taxonomy extends Material {

    private static final long serialVersionUID = 1L;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    private TaxonomyLevel level;
    private List<Taxonomy> taxHierachy = new ArrayList<>();

    public Taxonomy(int id,
                    Integer projectId,
                    List<MaterialName> names,
                    List<Taxonomy> hierarchy,
                    User owner,
                    Date creationDate,
                    ACList acList) {
        super(id, names, projectId, new HazardInformation(), new StorageInformation());
        this.setOwner(owner);
        this.creationTime = creationDate;
        this.type = MaterialType.TAXONOMY;
        this.taxHierachy = hierarchy;
        setACList(acList);

    }

    public Taxonomy(int id,
                    List<MaterialName> names,
                    HazardInformation hazards,
                    StorageInformation storageInformation,
                    List<Taxonomy> hierarchy,
                    User owner,
                    Date creationDate) {
        super(id, names, null, hazards, storageInformation);
        this.setOwner(owner);
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
        Taxonomy copiedTaxonomy = new Taxonomy(id, copiedNames, hazards, storageInformation, taxHierachy, getOwner(), creationTime);
        copiedTaxonomy.setTaxHierachy(new ArrayList<>());
        for (Taxonomy t : taxHierachy) {
            copiedTaxonomy.getTaxHierarchy().add(t);
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

    public List<Taxonomy> getTaxHierarchy() {
        return taxHierachy;
    }

    public void setTaxHierachy(List<Taxonomy> taxHierachy) {
        this.taxHierachy = taxHierachy;
    }

    public void setLevel(TaxonomyLevel level) {
        this.level = level;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Taxonomy)) {
            return false;
        }
        Taxonomy otherUser = (Taxonomy) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.TAXONOMY);
    }

}
