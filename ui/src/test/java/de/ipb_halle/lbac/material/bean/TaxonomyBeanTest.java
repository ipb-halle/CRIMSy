/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.mocks.MaterialEditSaverMock;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.material.service.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.service.TaxonomyService;
import de.ipb_halle.lbac.material.service.TissueService;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Before
    public void init() {

        bean = new TaxonomyBean();
        bean.setTaxonomyService(taxonomyService);
        bean.init();

        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID)));
        materialService.setUserBean(userBean);
        materialService.setEditedMaterialSaver(new MaterialEditSaverMock(materialService));
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(materialService.getEm()));
        bean.setMaterialService(materialService);
        owner = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        String userGroups = GlobalAdmissionContext.getPublicReadACL().getId().toString();
        createTaxonomyTreeInDB(userGroups, owner.getId().toString());

    }

    @After
    public void finish() {

    }

    @Test
    public void test001_reloadTaxonomies() {
        LoginEvent event = new LoginEvent(owner);
        bean.setCurrentAccount(event);
        bean.getTreeController().reloadTreeNode(null);
        int i = 0;
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TaxonomyBeanTest.war")
                .addClass(TaxonomyService.class)
                .addClass(TaxonomyBean.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(LdapProperties.class)
                .addClass(KeyManager.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(MoleculeService.class)
                .addClass(TaxonomyService.class)
                .addClass(ProjectService.class)
                .addClass(TissueService.class)
                .addClass(MaterialService.class);
    }
}
