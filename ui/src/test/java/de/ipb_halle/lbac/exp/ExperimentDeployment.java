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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.datalink.LinkedDataAgent;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.images.ImageService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MaterialDeployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author fmauz
 */
public class ExperimentDeployment {

    public static WebArchive add(WebArchive deployment) {
        WebArchive d = deployment
                .addClass(ExpRecordService.class)
                .addClass(TextService.class)
                .addClass(AssayService.class)
                .addClass(ItemService.class)
                .addClass(LinkedDataAgent.class)
                .addClass(ExperimentBean.class)
                .addClass(ExperimentService.class)
                .addClass(ImageService.class)
                .addClass(ItemAgent.class)
                .addClass(MaterialAgent.class);
        return MaterialDeployment.add(d);
    }
}
