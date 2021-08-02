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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialCompositionBeanTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;
    private Project project;

    @Inject
    private MaterialCompositionBean bean;

    @Before
    public void init() {
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(em));
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);

    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);

    }

    @Test
    public void test001_getCompositionTypes() {
        Assert.assertEquals(3, bean.getCompositionTypes().size());
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.EXTRACT));
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.MIXTURE));
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.PROTEIN));
    }

    @Test
    public void test002_setChoosenType() {
        bean.setChoosenType(CompositionType.MIXTURE);
        Assert.assertEquals(CompositionType.MIXTURE, bean.getChoosenType());
        Assert.assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        Assert.assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        Assert.assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));

        bean.setChoosenType(CompositionType.PROTEIN);
        Assert.assertEquals(CompositionType.PROTEIN, bean.getChoosenType());
        Assert.assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        Assert.assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        Assert.assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));

        bean.setChoosenType(CompositionType.EXTRACT);
        Assert.assertEquals(CompositionType.EXTRACT, bean.getChoosenType());
        Assert.assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        Assert.assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        Assert.assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialCompositionBeanTest.war")
                        .addClass(IndexService.class)
                        .addClass(MaterialCompositionBean.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }

}
