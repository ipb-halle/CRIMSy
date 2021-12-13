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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.lbac.material.common.Invalidity;
import de.ipb_halle.lbac.material.common.MaterialNameValidator;
import de.ipb_halle.lbac.material.common.MaterialValidator;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator for {@link Sequence} materials. A sequence is valid if its names
 * are valid and its sequence type is selected.
 *
 * @author fmauz
 */
public class SequenceValidator implements MaterialValidator {
    private Set<Invalidity> errors = new HashSet<>();

    @Override
    public boolean checkValidity(MaterialBean bean) {
        errors.clear();
        MaterialNameValidator nameValidator = new MaterialNameValidator();
        boolean namesValid = nameValidator.checkValidity(bean);
        if (!namesValid) {
            errors.addAll(nameValidator.getInvalidities());
        }

        boolean sequenceValid = true;
        if (!bean.getSequenceInfos().isSequenceTypeSelected()) {
            sequenceValid = false;
            errors.add(Invalidity.NO_SEQUENCETYPE_CHOSEN);
        }

        return namesValid && sequenceValid;
    }

    @Override
    public Set<Invalidity> getInvalidities() {
        return errors;
    }
}
