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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.util.Arrays;
import java.util.List;
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
public class SearchBeanTest extends TestBase {

    private NetObjectFactory factory = new NetObjectFactory();
    private List<NetObject> netObjects;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        netObjects = factory.createNetObjects();
    }

    @Test
    public void test001_actionAddFoundObjectsToShownObjects() {
        SearchBean bean = new SearchBean();
        bean.getSearchState().addNoteToSearch(netObjects.get(0).getNode().getId());
        Assert.assertTrue(bean.isSearchActive());
        bean.getSearchState().addNetObjects(Arrays.asList(
                netObjects.get(0),
                netObjects.get(2),
                netObjects.get(4),
                netObjects.get(6)));
        Assert.assertFalse(bean.isSearchActive());
        Assert.assertEquals(4, bean.getUnshownButFoundObjects());
        Assert.assertEquals(0, bean.getShownObjects().size());
        Assert.assertEquals(0, bean.getShownObjects().size());
        bean.actionAddFoundObjectsToShownObjects();
        Assert.assertEquals(4, bean.getShownObjects().size());
        Assert.assertEquals(0, bean.getUnshownButFoundObjects());

        bean.getSearchState().addNetObjects(Arrays.asList(
                netObjects.get(0)));
        Assert.assertFalse(bean.isSearchActive());
        bean.actionAddFoundObjectsToShownObjects();
        Assert.assertEquals(4, bean.getShownObjects().size());
        Assert.assertEquals(0, bean.getUnshownButFoundObjects());
        
        Assert.assertEquals("localDoc",bean.getNetObjectPresenter().getName(netObjects.get(0)));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchBeanTest.war")
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(MoleculeService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(FileEntityService.class)
                .addClass(ExperimentService.class)
                .addClass(TaxonomyNestingService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

}
