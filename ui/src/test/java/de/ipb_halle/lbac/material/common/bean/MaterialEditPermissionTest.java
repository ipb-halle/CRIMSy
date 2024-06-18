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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import static de.ipb_halle.lbac.material.common.MaterialDetailType.COMMON_INFORMATION;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.inject.Inject;
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
public class MaterialEditPermissionTest extends TestBase {

    private static final long serialVersionUID = 1L;
    @Inject
    private MaterialService materialService;
    @Inject
    private ACListService aclistService;
    @Inject
    private HazardService hazardService;
    private MaterialEditPermission permissionBean;
    private MaterialBeanMock materialBean;
    private UserBeanMock userBean;

    @BeforeEach
    public void init() {
        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        materialBean = new MaterialBeanMock(loggingProfiler);
        materialBean.setMessagePresenter(getMessagePresenterMock());
        materialBean.setAcListService(aclistService);
        materialBean.setHazardService(hazardService);
        materialBean.setMessagePresenter(getMessagePresenterMock());
        permissionBean = new MaterialEditPermission(materialBean);
        materialBean.setMode(MaterialBean.Mode.CREATE);
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        materialBean.setUserBean(userBean);
    }

    @Test
    public void test001_isDetailInformationEditable() {
        //Create material with public user as owner
        Structure s = createStructure(GlobalAdmissionContext.getPublicReadACL(), publicUser);
        materialBean.getMaterialEditState().setMaterialToEdit(s);
        materialBean.getMaterialEditState().setMaterialBeforeEdit(s);

        //In creation mode always returns true
        Assert.assertTrue(permissionBean.isDetailInformationEditable(COMMON_INFORMATION.toString()));

        //In EDIT mode returns true because current user is owner
        materialBean.setMode(MaterialBean.Mode.EDIT);
        Assert.assertTrue(permissionBean.isDetailInformationEditable(COMMON_INFORMATION.toString()));

        //In EDIT mode returns true because current user (admin) is in public ACL
        userBean.setCurrentAccount(adminUser);
        Assert.assertTrue(permissionBean.isDetailInformationEditable(COMMON_INFORMATION.toString()));

        //In EDIT mode returns false because current user(public) is not in adminOnlyAcl
        s.setACList(context.getAdminOnlyACL());
        s.setOwner(adminUser);
        userBean.setCurrentAccount(publicUser);
        Assert.assertFalse(permissionBean.isDetailInformationEditable(COMMON_INFORMATION.toString()));

        //In HISTORY mode no edit is allowed
        materialBean.setMode(MaterialBean.Mode.HISTORY);
        userBean.setCurrentAccount(adminUser);
        Assert.assertFalse(permissionBean.isDetailInformationEditable(COMMON_INFORMATION.toString()));
    }

    @Test
    public void test002_isDetailPanelVisible() {
        //Create material with public user as owner
        materialBean.setMode(MaterialBean.Mode.CREATE);

        //No materialType set
        Assert.assertFalse(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //Structure has the detail information and in creationmode is always visible
        materialBean.setCurrentMaterialType(MaterialType.STRUCTURE);
        Assert.assertTrue(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //Structure has not the taxonomy detailinformation
        materialBean.setCurrentMaterialType(MaterialType.STRUCTURE);
        Assert.assertFalse(permissionBean.isDetailPanelVisible(MaterialDetailType.TAXONOMY.toString()));

        //In  EDIT/HISTORY mode current user (public) has access to common information
        userBean.setCurrentAccount(publicUser);
        materialBean.setMode(MaterialBean.Mode.EDIT);
        Structure s = createStructure(GlobalAdmissionContext.getPublicReadACL(), adminUser);
        materialBean.getMaterialEditState().setMaterialToEdit(s);
        materialBean.getMaterialEditState().setMaterialBeforeEdit(s);
        Assert.assertTrue(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));
        materialBean.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertTrue(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //Not valide detailtype
        Assert.assertFalse(permissionBean.isDetailPanelVisible("no valide subtype"));

        //Create corrupted state
        userBean.setCurrentAccount(null);
        Assert.assertFalse(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //In EDIT/HISTORY mode current user is owner but has no read rights
        userBean.setCurrentAccount(publicUser);
        materialBean.setMode(MaterialBean.Mode.EDIT);
        s.setOwner(publicUser);
        s.setACList(context.getAdminOnlyACL());
        Assert.assertTrue(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));
        materialBean.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertTrue(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //In EDIT/HISTORY mode current user is not owner but has no read rights
        userBean.setCurrentAccount(publicUser);
        materialBean.setMode(MaterialBean.Mode.EDIT);
        s.setOwner(adminUser);
        s.setACList(context.getAdminOnlyACL());
        Assert.assertFalse(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));
        materialBean.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertFalse(permissionBean.isDetailPanelVisible(COMMON_INFORMATION.toString()));

        //Not valide detailtype
        Assert.assertFalse(permissionBean.isDetailPanelVisible("no valide subtype"));
        userBean.setCurrentAccount(publicUser);
    }

    @Test
    public void test003_isFormulaAndMassesInputsEnabled() {
        materialBean.setAutoCalcFormularAndMasses(false);
        Assert.assertFalse(permissionBean.isFormulaAndMassesInputsDisabled());
        materialBean.setAutoCalcFormularAndMasses(true);
        materialBean.setMode(MaterialBean.Mode.CREATE);
        Assert.assertTrue(permissionBean.isFormulaAndMassesInputsDisabled());
        materialBean.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertTrue(permissionBean.isFormulaAndMassesInputsDisabled());
    }

    @Test
    public void test004_historyButtonCheck() {
        Structure s = createStructure(GlobalAdmissionContext.getPublicReadACL(), adminUser);
        materialBean.getMaterialEditState().setMaterialToEdit(s);
        materialBean.getMaterialEditState().setMaterialBeforeEdit(s);

        Assert.assertFalse(permissionBean.isForwardButtonEnabled());
        Assert.assertFalse(permissionBean.isBackwardButtonEnabled());

        Calendar c = new GregorianCalendar();
        c.set(2000, 1, 1);
        Date d_2000_01_01 = c.getTime();
        MaterialDifference diff2000_01_01 = createMaterialDiff(s.getId(), d_2000_01_01);
        c.add(Calendar.MONTH, 1);
        Date d_2000_02_01 = c.getTime();
        MaterialDifference diff2000_02_01 = createMaterialDiff(s.getId(), d_2000_02_01);
        c.add(Calendar.MONTH, 1);
        Date d_2000_03_01 = c.getTime();
        MaterialDifference diff2000_03_01 = createMaterialDiff(s.getId(), d_2000_03_01);
        s.getHistory().addDifference(diff2000_03_01);
        s.getHistory().addDifference(diff2000_02_01);
        s.getHistory().addDifference(diff2000_01_01);

        materialBean.getMaterialEditState().setCurrentVersiondate(d_2000_03_01);
        Assert.assertFalse(permissionBean.isForwardButtonEnabled());
        Assert.assertTrue(permissionBean.isBackwardButtonEnabled());

        materialBean.getMaterialEditState().setCurrentVersiondate(d_2000_02_01);
        Assert.assertTrue(permissionBean.isForwardButtonEnabled());
        Assert.assertTrue(permissionBean.isBackwardButtonEnabled());

        materialBean.getMaterialEditState().setCurrentVersiondate(d_2000_01_01);
        Assert.assertTrue(permissionBean.isForwardButtonEnabled());
        Assert.assertTrue(permissionBean.isBackwardButtonEnabled());

        materialBean.getMaterialEditState().setCurrentVersiondate(null);
        Assert.assertTrue(permissionBean.isForwardButtonEnabled());
        Assert.assertFalse(permissionBean.isBackwardButtonEnabled());

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialEditPermissionTest.war")
                        .addClass(IndexService.class);
        deployment = UserBeanDeployment.add(deployment);
        deployment = ItemDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }

    private Structure createStructure(ACList aclist, User owner) {
        Structure s = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        s.setOwner(owner);
        s.setACList(aclist);
        return s;
    }

    private MaterialDifference createMaterialDiff(int matId, Date d) {
        MaterialDifference diff = new MaterialHazardDifference();
        diff.initialise(matId, publicUser.getId(), d);
        return diff;
    }
}
