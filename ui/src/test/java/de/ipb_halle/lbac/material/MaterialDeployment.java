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

import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.sequence.search.service.FastaRESTSearchService;
import de.ipb_halle.lbac.material.sequence.search.service.SearchParameterService;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import de.ipb_halle.lbac.util.reporting.ReportJobService;
import de.ipb_halle.lbac.util.reporting.ReportMgr;
import de.ipb_halle.lbac.util.reporting.ReportService;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 *
 * @author fmauz
 */
public class MaterialDeployment {

    public static WebArchive add(WebArchive deployment) {
        return deployment
                .addClass(ProjectService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(MaterialService.class)
                .addClass(IndexService.class)
                .addClass(FastaRESTSearchService.class)
                .addClass(MaterialCompositionBean.class)
                .addClass(HazardService.class)
                .addClass(SearchParameterService.class)
                .addClass(SequenceSearchService.class)
                .addClass(IndexService.class)
                .addClass(ReportMgr.class)
                .addClass(ReportService.class)
                .addClass(ReportJobService.class)
                .addClass(JobService.class)
                .addClass(SendFileBeanMock.class)
                .addClass(TaxonomyNestingService.class);
    }
}
