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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.TESTCLOUD;
import static de.ipb_halle.lbac.base.TestBase.TEST_NODE_ID;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.RemoteExperiment;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.RemoteMaterial;
import de.ipb_halle.lbac.search.bean.SearchFilter;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.mocks.SearchWebServiceMock;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SearchWebClientTest extends TestBase {

    @Inject
    SearchWebClient searchWebClient;

    private User publicUser;

    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();

        publicUser = context.getPublicAccount();

    }

    @Test
    @RunAsClient
    public void test001_getRemoteResults() {
        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);

        SearchResult result = searchWebClient.getRemoteSearchResult(cn, publicUser, new ArrayList<>());
        Assert.assertEquals(3, result.getAllFoundObjects().size());
        RemoteMaterial material = (RemoteMaterial) result.getAllFoundObjects().get(0).getSearchable();
        Assert.assertEquals(20, material.getId());
        Assert.assertEquals("INDEX-1", material.getIndices().get(1));
        Assert.assertEquals("MOLECULE", material.getMoleculeString());
        Assert.assertEquals("STRCUTURE-1", material.getNameToDisplay());
        Assert.assertEquals(2, material.getNames().size());
        Assert.assertEquals("CO2", material.getSumFormula());
        Assert.assertEquals(SearchTarget.MATERIAL, material.getType().getGeneralType());
        Assert.assertEquals(MaterialType.STRUCTURE, material.getType().getMaterialType());
        Assert.assertEquals(SearchTarget.MATERIAL, material.getTypeToDisplay().getGeneralType());
        Assert.assertEquals(MaterialType.STRUCTURE, material.getTypeToDisplay().getMaterialType());

        RemoteItem item = (RemoteItem) result.getAllFoundObjects().get(1).getSearchable();
        Assert.assertEquals(10d, item.getAmount(), 0);
        Assert.assertEquals("RemoteItem-desc", item.getDescription());
        Assert.assertEquals(100, item.getId());
        Assert.assertEquals("RemoteItemMaterialName", item.getMaterialName());
        Assert.assertEquals("100 (RemoteItemMaterialName)", item.getNameToDisplay());
        Assert.assertEquals("remoteItemProjectName", item.getProjectName());
        Assert.assertEquals(SearchTarget.ITEM, item.getTypeToDisplay().getGeneralType());
        Assert.assertEquals("remoteItemUnit", item.getUnit());

        RemoteExperiment exp = (RemoteExperiment) result.getAllFoundObjects().get(2).getSearchable();
    }

 
    @Test
    @RunAsClient
    public void test002_serialisation() throws Exception {
        SearchFilter filter = new SearchFilter(publicUser);

        JAXBContext jaxContext = JAXBContext.newInstance(SearchWebRequest.class);
        Marshaller jaxbMarshaller = jaxContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        SearchWebRequest request = new SearchWebRequest();
        filter.setSearchTerms("Phenol");

        List<SearchRequest> requests = filter.createRequests();
        List<SearchRequestImpl> impls = new ArrayList<>();
        for (SearchRequest r : requests) {
            impls.add((SearchRequestImpl) r);
        }
        request.addRequest(impls);
        File f = new File("target/searchWebRequest.xml");
        jaxbMarshaller.marshal(request, f);

        JAXBContext jaxbContext = JAXBContext.newInstance(SearchWebRequest.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        SearchWebRequest unserialisedRequest = (SearchWebRequest) jaxbUnmarshaller.unmarshal(new FileInputStream(f));
      
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchWebClientTest.war")
                .addClass(SearchService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(FileEntityService.class)
                .addClass(ExperimentService.class)
                .addClass(SearchWebService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(SearchWebClient.class)
                .addClass(SearchWebServiceMock.class)
                .addClass(TextService.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }
}
