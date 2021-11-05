/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.MaterialType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author fmauz
 */
public enum CompositionType implements Serializable {

    EXTRACT(
            MaterialType.BIOMATERIAL,
            MaterialType.INACCESSIBLE,
            MaterialType.STRUCTURE
    ),
    PROTEIN(
            MaterialType.SEQUENCE,
            MaterialType.INACCESSIBLE,
            MaterialType.BIOMATERIAL
    ),
    MIXTURE(MaterialType.STRUCTURE,
            MaterialType.INACCESSIBLE);

    private final List<MaterialType> allowedMaterialTypes;

    CompositionType(MaterialType... types) {
        allowedMaterialTypes = new ArrayList<>();
        allowedMaterialTypes.addAll(Arrays.asList(types));
    }

    public List<MaterialType> getAllowedTypes() {
        return allowedMaterialTypes;
    }

}
