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
package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.material.common.MaterialNameValidator;
import de.ipb_halle.lbac.material.common.MaterialSaver;
import de.ipb_halle.lbac.material.common.MaterialValidator;
import de.ipb_halle.lbac.material.common.history.IMaterialComparator;
import de.ipb_halle.lbac.material.common.history.StructureComparator;
import de.ipb_halle.lbac.material.common.service.MaterialFactory;
import de.ipb_halle.lbac.material.common.service.MaterialLoader;
import de.ipb_halle.lbac.material.common.service.MaterialUIInformation;

/**
 *
 * @author fmauz
 */
public class StructureFactory implements MaterialFactory {

    private static final long serialVersionUID = 1L;

    StructureInformationSaver saver = new StructureInformationSaver();

    @Override
    public MaterialSaver createSaver() {
        return saver;
    }

    @Override
    public MaterialLoader createLoader() {
        return new StructureLoader();
    }

    public void setSaver(StructureInformationSaver saver) {
        this.saver = saver;
    }

    @Override
    public IMaterialComparator createComparator() {
        return new StructureComparator();
    }

    @Override
    public MaterialValidator createValidator() {
       return new MaterialNameValidator();
    }

    @Override
    public MaterialUIInformation createUIInformation() {
        return new StructureUIInformation();
    }
}
