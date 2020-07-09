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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class IndexEntry  implements Serializable{

    protected String value;
    protected int typeId;
    protected String language;
    

    public IndexEntry(
            int typeId,
            String value,
            String language) {
        this.value = value;
        this.typeId = typeId;
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        final IndexEntry other = (IndexEntry) obj;

        return (other.typeId == typeId);

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.typeId);
        return hash;
    }

    public MaterialIndexEntryEntity toDbEntity(int materialId, int rank) {
        MaterialIndexEntryEntity iee = new MaterialIndexEntryEntity();
        iee.setMaterialid(materialId);
        iee.setRank(rank);
        iee.setTypeid(typeId);
        iee.setValue(value);
        iee.setLanguage(language);
        return iee;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
