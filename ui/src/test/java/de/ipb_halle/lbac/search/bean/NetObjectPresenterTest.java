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
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
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
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class NetObjectPresenterTest extends TestBase {

    private List<NetObject> netObjects;

    private NetObjectFactory netObjectFactory = new NetObjectFactory();
    private NetObjectPresenter presenter = new NetObjectPresenter();

    @Before
    @Override
    public void setUp() {
        super.setUp();
        netObjects = netObjectFactory.createNetObjects();
    }

    @Test
    public void test001_getName() {
        Assert.assertEquals("localDoc", presenter.getName(netObjects.get(0)));
        Assert.assertEquals("remoteDoc", presenter.getName(netObjects.get(1)));
        Assert.assertEquals("local User", presenter.getName(netObjects.get(2)));
        Assert.assertEquals("remote User", presenter.getName(netObjects.get(3)));
        Assert.assertEquals("localProject", presenter.getName(netObjects.get(4)));
        Assert.assertEquals("remoteProject", presenter.getName(netObjects.get(5)));
        Assert.assertEquals("localStructure", presenter.getName(netObjects.get(6)));
        Assert.assertEquals("remoteStructure", presenter.getName(netObjects.get(7)));
        Assert.assertEquals("12014 (localStructure)", presenter.getName(netObjects.get(8)));
        Assert.assertEquals("12014 (remoteStructure)", presenter.getName(netObjects.get(9)));
        Assert.assertEquals("localExp", presenter.getName(netObjects.get(10)));
        Assert.assertEquals("remoteExp", presenter.getName(netObjects.get(11)));
    }

    @Test
    public void test002_getNodeName() {
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(0)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(1)));
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(2)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(3)));
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(4)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(5)));
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(6)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(7)));
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(8)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(9)));
        Assert.assertEquals("local", presenter.getNodeName(netObjects.get(10)));
        Assert.assertEquals("remote", presenter.getNodeName(netObjects.get(11)));
    }

    @Test
    public void test003_getLink() throws UnsupportedEncodingException {
        String expectedPath = "/ui/servlet/document/GET?nodeId="
                + netObjects.get(0).getNode().getId().toString()
                + "&collectionId=1&contentType=pdf&originalName=localDoc&path=%2Fpath";
        Assert.assertEquals(expectedPath, presenter.getLink(netObjects.get(0)));
        expectedPath = "/ui/servlet/document/GET?nodeId="
                + netObjects.get(1).getNode().getId().toString()
                + "&collectionId=1&contentType=pdf&originalName=remoteDoc&path=%2Fpath";
        Assert.assertEquals(expectedPath, presenter.getLink(netObjects.get(1)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(2)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(3)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(4)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(5)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(6)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(7)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(8)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(9)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(10)));
        Assert.assertEquals("-", presenter.getLink(netObjects.get(11)));
    }

    @Test
    @Ignore
    public void test004_getToolTip() {
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(0)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(1)));
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(2)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(3)));
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(4)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(5)));
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(6)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(7)));
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(8)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(9)));
        Assert.assertEquals("local", presenter.getToolTip(netObjects.get(10)));
        Assert.assertEquals("remote", presenter.getToolTip(netObjects.get(11)));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("NetObjectPresenterTest.war")
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
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(ExperimentService.class)
                .addClass(TaxonomyNestingService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

}
