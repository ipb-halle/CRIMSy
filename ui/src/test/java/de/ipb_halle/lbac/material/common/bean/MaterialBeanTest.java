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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import java.util.HashMap;
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
public class MaterialBeanTest extends TestBase {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private ACListService aclistService;
    
    @Inject
    private MaterialService materialService;
    
    @Inject
    private ProjectService projectService;
    
    MateriaBeanMock instance;
    CreationTools creationTools;
    User publicUser;
    User customUser;
    ACList acl;
    Material material;
    UserBeanMock userBean;
    Project project;
    
    @Inject
    private IndexService indexService;
    
    @Inject
    private HazardService hazardService;
    
    @Inject
    private TaxonomyService taxoService;
    
    @Before
    public void init() {
        super.setUp();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ACList publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        createTaxonomyTreeInDB(publicReadAcl.getId(), publicUser.getId());
        instance = new MateriaBeanMock();
        instance.setAcListService(aclistService);
        instance.setHazardService(hazardService);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        acl = new ACList();
        customUser = createUser("testUser", "testUser");
        acl.addACE(customUser, new ACPermission[]{ACPermission.permEDIT});
        acl = aclistService.save(acl);
        project.setOwner(publicUser);
        project.setACList(acl);
        projectService.saveProjectToDb(project);
        
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        instance.setUserBean(userBean);
        
        material = creationTools.createStructure(project);
        Structure s = (Structure) material;
        s.getMolecule().setStructureModel(null);
        material.setOwner(publicUser);
        
        materialService.saveMaterialToDB(material, acl.getId(), new HashMap<>(), publicUser);
        
        instance.setMaterialIndexBean(new MaterialIndexBean());
        instance.setMaterialNameBean(new MaterialNameBean());
        instance.setMessagePresenter(new MessagePresenterMock());
        instance.getMaterialEditState().setMaterialToEdit(material);
        instance.getMaterialEditState().setMaterialBeforeEdit(material);
        instance.setMaterialService(materialService);
        
    }
    
    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);
        // cleanUserFromDB(customUser);

    }
    
    @Test
    public void test001_checkRights() {
        
        instance.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertFalse("testcase 001: In history mode edit must be false ", instance.isProjectEditEnabled());
        instance.setMode(MaterialBean.Mode.CREATE);
        Assert.assertTrue("testcase 001: In creation mode edit must be true ", instance.isProjectEditEnabled());
        instance.setMode(MaterialBean.Mode.EDIT);
        Assert.assertTrue("testcase 001: Owner must be able to edit project", instance.isProjectEditEnabled());
        User admin = memberService.loadUserById(GlobalAdmissionContext.OWNER_ACCOUNT_ID);
        userBean.setCurrentAccount(admin);
        Assert.assertFalse("testcase 001: No priviliged user must not  be able to edit project", instance.isProjectEditEnabled());
        userBean.setCurrentAccount(customUser);
        Assert.assertTrue("testcase 001: Priviliged user must   be able to edit project", instance.isProjectEditEnabled());
    }
    
    @Test
    public void test002_navigateInHistory() throws Exception {
        MaterialIndexBean indexBean = new MaterialIndexBean();
        
        instance.setProjectService(projectService);
        indexBean.setIndexService(indexService);
        instance.setMaterialIndexBean(indexBean);
        
        instance.setProjectBean(new ProjectBean());
        
        Material originalMaterial = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(originalMaterial.copyMaterial());
        MaterialEditState materialEditState = new MaterialEditState(project, null, originalMaterial.copyMaterial(), originalMaterial, instance.getHazardController());
        materialEditState.getMaterialToEdit().getNames().add(new MaterialName("Edited-name-1", "de", 3));
        materialEditState.getMaterialToEdit().getNames().add(new MaterialName("Edited-name-2", "en", 4));
        
        materialService.saveEditedMaterial(
                materialEditState.getMaterialToEdit(),
                materialEditState.getMaterialBeforeEdit(),
                materialEditState.getCurrentProject().getUserGroups().getId(),
                userBean.getCurrentAccount().getId());
        
        instance.setMaterialIndexBean(indexBean);
        originalMaterial = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(originalMaterial);
        instance.switchOneVersionBack();
        
        Assert.assertEquals(2, instance.getMaterialNameBean().getNames().size());
        Assert.assertEquals("Test-Struktur", instance.getMaterialNameBean().getNames().get(0).getValue());
        Assert.assertEquals("Test-Structure", instance.getMaterialNameBean().getNames().get(1).getValue());
        
        instance.switchOneVersionForward();
        Assert.assertEquals(4, instance.getMaterialNameBean().getNames().size());
        Assert.assertEquals("Test-Struktur", instance.getMaterialNameBean().getNames().get(0).getValue());
        Assert.assertEquals("Test-Structure", instance.getMaterialNameBean().getNames().get(1).getValue());
        Assert.assertEquals("Edited-name-1", instance.getMaterialNameBean().getNames().get(2).getValue());
        Assert.assertEquals("Edited-name-2", instance.getMaterialNameBean().getNames().get(3).getValue());
    }
    
    @Test
    public void test003_editBioMaterial() {
        MaterialIndexBean indexBean = new MaterialIndexBean();
        indexBean.setIndexService(indexService);
        instance.setMaterialIndexBean(indexBean);
        instance.setTaxonomyService(taxoService);
        
        BioMaterial bioMat = creationTools.createBioMaterial(project, "BioMat-001", taxoService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(bioMat, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>(), publicUser);
        instance.setProjectService(projectService);
        instance.setProjectBean(new ProjectBean());
        instance.startMaterialEdit(bioMat);
        
        instance.getHazardController().setBioSavetyLevel(instance.getHazardController().getPossibleBioSavetyLevels().get(1));
        
        instance.actionSaveMaterial();
        
        Material loadedBioMat = materialService.loadMaterialById(bioMat.getId());
        Assert.assertEquals(1, loadedBioMat.getHazards().getHazards().size());
        Assert.assertEquals(12, loadedBioMat.getHazards().getHazards().keySet().iterator().next().getId());
        
        instance.startMaterialEdit(loadedBioMat);
        instance.getHazardController().setBioSavetyLevel(instance.getHazardController().getPossibleBioSavetyLevels().get(2));
        instance.actionSaveMaterial();
        
        loadedBioMat = materialService.loadMaterialById(bioMat.getId());
        instance.getMaterialEditState().setMaterialBeforeEdit(loadedBioMat);
        instance.getMaterialEditState().setCurrentVersiondate(loadedBioMat.getHistory().getChanges().keySet().stream().reduce((first,second)->second).orElse(null));
        Assert.assertEquals(1, loadedBioMat.getHazards().getHazards().size());
        Assert.assertEquals(13, loadedBioMat.getHazards().getHazards().keySet().iterator().next().getId());
        
        instance.switchOneVersionBack();
        
        instance.switchOneVersionBack();
        
        instance.switchOneVersionForward();
        
        instance.switchOneVersionForward();
        
    }
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialBeanTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
