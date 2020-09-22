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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.mocks.MaterialEditSaverMock;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.structure.StructureInformationSaverMock;
import de.ipb_halle.lbac.project.ProjectService;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.BehaviorBase;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class TaxonomyBeanTest extends TestBase {

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;

    protected TaxonomyBean bean;

    protected User owner;
    protected TreeNode nodeToOperateOn;

    @Before
    public void init() {

        bean = new TaxonomyBean();
        bean.setTaxonomyService(taxonomyService);
        bean.init();

        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        materialService.setUserBean(userBean);
        materialService.setEditedMaterialSaver(new MaterialEditSaverMock(materialService));
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(materialService.getEm()));
        bean.setMaterialService(materialService);
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        Integer userGroups = GlobalAdmissionContext.getPublicReadACL().getId();
        createTaxonomyTreeInDB(userGroups, owner.getId());

    }

    @After
    public void finish() {

    }

    @Test
    public void test001_reloadTaxonomies() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode();
        TreeNode tree = bean.getTreeController().getTaxonomyTree();

        int i = 0;
    }

    @Test
    public void test002_editTaxonomy() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode();
        TreeNode tree = bean.getTreeController().getTaxonomyTree();

        bean.onTaxonomySelect(createSelectEvent("Pilze_de", tree));
        Taxonomy taxonomy = (Taxonomy) bean.getSelectedTaxonomy().getData();
        Assert.assertEquals("Pilze_de", taxonomy.getFirstName());

        bean.actionClickFirstButton();
        Assert.assertEquals(TaxonomyBean.Mode.EDIT, bean.getMode());
        bean.actionClickFirstButton();
        Assert.assertEquals(TaxonomyBean.Mode.SHOW, bean.getMode());

        bean.actionClickFirstButton();
        Assert.assertEquals(TaxonomyBean.Mode.EDIT, bean.getMode());
       
        bean.onTaxonomySelect(createSelectEvent("Leben_de", tree));

        bean.actionClickFirstButton();
        Assert.assertEquals(TaxonomyBean.Mode.SHOW, bean.getMode());

    }

    private NodeSelectEvent createSelectEvent(String nameOfTaxToSelect, TreeNode tree) {
        setTaxonomyFromTree(nameOfTaxToSelect, tree);

        return new NodeSelectEvent(
                new UIViewRoot(),
                new BehaviorBase(),
                nodeToOperateOn
        );
    }

    private void setTaxonomyFromTree(String name, TreeNode tree) {
        if (((Taxonomy) tree.getData()).getFirstName().equals(name)) {
            nodeToOperateOn = tree;
        }
        for (TreeNode n : tree.getChildren()) {
            setTaxonomyFromTree(name, n);
        }
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("TaxonomyBeanTest.war")
                .addClass(TaxonomyService.class)
                .addClass(TaxonomyBean.class)
                .addClass(MoleculeService.class)
                .addClass(TaxonomyService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(ProjectService.class)
                .addClass(TissueService.class)
                .addClass(MaterialService.class);
        return UserBeanDeployment.add(deployment);
    }
}
