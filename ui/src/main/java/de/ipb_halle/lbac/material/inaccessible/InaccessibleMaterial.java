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
package de.ipb_halle.lbac.material.inaccessible;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class InaccessibleMaterial extends Material {

    public InaccessibleMaterial(List<MaterialName> nameList, ACList aclist) {
        super(-1, nameList, null, new HazardInformation(), new StorageClassInformation());
        this.setType(MaterialType.INACCESSIBLE);
        this.setACList(aclist);
    }

    public static InaccessibleMaterial createNewInstance(ACList aclist) {
        List<MaterialName> nameList = new ArrayList<>();
        nameList.add(new MaterialName("inaccessible", "en", 0));
        return new InaccessibleMaterial(nameList, aclist);

    }

    @Override
    public Material copyMaterial() {
        return this;
    }

    @Override
    public Object createEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEqualTo(Object other) {
       return false;
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.INACCESSIBLE);
    }

}
