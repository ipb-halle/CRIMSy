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

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.List;
import org.jboss.arquillian.container.test.api.Deployment;
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
public class NetObjectPresenterTest extends TestBase {

    private static final long serialVersionUID = 1L;

    private List<NetObject> netObjects;

    private NetObjectFactory netObjectFactory = new NetObjectFactory();
    private NetObjectPresenter presenter;

    @BeforeEach
    public void init() {
        netObjects = netObjectFactory.createNetObjects();
        presenter = new NetObjectPresenter(new User(), getMessagePresenterMock());
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
        Assert.assertEquals("100 (RemoteMaterial)", presenter.getName(netObjects.get(9)));
        Assert.assertEquals("localExp", presenter.getName(netObjects.get(10)));
        Assert.assertEquals("remoteExperiment", presenter.getName(netObjects.get(11)));
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
    public void test004_getToolTip() {
        Assert.assertNull(presenter.getToolTip(netObjects.get(0)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(1)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(2)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(3)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(4)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(5)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(6)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(7)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(8)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(9)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(10)));
        Assert.assertNull(presenter.getToolTip(netObjects.get(11)));
    }

    @Test
    public void test005_isDownloadLinkVisible() {
        Assert.assertTrue(presenter.isDownloadLinkVisible(netObjects.get(0)));
        Assert.assertTrue(presenter.isDownloadLinkVisible(netObjects.get(1)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(2)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(3)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(4)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(5)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(6)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(7)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(8)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(9)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(10)));
        Assert.assertFalse(presenter.isDownloadLinkVisible(netObjects.get(11)));
    }

    @Test
    public void test005_isInternalLinkVisible() {
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(0)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(1)));
        Assert.assertTrue(presenter.isInternalLinkVisible(netObjects.get(2)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(3)));
        Assert.assertTrue(presenter.isInternalLinkVisible(netObjects.get(4)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(5)));
        Assert.assertTrue(presenter.isInternalLinkVisible(netObjects.get(6)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(7)));
        Assert.assertTrue(presenter.isInternalLinkVisible(netObjects.get(8)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(9)));
        Assert.assertTrue(presenter.isInternalLinkVisible(netObjects.get(10)));
        Assert.assertFalse(presenter.isInternalLinkVisible(netObjects.get(11)));
    }

    @Test
    public void test006_isExternalLinkVisible() {
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(0)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(1)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(2)));
        Assert.assertTrue(presenter.isExternalLinkVisible(netObjects.get(3)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(4)));
        Assert.assertTrue(presenter.isExternalLinkVisible(netObjects.get(5)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(6)));
        Assert.assertTrue(presenter.isExternalLinkVisible(netObjects.get(7)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(8)));
        Assert.assertTrue(presenter.isExternalLinkVisible(netObjects.get(9)));
        Assert.assertFalse(presenter.isExternalLinkVisible(netObjects.get(10)));
        Assert.assertTrue(presenter.isExternalLinkVisible(netObjects.get(11)));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("NetObjectPresenterTest.war")
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(DocumentSearchService.class)
                .addClass(CollectionService.class)
                .addClass(FileObjectService.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(ExperimentService.class)
                .addClass(TaxonomyNestingService.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(MaterialDeployment.add(deployment))));
    }

}
