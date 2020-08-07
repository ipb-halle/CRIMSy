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
package de.ipb_halle.lbac.search.document;

/**
 *
 * @author fmauz
 */
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.util.HashSet;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DocumentSearchServiceTest extends TestBase {

    @Inject
    private DocumentSearchService instance;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("WordCloudWebServiceTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrSearcher.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(NodeService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(FileService.class)
                .addClass(MembershipService.class)
                .addClass(MemberService.class)
                .addClass(TermVectorEntityService.class);
    }

    @Test
    public void getTagStringForSeachRequestTest() {
        assertEquals("", instance.getTagStringForSeachRequest(null));
        assertEquals("", instance.getTagStringForSeachRequest(new HashSet<>()));
        HashSet<String> set = new HashSet<>();
        set.add("term1");
        assertEquals("term1", instance.getTagStringForSeachRequest(set));
        set.add("term2");
        //Because there is no order in Sets both concatinations are correct
        String result = instance.getTagStringForSeachRequest(set);
        assertTrue(
                "term1 AND term2".equals(result)
                || "term2 AND term1".equals(result));

    }
}
