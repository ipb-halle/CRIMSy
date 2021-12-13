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

import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import java.util.Set;

/**
 * Validator for a material.
 *
 * @author fmauz
 */
public interface MaterialValidator {
    /**
     * Check the validity of the material that is currently in
     * {@link MaterialBean}'s edit state.
     * 
     * @param bean
     * @return validation result
     */
    public boolean checkValidity(MaterialBean bean);

    /**
     * @return errors from last call of {@link #checkValidity(MaterialBean)} or
     *         empty set if this method was not called before
     */
    public Set<Invalidity> getInvalidities();
}
