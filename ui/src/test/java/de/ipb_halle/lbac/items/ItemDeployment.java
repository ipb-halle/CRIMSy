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
package de.ipb_halle.lbac.items;

import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.device.job.PrintJobService;
import de.ipb_halle.lbac.device.print.LabelService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.device.print.PrinterService;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemLabelService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.reporting.ReportJobService;
import de.ipb_halle.lbac.reporting.ReportMgr;
import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import de.ipb_halle.reporting.ReportService;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author fmauz
 */
public class ItemDeployment {

    public static WebArchive add(WebArchive deployment) {
        WebArchive d = deployment
                .addClass(ItemService.class)
                .addClass(ArticleService.class)
                .addClass(ContainerService.class)
                .addClass(ContainerNestingService.class)
                .addClass(ContainerPositionService.class)
                .addClass(ItemOverviewBean.class)
                .addClass(ItemBean.class)
                .addClass(ItemLabelService.class)
                .addClass(PrintBean.class)
                .addClass(PrintJobService.class)
                .addClass(PrinterService.class)
                .addClass(LabelService.class)
                .addClass(PreferenceService.class)
                .addClass(MaterialService.class)
                .addClass(ReportMgr.class)
                .addClass(ReportService.class)
                .addClass(ReportJobService.class)
                .addClass(SendFileBeanMock.class);
        return MaterialDeployment.add(d);
    }
}
