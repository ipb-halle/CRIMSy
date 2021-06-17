/*
 * CRIMSy 
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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;

import java.util.List;

/**
 * Interface for classes holding a single material and having that material
 * assigned / changed by a MaterialAgent
 *
 * @author fbroda
 */
public interface MaterialHolder {

    /**
     * @return the currently active material
     */
    public Material getMaterial();

    /**
     * @return an array of acceptable material types in the current situation.
     * The return value of this method may depend on the current field.
     */
    public List<MaterialType> getMaterialTypes();

    /**
     * @param material the material chosen by the user
     */
    public void setMaterial(Material material);

}
