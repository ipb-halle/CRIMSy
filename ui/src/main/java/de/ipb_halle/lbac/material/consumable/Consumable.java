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
package de.ipb_halle.lbac.material.consumable;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class Consumable extends Material {

    public Consumable(
            int id,
            List<MaterialName> names,
            int projectId,
            HazardInformation hazards,
            StorageInformation storageInfos) {
        super(id, names, projectId, hazards, storageInfos);
        this.type = MaterialType.CONSUMABLE;
    }

    @Override
    public String getNumber() {
        return "";
    }

    @Override
    public Material copyMaterial() {
        Consumable c= new Consumable(id, names, projectId, hazards, storageInformation);
        List<MaterialName> copiedNames=new ArrayList<>();
        for(MaterialName n:names){
            copiedNames.add(new MaterialName((n.getValue()), n.getLanguage(), n.getRank()));
        }
        c.setCreationTime(creationTime);
        c.setNames(copiedNames);
        c.setACList(getACList());
        c.setOwner(getOwner());
        return c;
    }

    @Override
    public Object createEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (!(other instanceof Consumable)) {
            return false;
        }
        Consumable otherUser = (Consumable) other;
        return Objects.equals(otherUser.getId(), this.getId());
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.CONSUMABLE);
    }

}
