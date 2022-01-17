/*
 * Cloud Resource & Information Management System (CRIMSy)
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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectEditBean;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author fmauz
 */
public class MaterialBeanDeployment {

    public static WebArchive add(WebArchive deployment) {
        return deployment.addClass(MaterialBean.class)
                .addClass(MaterialNameBean.class)
                .addClass(MaterialOverviewBean.class)
                .addClass(MaterialIndexBean.class)
                .addClass(ProjectEditBean.class)
                .addClass(ItemBean.class)
                .addClass(ItemOverviewBean.class)
                .addClass(ProjectBean.class)
                .addClass(SequenceSearchService.class)
                .addClass(MaterialCompositionBean.class);
    }
}
