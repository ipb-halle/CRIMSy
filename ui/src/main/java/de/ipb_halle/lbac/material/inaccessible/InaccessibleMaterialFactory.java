/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.material.common.MaterialSaver;
import de.ipb_halle.lbac.material.common.MaterialValidator;
import de.ipb_halle.lbac.material.common.history.IMaterialComparator;
import de.ipb_halle.lbac.material.common.service.MaterialFactory;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;
import de.ipb_halle.lbac.material.common.service.MaterialUIInformation;

/**
 * 
 * @author flange
 */
public class InaccessibleMaterialFactory implements MaterialFactory {
    private static final long serialVersionUID = 1L;

    @Override
    public MaterialSaver createSaver() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public MaterialLoader createLoader() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public IMaterialComparator createComparator() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public MaterialValidator createValidator() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public MaterialUIInformation createUIInformation() {
        return new MaterialUIInformation() {
        };
    }
}
