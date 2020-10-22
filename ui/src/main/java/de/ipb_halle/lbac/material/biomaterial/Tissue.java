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
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.MaterialType;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Tissue extends Material {

    protected Taxonomy taxonomy;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public Tissue(
            int id,
            List<MaterialName> names,
            Taxonomy taxo
    ) {
        super(id, names, null, new HazardInformation(), new StorageClassInformation());
        this.taxonomy = taxo;
        this.type = MaterialType.TISSUE;
    }

    @Override
    public String getNumber() {
        return "";
    }

    @Override
    public Tissue copyMaterial() {
        getCopiedNames();
        try {
            return new Tissue(
                    id,
                    getCopiedNames(),
                    taxonomy == null ? null : taxonomy.copyMaterial());
        } catch (Exception e) {
            logger.info("Error at copiing tissue");
        }
        return null;
    }

    @Override
    public TissueEntity createEntity() {
        TissueEntity entity = new TissueEntity();
        entity.setId(id);
        entity.setTaxoid(taxonomy.getId());
        return entity;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Tissue)) {
            return false;
        }
        Tissue otherUser = (Tissue) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

}
