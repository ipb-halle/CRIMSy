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
package de.ipb_halle.lbac.material.common;

import static de.ipb_halle.lbac.material.common.Invalidity.EMPTY_MATERIAL_NAME;
import static de.ipb_halle.lbac.material.common.Invalidity.NO_MATERIAL_NAME;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class MaterialNameValidator {

    private Set<Invalidity> errors = new HashSet<>();
    private List<MaterialName> names;

    public boolean areMaterialNamesValid(
            List<MaterialName> names,
            Set<Invalidity> errors) {
        this.names = names;
        this.errors=errors;

        return checkNameExistance() && areAllNamesNotEmpty();
    }

    private boolean checkNameExistance() {
        if (names.isEmpty()) {
            errors.add(NO_MATERIAL_NAME);
            return false;
        }
        return true;
    }

    private boolean areAllNamesNotEmpty() {
        for (MaterialName mn : names) {
            if (mn.getValue().isEmpty()) {
                errors.add(EMPTY_MATERIAL_NAME);
                return false;
            }
        }
        return true;
    }

}
