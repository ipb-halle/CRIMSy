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
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.exception.ExceptionUtils;
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
        super(id, names, null, new HazardInformation(), new StorageInformation());
        this.taxonomy = taxo;
        this.type = MaterialType.TISSUE;
    }

    @Override
    public Tissue copyMaterial() {
        try {
            return new Tissue(
                    id,
                    getCopiedNames(),
                    taxonomy == null ? null : taxonomy.copyMaterial());
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
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

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.TISSUE);
    }

}
