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
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
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
    
    @Test
    public void test003_switchMaterialType() {
        bean.setChoosenType(CompositionType.MIXTURE);
        bean.actionSwitchMaterialType(MaterialType.STRUCTURE.toString());
        Assert.assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype not allowed by Composition type
        bean.actionSwitchMaterialType(MaterialType.SEQUENCE.toString());
        Assert.assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype not valide 
        bean.actionSwitchMaterialType("no valide type");
        Assert.assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype null
        bean.actionSwitchMaterialType(null);
        Assert.assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
    }
    
    @Test
    public void test004_isMaterialAlreadyInComposition() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);
        
        Assert.assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure1));
        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertTrue(bean.isMaterialAlreadyInComposition(dummyStructure1));
        Assert.assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure2));
    }
    
    @Test
    public void test005_actionAddMaterialToComposition() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);
        BioMaterial dummyBioMaterial3 = new BioMaterial(3, new ArrayList<>(), 0, null, null, null, null);
        bean.setChoosenType(CompositionType.MIXTURE);
        
        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertEquals(1, bean.getMaterialsInComposition().size());
        //Not the same material again
        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertEquals(1, bean.getMaterialsInComposition().size());
        // No biomaterial because compositionType is MIXTURE
        bean.actionAddMaterialToComposition(dummyBioMaterial3);
        Assert.assertEquals(1, bean.getMaterialsInComposition().size());
        bean.actionAddMaterialToComposition(dummyStructure2);
        Assert.assertEquals(2, bean.getMaterialsInComposition().size());
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
