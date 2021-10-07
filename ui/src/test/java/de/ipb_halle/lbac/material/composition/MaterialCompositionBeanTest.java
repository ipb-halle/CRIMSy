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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectEditBean;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.junit.Assert;
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
    int publicAclId;
    private int structureId1, structureId2, biomaterialId;

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
        Assert.assertEquals(3, bean.getCompositionTypes().size());
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.EXTRACT));
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.MIXTURE));
        Assert.assertTrue(bean.getCompositionTypes().contains(CompositionType.PROTEIN));
    }

    @Test
    public void test002_setChoosenType() {
        bean.setChoosenType(CompositionType.MIXTURE);
        Assert.assertEquals(CompositionType.MIXTURE, bean.getChoosenType());
        Assert.assertFalse(bean.isMaterialTypePanelDisabled("No valide name"));
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

        Assert.assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure1.getId()));
        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertTrue(bean.isMaterialAlreadyInComposition(dummyStructure1.getId()));
        Assert.assertFalse(bean.isMaterialAlreadyInComposition(dummyStructure2.getId()));
    }

    @Test
    public void test005_actionAddMaterialToComposition() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Structure dummyStructure2 = new Structure("", 0d, 0d, 2, new ArrayList<>(), 0);
        BioMaterial dummyBioMaterial3 = new BioMaterial(3, new ArrayList<>(), 0, null, null, null, null);
        bean.setChoosenType(CompositionType.MIXTURE);

        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertEquals(1, bean.getConcentrationsInComposition().size());
        //Not the same material again
        bean.actionAddMaterialToComposition(dummyStructure1);
        Assert.assertEquals(1, bean.getConcentrationsInComposition().size());
        // No biomaterial because compositionType is MIXTURE
        bean.actionAddMaterialToComposition(dummyBioMaterial3);
        Assert.assertEquals(1, bean.getConcentrationsInComposition().size());
        bean.actionAddMaterialToComposition(dummyStructure2);
        Assert.assertEquals(2, bean.getConcentrationsInComposition().size());
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
        Assert.assertEquals(2, listOfIds.size());
        Assert.assertTrue(listOfIds.contains(1));
        Assert.assertTrue(listOfIds.contains(2));

        // Only one strcuture should be available because compositiontype is MIXTURE and the other structure is already in
        bean.getConcentrationsInComposition().add(new Concentration(dummyStructure1));
        listOfIds = bean.getMaterialsThatCanBeAdded().stream().map(m -> m.getId()).collect(Collectors.toCollection(ArrayList::new));
        Assert.assertEquals(1, listOfIds.size());
        Assert.assertTrue(listOfIds.contains(2));
    }

    @Test
    public void test007_actionStartSearch() {
        createProject("SearchServiceTest-Project-01", GlobalAdmissionContext.getPublicReadACL(), publicUser);

        createMaterials();
        bean.setChoosenType(CompositionType.MIXTURE);
        bean.actionSwitchMaterialType("STRUCTURE");
        bean.actionStartSearch();
        Assert.assertEquals(2, bean.getMaterialsThatCanBeAdded().size());

        bean.setMaterialName("l-002");
        bean.actionStartSearch();
        Assert.assertEquals(1, bean.getMaterialsThatCanBeAdded().size());

        bean.setSearchMolecule("H2O");
        bean.actionStartSearch();
        Assert.assertEquals(0, bean.getMaterialsThatCanBeAdded().size());

        bean.setChoosenType(CompositionType.EXTRACT);
        bean.actionSwitchMaterialType("BIOMATERIAL");
        bean.actionStartSearch();
        Assert.assertEquals(0, bean.getMaterialsThatCanBeAdded().size());
    }

    @Test
    public void test008_checkLocalizationOfMaterialType() {
        Structure dummyStructure1 = new Structure("", 0d, 0d, 1, new ArrayList<>(), 0);
        Assert.assertEquals("search_category_STRUCTURE", bean.getLocalizedMaterialType(new Concentration(dummyStructure1)));
    }

    @Test
    public void test009_onTabChange() {
        Tab structureTab = new Tab();
        structureTab.setTitle(MaterialType.STRUCTURE.toString());
        Assert.assertEquals("search_category_STRUCTURE", bean.getLocalizedTabTitle(MaterialType.STRUCTURE.toString()));
        bean.onTabChange(new TabChangeEvent(new UIViewRoot(), new BehaviorBase(), structureTab));
        Assert.assertEquals(MaterialType.STRUCTURE, bean.getChoosenMaterialType());
    }

    @Test
    public void test010_changeHistoryState() {
        createProject("SearchServiceTest-Project-01", GlobalAdmissionContext.getPublicReadACL(), publicUser);
        createMaterials();

        MaterialComposition composition = new MaterialComposition(project.getId(),CompositionType.EXTRACT);
        composition.addComponent(materialService.loadMaterialById(structureId1), .5d);
        composition.addComponent(materialService.loadMaterialById(biomaterialId), null);
        //Remove structure 2
        CompositionDifference diff1 = new CompositionDifference("EDIT");
        diff1.addDifference(structureId2, null, null, null);
        Calendar cal = new GregorianCalendar();
        cal.set(2000, 8, 12);
        Date date1 = cal.getTime();
        diff1.initialise(1, publicUser.getId(), date1);
        composition.getHistory().addDifference(diff1);
        //Add structure 1 and biomaterial 1
        CompositionDifference diff2 = new CompositionDifference("EDIT");
        diff2.addDifference(null, structureId1, null, .75d);
        diff2.addDifference(null, biomaterialId, null, null);

        cal.set(2000, 8, 10);
        Date date2 = cal.getTime();
        diff2.initialise(1, publicUser.getId(), date2);
        composition.getHistory().addDifference(diff2);

        materialBean.startMaterialEdit(composition);

        Assert.assertEquals(2, bean.getConcentrationsInComposition().size());
        Assert.assertEquals(biomaterialId, bean.getConcentrationsInComposition().get(0).getMaterial().getId());
        Assert.assertEquals(structureId1, bean.getConcentrationsInComposition().get(1).getMaterial().getId());
        
        materialBean.switchOneVersionBack();
        
        Assert.assertEquals(3, bean.getConcentrationsInComposition().size());
        
         materialBean.switchOneVersionBack();
          Assert.assertEquals(1, bean.getConcentrationsInComposition().size());
        
        
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
