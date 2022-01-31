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
package de.ipb_halle.lbac.material.common.service;

import de.ipb_halle.lbac.globals.NavigationConstants;

/**
 * UI information for a specific material.
 *
 * @author flange
 */
public interface MaterialUIInformation {
    /**
     * Path of the facelet template (a {@code ui:composition}) relative to
     * {@link NavigationConstants#TEMPLATE_FOLDER} that is {@code ui:include}d in
     * the link preview dialog for this material.
     * 
     * @return path of the facelet template, not {@code null}
     */
    public default String getLinkPreviewTemplate() {
        return "material/components/linkPreview/noMaterial" + NavigationConstants.TEMPLATE_EXT;
    }
}