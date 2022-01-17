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

import de.ipb_halle.lbac.material.common.bean.MaterialBean;

/**
 * Validator for materials that is valid if the material's names are valid.
 *
 * @author fmauz
 */
public class MaterialNameValidator implements MaterialValidator {
    private Set<Invalidity> errors = new HashSet<>();

    @Override
    public boolean checkValidity(MaterialBean bean) {
        errors.clear();
        boolean namesValid = areMaterialNamesValid(bean.getMaterialNameBean().getNames());

        return namesValid;
    }

    @Override
    public Set<Invalidity> getInvalidities() {
        return errors;
    }

    private boolean areMaterialNamesValid(List<MaterialName> names) {
        return checkNameExistance(names) && areAllNamesNotEmpty(names);
    }

    private boolean checkNameExistance(List<MaterialName> names) {
        if ((names == null) || names.isEmpty()) {
            errors.add(NO_MATERIAL_NAME);
            return false;
        }
        return true;
    }

    private boolean areAllNamesNotEmpty(List<MaterialName> names) {
        for (MaterialName name : names) {
            String value = name.getValue();
            if ((value == null) || value.trim().isEmpty()) {
                errors.add(EMPTY_MATERIAL_NAME);
                return false;
            }
        }
        return true;
    }
}
