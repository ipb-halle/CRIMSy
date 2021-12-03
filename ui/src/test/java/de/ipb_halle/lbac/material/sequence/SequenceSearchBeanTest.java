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

import de.ipb_halle.fasta_search_service.models.search.TranslationTable;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.label.LabelService;
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.search.bean.SearchMode;
import de.ipb_halle.lbac.material.sequence.search.bean.SequenceSearchBean;
import de.ipb_halle.lbac.search.SearchOrchestrator;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.SearchWebClient;
import de.ipb_halle.lbac.search.bean.SearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import java.util.Arrays;
import java.util.HashMap;
import javax.inject.Inject;
import org.junit.Test;

/**
 * @author flange
 */
@RunWith(Arquillian.class)
public class SequenceSearchBeanTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private SequenceSearchBean searchBean;

    @Inject
    private MaterialService materialService;

    @Inject
    private FastaRESTSearchServiceMock serviceMock;

    @Test
    public void test001_search() {

        Sequence s = new Sequence(Arrays.asList(new MaterialName("xx", "de", 0)), null, SequenceData.builder().sequenceString("AAA").sequenceType(SequenceType.DNA).build());
        materialService.saveMaterialToDB(s, context.getAdminOnlyACL().getId(), new HashMap<>(), publicUser);       
        searchBean.getSearchMaskController().setQuery("AAA");
        searchBean.getSearchMaskController().setSearchMode(SearchMode.DNA_DNA);
        searchBean.getSearchMaskController().setTranslationTable(TranslationTable.EUPLOTID_NUCLEAR);
        searchBean.getResultsTableController().setLastUser(publicUser);
        searchBean.getSearchMaskController().actionStartMaterialSearch();
        int j = 0;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SequenceSearchBeanTest.war")
                .addClass(FastaSearchServiceEndpointMock.class);
        deployment = MaterialDeployment.add(deployment);
        deployment = ItemDeployment.add(deployment);
        deployment = ExperimentDeployment.add(deployment);
        deployment = PrintBeanDeployment.add(deployment);
        deployment.deleteClass(FastaRESTSearchService.class);
        deployment.addClass(FastaRESTSearchServiceMock.class);
        deployment.addClass(SearchBean.class);
        deployment.addClass(SequenceSearchBean.class);
        deployment.addClass(ItemBean.class);
        deployment.addClass(SearchWebClient.class);
        deployment.addClass(SearchService.class);
        deployment.addClass(PrintBean.class);
        deployment.addClass(DocumentSearchService.class);
        deployment.addClass(SearchOrchestrator.class);
        deployment.addClass(LabelService.class);
        deployment = MaterialBeanDeployment.add(deployment);
        return UserBeanDeployment.add(deployment);
    }

    @Before
    public void init() throws URISyntaxException {

    }

}
