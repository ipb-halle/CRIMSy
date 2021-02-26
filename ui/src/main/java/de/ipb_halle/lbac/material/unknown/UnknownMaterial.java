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
package de.ipb_halle.lbac.material.unknown;

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
public class UnknownMaterial extends Material {

    public UnknownMaterial(int fakeId, List<MaterialName> nameList,ACList aclist) {
        super(fakeId, nameList, null, new HazardInformation(), new StorageClassInformation());
        this.setType(MaterialType.UNKNOWN);
        this.setACList(aclist);
    }

    public static UnknownMaterial createNewInstance(ACList aclist) {
        int fakeId = (int) Math.random() * 100000000;
        List<MaterialName> nameList = new ArrayList<>();
        nameList.add(new MaterialName("unknown", "en", 0));
        return new UnknownMaterial(fakeId, nameList,aclist);

    }

    @Override
    public Material copyMaterial() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object createEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEqualTo(Object other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.MATERIAL, MaterialType.UNKNOWN);
    }

}
