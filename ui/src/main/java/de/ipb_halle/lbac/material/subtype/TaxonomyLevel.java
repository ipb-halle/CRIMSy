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
package de.ipb_halle.lbac.material.subtype;

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.material.entity.TaxonomyLevelEntity;

/**
 *
 * @author fmauz
 */
public class TaxonomyLevel implements DTO {

    private int id;
    private String name;

    public TaxonomyLevel(TaxonomyLevelEntity dbentity) {
        this.id = dbentity.getId();
        this.name = dbentity.getName();
    }

    @Override
    public Object createEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaxonomyLevel other = (TaxonomyLevel) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
