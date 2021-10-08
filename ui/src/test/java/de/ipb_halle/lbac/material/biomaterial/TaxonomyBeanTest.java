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
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.bean.MaterialEditSaver;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.TaxonomyBeanMock;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import java.util.HashMap;
import java.util.List;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.BehaviorBase;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
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
    private TaxonomyNestingService taxoNestingService;
    @Inject
    private MaterialService materialService;

    protected TaxonomyBeanMock bean;

    protected User owner;
    protected TreeNode nodeToOperateOn;

    @Before
    public void init() {
        Integer userGroups = GlobalAdmissionContext.getPublicReadACL().getId();
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        createTaxonomyTreeInDB(userGroups, owner.getId());
        bean = new TaxonomyBeanMock();
        bean.setTaxonomyService(taxonomyService);
        bean.init(memberService, taxonomyService);

        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        materialService.setEditedMaterialSaver(new MaterialEditSaver(materialService, taxoNestingService));
        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
        bean.setMaterialService(materialService);

    }

    @Test
    public void test001_expandCollapseTaxonomies() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode();
        assertNotExpanded("Pilze_de");
        NodeExpandEvent expandEvent = createExpandEvent("Pilze_de");
        bean.onTaxonomyExpand(expandEvent);
        assertExpanded("Pilze_de");
        assertNotExpanded("Champignonartige_de");
        bean.onTaxonomyCollapse(createCollapseEvent("Pilze_de"));
        assertNotExpanded("Champignonartige_de");
    }

    @Test
    public void test002_editTaxonomy() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode();
        Assert.assertTrue(bean.getRenderController().isFirstButtonDisabled());
        bean.onTaxonomyExpand(createExpandEvent("Pilze_de"));
        bean.onTaxonomyExpand(createExpandEvent("Agaricomycetes_de"));
        bean.onTaxonomySelect(createSelectEvent("Champignonartige_de"));

        Assert.assertFalse(bean.getRenderController().isFirstButtonDisabled());
        Assert.assertTrue(bean.getRenderController().isHistoryVisible());
        Assert.assertFalse(bean.getRenderController().isParentVisible());
        bean.actionClickFirstButton();

        Assert.assertFalse(bean.getRenderController().isFirstButtonDisabled());
        Assert.assertFalse(bean.getRenderController().isHistoryVisible());
        assertNotSelectable("Champignonartige_de");
        assertNotSelectable("Wulstlingsverwandte_de");
        assertNotSelectable("Wulstlinge_de");
        assertNotSelectable("Schleimschirmlinge_de");
        assertSelectable("Agaricomycetes_de");
        assertSelectable("Dacrymycetes_de");
        assertSelectable("Pilze_de");
        assertSelectable("Pflanzen_de");
        assertSelectable("Bakterien_de");
        assertSelectable("Leben_de");
        bean.getRenderController().getCategoryOfChoosenTaxo();
        Assert.assertEquals(1, bean.getLevelController().getLevels().size());

        bean.getNameController().addNewName();
        bean.getNameController().getNames().get(1).setValue("Champignonartige_de_edited");

        bean.actionClickFirstButton();

        assertSelectable("Champignonartige_de");
        assertSelectable("Wulstlingsverwandte_de");
        assertSelectable("Wulstlingsverwandte_de");
        assertSelectable("Wulstlinge_de");
        assertSelectable("Schleimschirmlinge_de");
        assertSelectable("Agaricomycetes_de");
        assertSelectable("Dacrymycetes_de");
        assertSelectable("Pilze_de");
        assertSelectable("Pflanzen_de");
        assertSelectable("Bakterien_de");
        assertSelectable("Leben_de");
        Assert.assertEquals(TaxonomyBean.Mode.SHOW, bean.getMode());

        bean.onTaxonomySelect(createSelectEvent("Champignonartige_de"));
        bean.actionClickFirstButton();

        bean.getNameController().addNewName();
        Assert.assertEquals(2, bean.getNameController().getNames().size());
        bean.getNameController().getNames().get(1).setValue("Champignonartige_de_edited");
        bean.getNameController().swapPosition(bean.getNameController().getNames().get(1), "HIGHER");

        bean.actionClickSecondButton();
        //bean.onTaxonomySelect(createSelectEvent("Champignonartige_de_edited"));

    }

    @Test
    public void test003_newTaxonomy() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode();

        bean.actionClickSecondButton();
        bean.onTaxonomyExpand(createExpandEvent("Pilze_de"));
        bean.onTaxonomyExpand(createExpandEvent("Agaricomycetes_de"));
        bean.onTaxonomySelect(createSelectEvent("Champignonartige_de"));

        Assert.assertEquals("Champignonartige_de", bean.getRenderController().getParentFirstName());
        bean.getRenderController().getLabelForParentTaxonomy();
        Assert.assertEquals(16, bean.getLevelController().getLevels().size());
        Assert.assertEquals(600, (int) bean.getLevelController().getLevels().get(0).getRank());

        bean.nameController.getNames().get(0).setValue("test003_de");
        bean.nameController.getNames().get(0).setLanguage("de");
        bean.nameController.addNewName();
        bean.nameController.getNames().get(1).setLanguage("en");
        bean.nameController.getNames().get(1).setValue("test003_en");
        bean.getLevelController().setSelectedLevel(bean.getLevelController().getLevels().get(0));

        List<Taxonomy> taxos = taxonomyService.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals(21, taxos.size());
        bean.actionClickSecondButton();

        taxos = taxonomyService.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals(22, taxos.size());

        nodeToOperateOn = null;
        createSelectEvent("test003_de");

        Taxonomy newTaxo = (Taxonomy) nodeToOperateOn.getData();
        Assert.assertNotNull(newTaxo);

        bean.actionClickSecondButton();
        bean.nameController.getNames().get(0).setValue("toCancel");
        bean.nameController.getNames().get(0).setLanguage("en");
        bean.getLevelController().setSelectedLevel(bean.getLevelController().getLevels().get(0));
        bean.actionClickFirstButton();
        taxos = taxonomyService.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals(22, taxos.size());
        Assert.assertEquals(TaxonomyBean.Mode.SHOW, bean.getMode());
    }

    private void assertNotSelectable(String nameOfTaxo) {
        selectTaxonomyFromTree(nameOfTaxo, bean.getTreeController().getTaxonomyTree());
        Assert.assertFalse(nodeToOperateOn.isSelectable());
    }

    private void assertSelectable(String nameOfTaxo) {
        selectTaxonomyFromTree(nameOfTaxo, bean.getTreeController().getTaxonomyTree());
        Assert.assertTrue(nodeToOperateOn.isSelectable());
    }

    private void assertNotExpanded(String nameOfTaxo) {
        selectTaxonomyFromTree(nameOfTaxo, bean.getTreeController().getTaxonomyTree());
        Assert.assertFalse(nodeToOperateOn.isExpanded());
    }

    private void assertExpanded(String nameOfTaxo) {
        selectTaxonomyFromTree(nameOfTaxo, bean.getTreeController().getTaxonomyTree());
        Assert.assertTrue(nodeToOperateOn.isExpanded());
    }

    private NodeSelectEvent createSelectEvent(String nameOfTaxToSelect) {
        nodeToOperateOn = null;
        selectTaxonomyFromTree(nameOfTaxToSelect, bean.getTreeController().getTaxonomyTree());
        if (nodeToOperateOn == null) {
            throw new RuntimeException("Could not find " + nameOfTaxToSelect + " in tree");
        }
        return new NodeSelectEvent(
                new UIViewRoot(),
                new BehaviorBase(),
                nodeToOperateOn
        );
    }

    private NodeExpandEvent createExpandEvent(String nameOfTaxToSelect) {
        nodeToOperateOn = null;
        selectTaxonomyFromTree(nameOfTaxToSelect, bean.getTreeController().getTaxonomyTree());
        if (nodeToOperateOn == null) {
            throw new RuntimeException("Could not find " + nameOfTaxToSelect + " in tree");
        }
        return new NodeExpandEvent(
                new UIViewRoot(),
                new BehaviorBase(),
                nodeToOperateOn
        );
    }

    private NodeCollapseEvent createCollapseEvent(String nameOfTaxToSelect) {
        nodeToOperateOn = null;
        selectTaxonomyFromTree(nameOfTaxToSelect, bean.getTreeController().getTaxonomyTree());
        if (nodeToOperateOn == null) {
            throw new RuntimeException("Could not find " + nameOfTaxToSelect + " in tree");
        }
        return new NodeCollapseEvent(
                new UIViewRoot(),
                new BehaviorBase(),
                nodeToOperateOn
        );
    }

    private void selectTaxonomyFromTree(String name, TreeNode tree) {
        Taxonomy taxo = (Taxonomy) tree.getData();
        if (taxo.getFirstName().equals(name)) {
            nodeToOperateOn = tree;
        }
        for (TreeNode n : tree.getChildren()) {
            selectTaxonomyFromTree(name, n);
        }
    }

    private TreeNode getTree() {
        return bean.getTreeController().getTaxonomyTree();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("TaxonomyBeanTest.war");
        return MaterialDeployment.add(UserBeanDeployment.add(deployment));
    }
}
