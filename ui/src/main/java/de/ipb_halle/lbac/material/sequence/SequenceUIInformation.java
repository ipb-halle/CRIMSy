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

import de.ipb_halle.lbac.globals.NavigationConstants;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.service.MaterialUIInformation;

/**
 * UI information for the material type {@link MaterialType#SEQUENCE}.
 * 
 * @author flange
 */
public class SequenceUIInformation implements MaterialUIInformation {
    @Override
    public String getLinkPreviewTemplate() {
        return "material/components/linkPreview/sequence" + NavigationConstants.TEMPLATE_EXT;
    }
}