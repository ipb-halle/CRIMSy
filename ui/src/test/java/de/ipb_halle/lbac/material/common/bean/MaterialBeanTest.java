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

import de.ipb_halle.lbac.admission.ACEntry;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.ResourceUtils;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.BehaviorBase;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class MaterialBeanTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private ACListService aclistService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    private TreeNode nodeToOperateOn;

    @Inject
    private MaterialBeanMock instance;

    @Inject
    private IndexService indexService;

    @Inject
    private HazardService hazardService;

    @Inject
    private TaxonomyService taxoService;

    @Inject
    private MaterialCompositionBean compositionBean;
   

    CreationTools creationTools;
    User publicUser;
    User customUser;
    ACList acl;
    Material material;
    UserBeanMock userBean;
    Project project;

    @BeforeEach
    public void init() {
        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ACList publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        createTaxonomyTreeInDB(publicReadAcl.getId(), publicUser.getId());
        /**
         * instance = new MaterialBeanMock();
         * instance.setAcListService(aclistService);
         * instance.setHazardService(hazardService);
         *
         */
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
        
        instance.setMessagePresenter(getMessagePresenterMock());        // review - should work by injection
        instance.getMaterialOverviewBean().setCurrentAccount(new LoginEvent(publicUser));

        material = creationTools.createStructure(project);
        Structure s = (Structure) material;
        s.getMolecule().setStructureModel(null);
        material.setOwner(publicUser);

        materialService.saveMaterialToDB(material, acl.getId(), new HashMap<>(), publicUser);
    }

    @AfterEach
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);
        // cleanUserFromDB(customUser);

    }

    @Test
    public void test001_checkRights() {
        instance.getMaterialEditState().setMaterialToEdit(material);
        instance.getMaterialEditState().setMaterialBeforeEdit(material);

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
        MaterialEditState materialEditState = new MaterialEditState(project, null, originalMaterial.copyMaterial(), originalMaterial, instance.getHazardController(),getMessagePresenterMock());
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
        Assert.assertEquals(13, loadedBioMat.getHazards().getHazards().keySet().iterator().next().getId());

        instance.startMaterialEdit(loadedBioMat);
        instance.getHazardController().setBioSavetyLevel(instance.getHazardController().getPossibleBioSavetyLevels().get(2));
        instance.actionSaveMaterial();

        loadedBioMat = materialService.loadMaterialById(bioMat.getId());
        instance.getMaterialEditState().setMaterialBeforeEdit(loadedBioMat);
        instance.getMaterialEditState().setCurrentVersiondate(loadedBioMat.getHistory().getChanges().keySet().stream().reduce((first, second) -> second).orElse(null));
        Assert.assertEquals(1, loadedBioMat.getHazards().getHazards().size());
        Assert.assertEquals(14, loadedBioMat.getHazards().getHazards().keySet().iterator().next().getId());

        instance.switchOneVersionBack();

        instance.switchOneVersionBack();

        instance.switchOneVersionForward();

        instance.switchOneVersionForward();
    }

    @Test
    public void test004_editStorageInformation() {
        material = creationTools.createBioMaterial(project, "BioMat-001", taxoService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(material, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>(), publicUser);
        //Set storageclass from none -> ID:4
        instance.startMaterialEdit(material);
        instance.getStorageInformationBuilder().setStorageClassActivated(true);
        instance.getStorageInformationBuilder().setChoosenStorageClass(materialService.loadStorageClasses().get(3));
        instance.actionSaveMaterial();

        //Load material,init storageInfortmationBuilder and check changes
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertNotNull(instance.getStorageInformationBuilder().getChoosenStorageClass());
        Assert.assertEquals(
                materialService.loadStorageClasses().get(3),
                instance.getStorageInformationBuilder().getChoosenStorageClass());
        Assert.assertTrue(
                instance.getStorageInformationBuilder().isStorageClassActivated());

        //Set storageclass from ID:4 -> ID:3, also add Remark
        instance.getStorageInformationBuilder().setChoosenStorageClass(materialService.loadStorageClasses().get(2));
        instance.getStorageInformationBuilder().setRemarks("Remark!!");
        instance.actionSaveMaterial();

        //Load material,init storageInfortmationBuilder and check changes
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertEquals(
                materialService.loadStorageClasses().get(2),
                instance.getStorageInformationBuilder().getChoosenStorageClass());
        Assert.assertEquals(
                "Remark!!",
                instance.getStorageInformationBuilder().getRemarks());
        Assert.assertTrue(
                instance.getStorageInformationBuilder().isStorageClassActivated());

        //Set storageclass from ID:3 -> none
        instance.getStorageInformationBuilder().setStorageClassActivated(false);
        instance.actionSaveMaterial();

        //Load material,init storageInfortmationBuilder and check changes
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertNull(material.getStorageInformation().getStorageClass());
        Assert.assertEquals(
                materialService.loadStorageClasses().get(0),
                instance.getStorageInformationBuilder().getChoosenStorageClass());
        Assert.assertFalse(
                instance.getStorageInformationBuilder().isStorageClassActivated());
        Assert.assertNull(
                instance.getStorageInformationBuilder().getRemarks()
        );
        //Check History of changes
        Assert.assertEquals(3, material.getHistory().getChanges().size());
        Iterator<Date> iter = material.getHistory().getChanges().keySet().iterator();
        //First change : none -> 4
        MaterialStorageDifference diff = (MaterialStorageDifference) material.getHistory().getChanges().get(iter.next()).get(0);
        Assert.assertEquals(4, diff.getStorageclassNew(), 0);
        Assert.assertNull(diff.getStorageclassOld());
        //Second change : 4 -> 3
        diff = (MaterialStorageDifference) material.getHistory().getChanges().get(iter.next()).get(0);
        Assert.assertEquals(4, diff.getStorageclassOld(), 0);
        Assert.assertEquals(3, diff.getStorageclassNew(), 0);
        Assert.assertNull(diff.getDescriptionOld());
        Assert.assertEquals("Remark!!", diff.getDescriptionNew());
        //Third change : 3 -> none
        diff = (MaterialStorageDifference) material.getHistory().getChanges().get(iter.next()).get(0);
        Assert.assertEquals(3, diff.getStorageclassOld(), 0);
        Assert.assertNull(diff.getStorageclassNew());
        Assert.assertEquals("Remark!!", diff.getDescriptionOld());
        Assert.assertNull(diff.getDescriptionNew());

    }

    @Test
    public void test005_editStorageConditions() {
        material = creationTools.createBioMaterial(project, "BioMat-001", taxoService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(material, GlobalAdmissionContext.getPublicReadACL().getId(), new HashMap<>(), publicUser);
        instance.startMaterialEdit(material);

        Assert.assertEquals(23, instance.getStorageInformationBuilder().getPossibleStorageClasses().size());
        // add frozen and lightsensitive

        addStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.keepFrozen);
        addStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.lightSensitive);

        instance.actionSaveMaterial();
        //load material and check new conditions
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertEquals(2, instance.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.keepFrozen));
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.lightSensitive));

        //remove frozen, add acidSensitive, keep cool
        removeStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.keepFrozen);

        addStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.acidSensitive);
        addStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.keepCool);

        instance.actionSaveMaterial();
        //load material and check new conditions
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertEquals(3, instance.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.lightSensitive));
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.acidSensitive));
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.keepCool));

        //remove all conditions and add keep below 40 degress
        instance.getStorageInformationBuilder().setSelectedConditions(new StorageCondition[0]);
        addStorageCondition(instance.getStorageInformationBuilder(), StorageCondition.keepTempBelowMinus40Celsius);
        instance.actionSaveMaterial();

        //load material and check new conditions
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertEquals(1, instance.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertTrue(containsStorageCondition(instance.getStorageInformationBuilder().getSelectedConditions(), StorageCondition.keepTempBelowMinus40Celsius));
        //clear all conditions
        instance.getStorageInformationBuilder().setSelectedConditions(new StorageCondition[0]);
        instance.actionSaveMaterial();
        //load material and check new conditions
        material = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(material);
        Assert.assertTrue(instance.getStorageInformationBuilder().getSelectedConditions().length == 0);

        //Check History of storage conditions
    }

    @Test
    public void test006_editTaxonomyOfBioMaterial() {

        ACList publicAcl = GlobalAdmissionContext.getPublicReadACL();
        cleanMaterialsFromDB();
        createTaxonomyTreeInDB(GlobalAdmissionContext.getPublicReadACL().getId(), publicUser.getId());
        Taxonomy mushromTaxo = taxoService.loadTaxonomyById(2);
        material = creationTools.createBioMaterial(project, "BioMat-001", mushromTaxo, null);
        materialService.saveMaterialToDB(material, publicAcl.getId(), new HashMap<>(), publicUser);

        instance.startMaterialEdit(material);
        instance.getTaxonomyController().onTaxonomyExpand(createExpandEvent("Pilze_de"));
        instance.getTaxonomyController().onTaxonomyExpand(createExpandEvent("Agaricomycetes_de"));
        instance.getTaxonomyController().onTaxonomySelect(createSelectEvent("Champignonartige_de"));

        instance.actionSaveMaterial();

        BioMaterial bioMaterial = (BioMaterial) materialService.loadMaterialById(material.getId());
        Assert.assertEquals(4, bioMaterial.getTaxonomy().getId());

        instance.startMaterialEdit(bioMaterial);
    }

    @Test
    public void test007_tryToEditMaterialWithoutName() {
        material = creationTools.createStructure(project);
        materialService.saveMaterialToDB(material, project.getACList().getId(), new HashMap<>(), publicUser.getId());

        instance.startMaterialEdit(material);
        instance.getMaterialNameBean().getNames().clear();
        instance.actionSaveMaterial();

        Assert.assertEquals(Arrays.asList("materialCreation_error_NO_MATERIAL_NAME"), instance.getErrorMessages());
    }

    @Test
    public void test008_saveNewComposition() {
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        project.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, project.getACList());
        projectService.saveEditedProjectToDb(project);
        material = creationTools.createStructure(project);
        materialService.saveMaterialToDB(material, project.getACList().getId(), new HashMap<>(), publicUser.getId());
        
        instance.startMaterialCreation();
        instance.getCompositionBean().actionAddMaterialToComposition(material);
    
            
        project.getACList().getACEntries().values().iterator().next().setPermEdit(true);
        
        Assert.assertEquals(1, instance.getCompositionBean().getConcentrationsInComposition().size());
        instance.getMaterialNameBean().getNames().get(0).setValue("Composition");
        instance.setCurrentMaterialType(MaterialType.COMPOSITION);
        instance.getMaterialEditState().setCurrentProject(project);
        instance.actionSaveMaterial();

        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("Composition");
        requestBuilder.addMaterialType(MaterialType.COMPOSITION);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());
        Assert.assertEquals(1, result.getAllFoundObjects().size());                        
    }

    @Test
    public void test009_editComposition() {
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveEditedProjectToDb(project);
        material = creationTools.createStructure(project);
        materialService.saveMaterialToDB(material, project.getACList().getId(), new HashMap<>(), publicUser.getId());

        Material material2 = creationTools.createStructure(project);
        material2.getNames().clear();
        material2.getNames().add(new MaterialName("component2", "en", 0));
        materialService.saveMaterialToDB(material2, project.getACList().getId(), new HashMap<>(), publicUser.getId());

        instance.startMaterialCreation();
        instance.getCompositionBean().actionAddMaterialToComposition(material);
        instance.getMaterialEditState().setCurrentProject(project);
        Assert.assertEquals(1, instance.getCompositionBean().getConcentrationsInComposition().size());
        instance.getMaterialNameBean().getNames().get(0).setValue("Composition");
        instance.setCurrentMaterialType(MaterialType.COMPOSITION);
        instance.actionSaveMaterial();
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("Composition");
        requestBuilder.addMaterialType(MaterialType.COMPOSITION);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());

        instance.startMaterialEdit((MaterialComposition) result.getAllFoundObjects().get(0).getSearchable());
        Assert.assertEquals(1, instance.getCompositionBean().getConcentrationsInComposition().size());
        Assert.assertEquals(CompositionType.EXTRACT, instance.getCompositionBean().getChoosenType());
        Assert.assertFalse(instance.getCompositionBean().isCompositionTypeEditable());
        Assert.assertTrue(instance.getCompositionBean().getMaterialName().isEmpty());
        Assert.assertTrue(instance.getCompositionBean().getSearchMolecule().isEmpty());
        Assert.assertTrue(instance.getCompositionBean().getFoundMaterials().isEmpty());

        Concentration conc = instance.getCompositionBean().getConcentrationsInComposition().get(0);
        instance.getCompositionBean().actionRemoveConcentrationFromComposition(conc);
        instance.getCompositionBean().actionAddMaterialToComposition(material2);
        Assert.assertEquals(1, instance.getCompositionBean().getConcentrationsInComposition().size());
        Assert.assertEquals(1, instance.getCompositionBean().getFoundMaterials().size());

        instance.actionSaveMaterial();

        requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("Composition");
        requestBuilder.addMaterialType(MaterialType.COMPOSITION);
        result = materialService.loadReadableMaterials(requestBuilder.build());
        MaterialComposition composition = (MaterialComposition) result.getAllFoundObjects().get(0).getSearchable();
        Assert.assertEquals(1, composition.getComponents().size());
        Assert.assertEquals(material2.getId(), composition.getComponents().iterator().next().getMaterialId());
    }

    @Test
    public void test010_saveNewStructure() {
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        project.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, project.getACList());
        projectService.saveEditedProjectToDb(project);

        instance.startMaterialCreation();
        instance.getMaterialEditState().setCurrentProject(project);
        instance.setCurrentMaterialType(MaterialType.STRUCTURE);

        instance.getMaterialNameBean().getNames().get(0).setValue("test-structure");
        instance.getMaterialIndexBean().getIndices().add(new IndexEntry(2, "XYZ", "de"));
        instance.getMaterialEditState().setCurrentProject(project);
        instance.actionSaveMaterial();

        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("test-structure");
        requestBuilder.addMaterialType(MaterialType.STRUCTURE);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());

        List<Structure> foundObjects = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, foundObjects.size());

        Structure loadedStruc = foundObjects.get(0);

        Assert.assertEquals(1, loadedStruc.getIndices().size());
    }

    @DisplayName("Issue-based (#156) - Disappeared molecule")
    @Test
    public void test_011_bug_disappeared_molecule_156() throws IOException {
        String benzene = ResourceUtils.readResourceFile("molfiles/Benzene.mol");

        //Create new Structure
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        project.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, project.getACList());
        projectService.saveEditedProjectToDb(project);
        instance.startMaterialCreation();
        instance.getMaterialEditState().setCurrentProject(project);
        instance.getMaterialNameBean().getNames().get(0).setValue("test_011-structure");
        instance.setAutoCalcFormularAndMasses(true);
        instance.getStructureInfos().setStructureModel(benzene);
        instance.getMaterialEditState().setCurrentProject(project);
        instance.actionSaveMaterial();

        //Load structure
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("test_011-structure");
        requestBuilder.addMaterialType(MaterialType.STRUCTURE);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());

        //Edit structure
        Structure originalStruc = (Structure) result.getAllFoundObjects().get(0).getSearchable();
        instance.startMaterialEdit(originalStruc);
        instance.setAutoCalcFormularAndMasses(false);
        instance.getStructureInfos().setAverageMolarMass(null);
        instance.actionSaveMaterial();

        //Check if the molecule still exists but without  molar mass
        result = materialService.loadReadableMaterials(requestBuilder.build());
        Structure editedStruc = (Structure) result.getAllFoundObjects().get(0).getSearchable();
        Assert.assertEquals(benzene, editedStruc.getMolecule().getStructureModel());
        Assert.assertNull(editedStruc.getAverageMolarMass());
    }
    
    @Test
    public void test012_createNewConsumable(){        
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        int originalPermCode=project.getACList().getPermCode();
        project.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, project.getACList());
        project.getDetailTemplates().get(MaterialDetailType.COMMON_INFORMATION).getACEntries().get(1).setPermEdit(true);
       
        projectService.saveEditedProjectToDb(project);
        
        instance.startMaterialCreation();
        instance.setCurrentMaterialType(MaterialType.CONSUMABLE);
        instance.getMaterialEditState().setCurrentProject(project);
        instance.getMaterialNameBean().getNames().get(0).setValue("test_012-consumable");
        
        
        instance.actionSaveMaterial();
        
         MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setMaterialName("test_012-consumable");
        requestBuilder.addMaterialType(MaterialType.CONSUMABLE);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());

        List<Consumable> foundObjects = result.getAllFoundObjects(Consumable.class, nodeService.getLocalNode());
        Assert.assertEquals(1, foundObjects.size());
        
        Consumable cons=foundObjects.get(0);
        Assert.assertNotEquals(originalPermCode,cons.getACList().getPermCode());


    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialBeanTest.war")
                        .addClass(IndexService.class)
                        .addClass(MaterialNameBean.class)
                        .addClass(MaterialOverviewBean.class)
                        .addClass(ProjectBean.class)
                        .addClass(ProjectEditBean.class)
                        .addClass(ItemBean.class)
                        .addClass(ItemOverviewBean.class)
                        .addClass(MaterialIndexBean.class)
                        .addClass(MaterialCompositionBean.class)
                        .addClass(MaterialBeanMock.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }

    private void addStorageCondition(StorageInformationBuilder builder, StorageCondition c) {
        StorageCondition[] conds = builder.getSelectedConditions();
        StorageCondition[] condsNew = new StorageCondition[conds.length + 1];
        for (int i = 0; i < conds.length; i++) {
            condsNew[i] = conds[i];
        }
        condsNew[condsNew.length - 1] = c;
        builder.setSelectedConditions(condsNew);
    }

    private void removeStorageCondition(StorageInformationBuilder builder, StorageCondition c) {
        StorageCondition[] conds = builder.getSelectedConditions();
        StorageCondition[] condsNew = new StorageCondition[conds.length - 1];
        int j = 0;
        for (int i = 0; i < conds.length; i++) {
            if (conds[i] != c) {
                condsNew[j] = conds[i];
                j++;
            }
        }
        builder.setSelectedConditions(condsNew);
    }

    private boolean containsStorageCondition(StorageCondition[] conds, StorageCondition c) {
        for (StorageCondition sc : conds) {
            if (sc == c) {
                return true;
            }
        }
        return false;
    }

    private NodeExpandEvent createExpandEvent(String nameOfTaxToSelect) {
        nodeToOperateOn = null;
        selectTaxonomyFromTree(nameOfTaxToSelect, instance.getTaxonomyController().getTreeController().getTaxonomyTree());
        if (nodeToOperateOn == null) {
            throw new RuntimeException("Could not find " + nameOfTaxToSelect + " in tree");
        }
        return new NodeExpandEvent(
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
        for (Object n : tree.getChildren()) {
            selectTaxonomyFromTree(name, (TreeNode) n);
        }
    }

    private NodeSelectEvent createSelectEvent(String nameOfTaxToSelect) {
        nodeToOperateOn = null;
        selectTaxonomyFromTree(nameOfTaxToSelect, instance.getTaxonomyController().getTreeController().getTaxonomyTree());
        if (nodeToOperateOn == null) {
            throw new RuntimeException("Could not find " + nameOfTaxToSelect + " in tree");
        }
        return new NodeSelectEvent(
                new UIViewRoot(),
                new BehaviorBase(),
                nodeToOperateOn
        );
    }

}
