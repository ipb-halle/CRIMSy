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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.search.SearchTarget;

/**
 *
 * @author fmauz
 */
public class Type {

    private SearchTarget generalType;
    private MaterialType materialType;

    public Type() {
    }

    public void setGeneralType(SearchTarget generalType) {
        this.generalType = generalType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public Type(SearchTarget generalType) {
        this.generalType = generalType;
    }

    public Type(SearchTarget generalType, MaterialType materialType) {
        this.generalType = generalType;
        this.materialType = materialType;
    }

    public String getTypeName() {
        if (generalType == SearchTarget.MATERIAL) {
            return materialType.toString();
        } else {
            return generalType.toString();
        }
    }

    public SearchTarget getGeneralType() {
        return generalType;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

}
