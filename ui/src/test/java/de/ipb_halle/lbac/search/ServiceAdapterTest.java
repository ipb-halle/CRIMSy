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
package de.ipb_halle.lbac.search;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.admission.User;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;

import java.util.ArrayList;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceSearchServiceMock;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.search.mocks.DocumentServiceMock;
import de.ipb_halle.lbac.search.mocks.ExperimentServiceMock;
import de.ipb_halle.lbac.search.mocks.ItemServiceMock;
import de.ipb_halle.lbac.search.mocks.MaterialServiceMock;
import de.ipb_halle.lbac.search.mocks.ProjectServiceMock;
import de.ipb_halle.lbac.service.NodeService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ServiceAdapterTest {

    private ServiceAdapter adapter;

    @Inject
    private NodeService nodeService;

    @Before
    public void init() {
        SequenceSearchServiceMock sequenceSearchServiceMock = new SequenceSearchServiceMock();
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResultImpl result = new SearchResultImpl();

            Sequence sequence = new Sequence(0, new ArrayList<>(), 1, new HazardInformation(), new StorageInformation(),
                    SequenceData.builder().build());

            SequenceAlignment alignent = new SequenceAlignment(sequence, new FastaResult());
            result.addResult(alignent);
            return result;
        });

        adapter = new ServiceAdapter(
                new ItemServiceMock(),
                new MaterialServiceMock(),
                new ProjectServiceMock(),
                new ExperimentServiceMock(),
                new DocumentServiceMock(),
                null,
                null,
                nodeService,
                sequenceSearchServiceMock);
    }

    @Test
    public void test001_navigateToMaterial() throws InterruptedException {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(new User(), 0, 1);
        builder.addMaterialType(MaterialType.SEQUENCE);

        SearchResult result = adapter.doSearch(builder.build());
        Assert.assertEquals(1, result.getAllFoundObjects(BioMaterial.class, nodeService.getLocalNode()).size());

        builder.setSequenceInformation("AAA", "DNA", SequenceType.DNA, 0);
        result = adapter.doSearch(builder.build());
        Assert.assertEquals(1, result.getAllFoundObjects(SequenceAlignment.class, nodeService.getLocalNode()).size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ServiceAdapterTest.war");
        deployment.addClass(NodeService.class);
        return deployment;
    }

}
