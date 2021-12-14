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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.BehaviorBase;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

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

    @Inject
    private UserBeanMock userBeanMock;
    private Project project1;

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private SearchService searchService;
    int publicAclId;
    private int structureId1, structureId2, biomaterialId;

    @Inject
    private HazardService hazardService;

    @Inject
    MaterialBean materialBean;

    @Before
    public void init() {
        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
        userBeanMock.setCurrentAccount(publicUser);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();

    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);
        if (project1 != null) {
            cleanProjectFromDB(project1, false);
        }

    }

    @Test
    public void test001_getCompositionTypes() {
        assertEquals(3, bean.getCompositionTypes().size());
        assertTrue(bean.getCompositionTypes().contains(CompositionType.EXTRACT));
        assertTrue(bean.getCompositionTypes().contains(CompositionType.MIXTURE));
        assertTrue(bean.getCompositionTypes().contains(CompositionType.PROTEIN));
    }

    @Test
    public void test002_setChoosenType() {
        bean.setChoosenType(CompositionType.MIXTURE);
        assertEquals(CompositionType.MIXTURE, bean.getChoosenType());
        assertFalse(bean.isMaterialTypePanelDisabled("No valide name"));
        assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));

        bean.setChoosenType(CompositionType.PROTEIN);
        assertEquals(CompositionType.PROTEIN, bean.getChoosenType());
        assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));

        bean.setChoosenType(CompositionType.EXTRACT);
        assertEquals(CompositionType.EXTRACT, bean.getChoosenType());
        assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.STRUCTURE.toString()));
        assertFalse(bean.isMaterialTypePanelDisabled(MaterialType.BIOMATERIAL.toString()));
        assertTrue(bean.isMaterialTypePanelDisabled(MaterialType.SEQUENCE.toString()));
    }

    @Test
    public void test003_switchMaterialType() {
        bean.setChoosenType(CompositionType.MIXTURE);
        bean.actionSwitchMaterialType(MaterialType.STRUCTURE.toString());
        assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype not allowed by Composition type
        bean.actionSwitchMaterialType(MaterialType.SEQUENCE.toString());
        assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype not valide 
        bean.actionSwitchMaterialType("no valide type");
        assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
        //Materialtype null
        bean.actionSwitchMaterialType(null);
        assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
    }

    @Test
    public void test004_isMaterialAlreadyInComposition() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);

        assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure1.getId()));
        bean.actionAddMaterialToComposition(dummyStructure1);
        assertTrue(bean.isMaterialAlreadyInComposition(dummyStructure1.getId()));
        assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure2.getId()));
    }

    @Test
    public void test005_actionAddMaterialToComposition() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);
        BioMaterial dummyBioMaterial3 = new BioMaterial(3, new ArrayList<>(), 0, null, null, null, null);
        bean.setChoosenType(CompositionType.MIXTURE);

        bean.actionAddMaterialToComposition(dummyStructure1);
        assertEquals(1, bean.getConcentrationsInComposition().size());
        //Not the same material again
        bean.actionAddMaterialToComposition(dummyStructure1);
        assertEquals(1, bean.getConcentrationsInComposition().size());
        // No biomaterial because compositionType is MIXTURE
        bean.actionAddMaterialToComposition(dummyBioMaterial3);
        assertEquals(1, bean.getConcentrationsInComposition().size());
        bean.actionAddMaterialToComposition(dummyStructure2);
        assertEquals(2, bean.getConcentrationsInComposition().size());
    }

    @Test
    public void test006_getMaterialsThatCanBeAdded() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);
        BioMaterial dummyBioMaterial3 = new BioMaterial(3, new ArrayList<>(), 0, null, null, null, null);
        bean.setChoosenType(CompositionType.MIXTURE);
        bean.getFoundMaterials().add(dummyStructure1);
        bean.getFoundMaterials().add(dummyStructure2);
        bean.getFoundMaterials().add(dummyBioMaterial3);

        // Only the two strcutures should be available because compositiontype is MIXTURE
        List<Integer> listOfIds = bean.getMaterialsThatCanBeAdded().stream().map(m -> m.getId()).collect(Collectors.toCollection(ArrayList::new));
        assertEquals(2, listOfIds.size());
        assertTrue(listOfIds.contains(1));
        assertTrue(listOfIds.contains(2));

        // Only one strcuture should be available because compositiontype is MIXTURE and the other structure is already in
        bean.getConcentrationsInComposition().add(new Concentration(dummyStructure1));
        listOfIds = bean.getMaterialsThatCanBeAdded().stream().map(m -> m.getId()).collect(Collectors.toCollection(ArrayList::new));
        assertEquals(1, listOfIds.size());
        assertTrue(listOfIds.contains(2));
    }

    @Test
    public void test007_actionStartSearch() {
        createProject("SearchServiceTest-Project-01", GlobalAdmissionContext.getPublicReadACL(), publicUser);

        createMaterials();
        bean.setChoosenType(CompositionType.MIXTURE);
        bean.actionSwitchMaterialType("STRUCTURE");
        bean.actionStartSearch();
        assertEquals(2, bean.getMaterialsThatCanBeAdded().size());

        bean.setMaterialName("l-002");
        bean.actionStartSearch();
        assertEquals(1, bean.getMaterialsThatCanBeAdded().size());

        bean.setSearchMolecule("H2O");
        bean.actionStartSearch();
        assertEquals(0, bean.getMaterialsThatCanBeAdded().size());

        bean.setChoosenType(CompositionType.EXTRACT);
        bean.actionSwitchMaterialType("BIOMATERIAL");
        bean.actionStartSearch();
        assertEquals(0, bean.getMaterialsThatCanBeAdded().size());
    }

    @Test
    public void test008_checkLocalizationOfMaterialType() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        assertEquals("search_category_STRUCTURE", bean.getLocalizedMaterialType(new Concentration(dummyStructure1)));
    }

    @Test
    public void test009_onTabChange() {
        Tab structureTab = new Tab();
        structureTab.setTitle("search_category_STRUCTURE");
        assertEquals("search_category_STRUCTURE", bean.getLocalizedTabTitle(MaterialType.STRUCTURE.toString()));
        bean.onTabChange(new TabChangeEvent(new UIViewRoot(), new BehaviorBase(), structureTab));
        assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
    }

    @Test
    public void test010_saveComposition() {
        createTaxonomyTreeInDB(publicAclId, publicUser.getId());
        createProject("SearchServiceTest-Project-01", GlobalAdmissionContext.getPublicReadACL(), publicUser);
        createMaterials();

        materialBean.startMaterialCreation();
        materialBean.getMaterialEditState().setCurrentProject(project);
        materialBean.getMaterialNameBean().getNames().get(0).setValue("test-composition");
        materialBean.setCurrentMaterialType(MaterialType.COMPOSITION);

        bean.actionAddMaterialToComposition(materialService.loadMaterialById(structureId1));
        bean.actionAddMaterialToComposition(materialService.loadMaterialById(structureId2));

        materialBean.getMaterialIndexBean().getIndices().add(new IndexEntry(2, "XYZ", "de"));

        materialBean.getHazardController().addHazardType(hazardService.getHazardById(1), null);
        materialBean.getStorageInformationBuilder().addStorageCondition(StorageCondition.keepCool);
        materialBean.getStorageInformationBuilder().setStorageClassActivated(true);
        materialBean.getStorageInformationBuilder().setChoosenStorageClass(materialBean.getStorageInformationBuilder().getStorageClassById(1));

        materialBean.actionSaveMaterial();

        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        builder.addMaterialType(MaterialType.COMPOSITION);
        builder.setMaterialName("test-composition");
        SearchResult result = searchService.search(Arrays.asList(builder.build()), nodeService.getLocalNode());
        List<MaterialComposition> foundObjects = result.getAllFoundObjects(MaterialComposition.class, nodeService.getLocalNode());
        assertEquals(1, foundObjects.size());

        MaterialComposition composition = foundObjects.get(0);
        assertEquals(1, composition.getIndices().size());
        assertEquals(2, composition.getComponents().size());
        assertEquals(1, composition.getHazards().getHazards().size());
        assertEquals(1, composition.getStorageInformation().getStorageClass().id, 0);
        assertEquals(1, composition.getStorageInformation().getStorageConditions().size());

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialCompositionBeanTest.war");
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        deployment = MaterialBeanDeployment.add(deployment);
        deployment = PrintBeanDeployment.add(deployment);
        deployment = MaterialDeployment.add(deployment);
        deployment.addClass(SearchService.class);
        deployment.addClass(ExperimentService.class);
        deployment.addClass(DocumentSearchService.class);
        return deployment;
    }

    private void createMaterials() {
        materialCreator = new MaterialCreator(entityManagerService);
        structureId1 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                "CCCCCCCCC",
                project1.getId(),
                "Testmaterial-001");
        materialCreator.addIndexToMaterial(structureId1, 2, "Index of material 1");

        structureId2 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project1.getId(),
                "Testmaterial-002");

        createTaxanomy(1000, "Life", 1, publicAclId, publicUser.getId());
        BioMaterial bioMaterial = creationTools.createBioMaterial(project1, "BioMaterial001", taxonomyService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(bioMaterial, publicAclId, new HashMap<>(), publicUser.getId());
        biomaterialId = bioMaterial.getId();
    }

    private void createProject(String projectName, ACList projectAcl, User user) {
        ProjectCreator projectCreator = new ProjectCreator(
                projectService,
                projectAcl);
        projectCreator.setProjectName(projectName);
        project1 = projectCreator.createAndSaveProject(user);
    }

}
