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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.history.MaterialHistory;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyLevel;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialEditSaver;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.material.structure.StructureInformationSaver;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.util.chemistry.Calculator;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialServiceTest extends TestBase {

    @Inject
    private MaterialService instance;

    @Inject
    private ProjectService projectService;

    @Inject
    private ACListService aclistService;

    @Inject
    private TaxonomyService taxoService;

    @Inject
    private TaxonomyNestingService taxonomyNestingService;

    private CreationTools creationTools;
    private User publicUser;

    String hazardStatement = "HazardStatement - Text";
    String precautionaryStatement = "PrecautionaryStatement - Text";
    String storageClassRemark = "storageClassRemark";

    @Before
    public void init() {
        creationTools = new CreationTools(hazardStatement, precautionaryStatement, storageClassRemark, memberService, projectService);
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        instance.setStructureInformationSaver(new StructureInformationSaverMock(instance.getEm()));
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        // instance.setEditedMaterialSaver(new MaterialEditSaver(instance, taxonomyNestingService));
    }

    @Test
    public void test001_saveStructure() {
        // Initialisieng the userbean for ownership of material
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
      
        //Preparing project and material
        Project p = creationTools.createProject();
        Structure m = creationTools.createStructure(p);
        m.setMolecule(new Molecule("CCOCC", 0));
        Calculator calc = new Calculator();
        m = calc.calculate(m);
        Integer idOfMatGeneralRights = p.getUserGroups().getId();

        instance.saveMaterialToDB(m, idOfMatGeneralRights, p.getDetailTemplates(),publicUser);

        String ownerId = userBean.getCurrentAccount().getId().toString();
        List materials = entityManagerService.doSqlQuery(
                "select materialtypeid,"
                + "ctime,"
                + "cast(aclist_id as varchar),"
                + "cast(owner_id as varchar),"
                + "projectid"
                + " from materials");
        Assert.assertEquals(1, materials.size());
        Object[] material = (Object[]) materials.get(0);

        //Checking the materials table entry
        Assert.assertEquals("MaterialType ID must be 1 (Structure)", 1, (int) material[0]);
        Assert.assertNotNull("Creation Date of material must be set", material[1]);
        Assert.assertEquals("ID of general rights not expected one", idOfMatGeneralRights.toString(), material[2]);
        Assert.assertEquals("ID of owner not expected one", ownerId, material[3]);
        Assert.assertEquals("ID of project not expected one", (int) p.getId(), (int) material[4]);

        //Checking the structure  table entry
        List structures = entityManagerService.doSqlQuery("select sumformula,molarmass,exactmolarmass,moleculeid from structures");
        Assert.assertEquals(1, structures.size());
        Object[] structure = (Object[]) structures.get(0);
        Assert.assertEquals("expected C4H10O", "C4H10O", (String) structure[0]);
        Assert.assertEquals("expected roughly 74", 74.0, (double) structure[1], 0.2);
        Assert.assertEquals("expected roughly 74", 74.0, (double) structure[2], 0.2);

        //Checking the hazard information
        List hazards = entityManagerService.doSqlQuery("select typeid,remarks from material_hazards where materialid=" + m.getId() + " order by typeid asc");
        Assert.assertEquals(4, hazards.size());

        Object[] h1 = (Object[]) hazards.get(0);
        Object[] h2 = (Object[]) hazards.get(1);
        Object[] h3 = (Object[]) hazards.get(2);
        Object[] h4 = (Object[]) hazards.get(3);
        Assert.assertEquals("ID of first hazard must be 7 (Irritant)", 7, (int) h1[0]);
        Assert.assertEquals("ID of first hazard must be 11 (attention)", 11, (int) h2[0]);
        Assert.assertEquals("ID of first hazard must be 12 (HazardStatement)", 12, (int) h3[0]);
        Assert.assertEquals("Text of HazardStatement not valide", hazardStatement, h3[1]);
        Assert.assertEquals("ID of first hazard must be 13 (PrecautionaryStatement)", 13, (int) h4[0]);
        Assert.assertEquals("Text of PrecautionaryStatement not valide", precautionaryStatement, h4[1]);

        //Checking the storage information
        List storageInfosInDB = entityManagerService.doSqlQuery(
                "select storageclass,description"
                + " from storages "
                + "where materialid=" + m.getId());
        Assert.assertEquals(1, storageInfosInDB.size());
        Object[] s = (Object[]) storageInfosInDB.get(0);
        Assert.assertEquals("ID storageclass must be 1", 1, (int) s[0]);
        Assert.assertEquals("Remark of storage not valide", storageClassRemark, s[1]);
        List storageProperties = entityManagerService.doSqlQuery(
                "select conditionid"
                + " from storageconditions_storages"
                + " where materialid=" + m.getId()
                + " order by conditionid");

        Assert.assertEquals(2, storageProperties.size());
        Assert.assertEquals("ID storagecondition must be 9 (keep cool)", 9, (int) storageProperties.get(0));
        Assert.assertEquals("ID storagecondition must be 10 (keep frozen)", 10, (int) storageProperties.get(1));

        //Checking the rights of details
        List detailRights = entityManagerService.doSqlQuery("select cast(aclistid as varchar),materialtypeid from materialdetailrights  where materialid=" + m.getId() + " order by materialtypeid asc");
        Assert.assertEquals(5, detailRights.size());
        Object[] dr1 = (Object[]) detailRights.get(0);
        Object[] dr2 = (Object[]) detailRights.get(1);
        Object[] dr3 = (Object[]) detailRights.get(2);
        Object[] dr4 = (Object[]) detailRights.get(3);
        Object[] dr5 = (Object[]) detailRights.get(4);

        Assert.assertEquals(MaterialDetailType.COMMON_INFORMATION, MaterialDetailType.getTypeById((int) dr1[1]));
        Assert.assertEquals(p.getUserGroups().getId().toString(), dr1[0]);
        Assert.assertEquals(MaterialDetailType.STRUCTURE_INFORMATION, MaterialDetailType.getTypeById((int) dr2[1]));
        Assert.assertEquals(p.getUserGroups().getId().toString(), dr2[0]);
        Assert.assertEquals(MaterialDetailType.INDEX, MaterialDetailType.getTypeById((int) dr3[1]));
        Assert.assertEquals(p.getUserGroups().getId().toString(), dr3[0]);
        Assert.assertEquals(MaterialDetailType.HAZARD_INFORMATION, MaterialDetailType.getTypeById((int) dr4[1]));
        Assert.assertEquals(p.getUserGroups().getId().toString(), dr4[0]);
        Assert.assertEquals(MaterialDetailType.STORAGE_CLASSES, MaterialDetailType.getTypeById((int) dr5[1]));
        Assert.assertEquals(p.getUserGroups().getId().toString(), dr5[0]);

        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 10);
        requestBuilder.addMaterialType(MaterialType.STRUCTURE);
        requestBuilder.setStructure("COC");
        SearchResult result = instance.getReadableMaterials(requestBuilder.build());

        cleanMaterialsFromDB();
        cleanProjectFromDB(p, false);

    }

    //Mocking the structureSaver to get the id
    @Test
    public void test002_updateStructure() throws Exception {

        Project p = creationTools.createProject();
        Project p2 = creationTools.createProject();

        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
       

        Structure structure = creationTools.createStructure(p);

        //Set initial storage parameter
        structure.getStorageInformation().setStorageClass(new StorageClass(1, "storage class 1"));
        structure.getStorageInformation().setAcidSensitive(true);
        //set initial indices & names
        structure.getNames().add(new MaterialName("name1_before_edit", "en", 0));
        structure.getNames().add(new MaterialName("name2_before_edit", "de", 1));
        structure.getIndices().add(new IndexEntry(1, "index of type 1", null));
        structure.getIndices().add(new IndexEntry(2, "index of type 2", null));
        //set initial hazard information
        structure.getHazards().setDanger(true);
        structure.getHazards().setHighlyFlammable(true);
        //set initial structure information
        structure.setSumFormula("H2O");
        structure.setAverageMolarMass(10d);
        structure.setExactMolarMass(9d);
        structure.setMolecule(new Molecule("molecule-model", 0));

        //Save the material to the db
        instance.saveMaterialToDB(structure, p.getUserGroups().getId(), p.getDetailTemplates(),publicUser);

        //Create a second material
        Structure editedMaterial = (Structure) structure.copyMaterial();
        //Change the storage parameter
        editedMaterial.getStorageInformation().setStorageClass(new StorageClass(2, "storage class 2"));
        editedMaterial.getStorageInformation().setKeepCool(false);
        editedMaterial.getStorageInformation().setAwayFromOxidants(true);
        //Change indices & names
        editedMaterial.getNames().clear();
        editedMaterial.getNames().add(new MaterialName("name2_after_edit", "de", 0));
        editedMaterial.getNames().add(new MaterialName("name1_after_edit", "en", 1));
        editedMaterial.getIndices().remove(1);
        //Change initial hazard information
        editedMaterial.getHazards().setExplosive(true);
        editedMaterial.getHazards().setHighlyFlammable(false);
        //Change  structure information
        editedMaterial.setSumFormula("H3O");
        editedMaterial.setAverageMolarMass(11d);
        editedMaterial.setExactMolarMass(10d);
        editedMaterial.setMolecule(new Molecule("molecule-model after edit", 1));

        //Change the owner and project
        User u2 = createUser("user2", "user2");
        editedMaterial.setOwner(u2);
        editedMaterial.setProjectId(p2.getId());

        instance.saveEditedMaterial(
                editedMaterial,
                structure,
                p.getUserGroups().getId(),
                u2.getId()
        );

        MaterialHistory history = instance.loadHistoryOfMaterial(structure.getId());
        Date changeDate = history.getChanges().keySet().iterator().next();

        MaterialStorageDifference storageDiff = history.getDifferenceOfTypeAtDate(MaterialStorageDifference.class, changeDate);
        Assert.assertNotNull(storageDiff);
        Assert.assertEquals(1, (long) storageDiff.getStorageclassOld());
        Assert.assertEquals(2, (long) storageDiff.getStorageclassNew());

        Integer keepCoolIndex = storageDiff.getStorageConditionsOld().indexOf(StorageCondition.keepCool);
        Assert.assertTrue(keepCoolIndex > -1);
        Assert.assertNull(storageDiff.getStorageConditionsNew().get(keepCoolIndex));

        Integer awayFromOxydantsIndex = storageDiff.getStorageConditionsNew().indexOf(StorageCondition.awayFromOxidants);
        Assert.assertTrue(awayFromOxydantsIndex > -1);
        Assert.assertNull(storageDiff.getStorageConditionsOld().get(awayFromOxydantsIndex));

        Material secondEditedMaterial = editedMaterial.copyMaterial();
        secondEditedMaterial.getStorageInformation().setKeepFrozen(false);
        instance.saveEditedMaterial(
                secondEditedMaterial,
                editedMaterial,
                p.getUserGroups().getId(),
                u2.getId()
        );

        history = instance.loadHistoryOfMaterial(structure.getId());
        Iterator<Date> iter = history.getChanges().keySet().iterator();

        if (iter.hasNext()) {
            iter.next();
        }
        if (iter.hasNext()) {
            changeDate = iter.next();

            storageDiff = history.getDifferenceOfTypeAtDate(MaterialStorageDifference.class, changeDate);
            Assert.assertNotNull(storageDiff);
            Integer keepFrozenIndex = storageDiff.getStorageConditionsOld().indexOf(StorageCondition.keepFrozen);
            Assert.assertTrue(keepFrozenIndex > -1);
            Assert.assertNull(storageDiff.getStorageConditionsNew().get(keepFrozenIndex));
        }

        cleanMaterialsFromDB();

        cleanProjectFromDB(p,
                false);
        cleanProjectFromDB(p2,
                false);
    }

    @Test
    public void test003_updateHazardInformation() throws Exception {
        cleanMaterialsFromDB();
        Project p = creationTools.createProject();
        User user = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(user);
       

        Structure m = creationTools.createStructure(p);
        m.setHazards(new HazardInformation());
        m.getHazards().setIrritant(true);
        m.getHazards().setAttention(true);
        m.getHazards().setHazardStatements("H-Statement before Edit");

        instance.saveMaterialToDB(m, p.getUserGroups().getId(), p.getDetailTemplates(),publicUser);

        //Create a second material
        Structure editedMaterial = (Structure) m.copyMaterial();
        editedMaterial.getHazards().setHazardStatements("H-Statement after Edit");
        editedMaterial.getHazards().setPrecautionaryStatements("P-Statement after Edit");
        editedMaterial.getHazards().setIrritant(false);
        editedMaterial.getHazards().setPoisonous(true);

        instance.saveEditedMaterial(
                editedMaterial, m, p.getUserGroups().getId(),
                user.getId()
        );
        List hazardHists = entityManagerService.doSqlQuery("select materialid,typeid_old,typeid_new ,remarks_old,remarks_new from material_hazards_hist order by typeid_old,typeid_new");
        Assert.assertEquals(4, hazardHists.size());

        Object[] poisonousEntry = (Object[]) hazardHists.get(0);
        Assert.assertEquals("Testcase 3.1 - materialId of historyentry must be 1", editedMaterial.getId(), poisonousEntry[0]);
        Assert.assertNull("Testcase 3.1 - old typeid of poisonous must be null, poisonous was added", poisonousEntry[1]);
        Assert.assertEquals("Testcase 3.1 - new typeid of poisonous must be 6, poisonous was added", Hazard.poisonous.getTypeId(), poisonousEntry[2]);
        Assert.assertNull("Testcase 3.1 - no remarks expected", poisonousEntry[3]);
        Assert.assertNull("Testcase 3.1 - no remarks expected", poisonousEntry[4]);

        Object[] irritantEntry = (Object[]) hazardHists.get(2);
        Assert.assertEquals("Testcase 3.2 - materialId of historyentry must be 1", editedMaterial.getId(), irritantEntry[0]);
        Assert.assertEquals("Testcase 3.2 - old typeid of irritant must be 7, irritant was removed", Hazard.irritant.getTypeId(), irritantEntry[1]);
        Assert.assertNull("Testcase 3.2 - new typeid of irritant must be null,irritant was removed", irritantEntry[2]);
        Assert.assertNull("Testcase 3.2 - no remarks expected", irritantEntry[3]);
        Assert.assertNull("Testcase 3.2 - no remarks expected", irritantEntry[4]);

        Object[] hazardStatementsEntry = (Object[]) hazardHists.get(3);
        Assert.assertEquals("Testcase 3.3 - materialId of historyentry must be 1", editedMaterial.getId(), hazardStatementsEntry[0]);
        Assert.assertEquals("Testcase 3.3 - old typeid of hazardStatementsEntry must be 12", HazardInformation.HAZARD_STATEMENT, hazardStatementsEntry[1]);
        Assert.assertEquals("Testcase 3.3 - new typeid of hazardStatementsEntry must be 12", HazardInformation.HAZARD_STATEMENT, hazardStatementsEntry[2]);
        Assert.assertEquals("Testcase 3.3 - old remarks of hazardStatementsEntry must be 'H-Statement before Edit'", "H-Statement before Edit", hazardStatementsEntry[3]);
        Assert.assertEquals("Testcase 3.3 - new remarks of hazardStatementsEntry must be 'H-Statement after Edit'", "H-Statement after Edit", hazardStatementsEntry[4]);

        Object[] precautionaryStatementsEntry = (Object[]) hazardHists.get(1);
        Assert.assertEquals("Testcase 3.4 - materialId of historyentry must be 1", editedMaterial.getId(), precautionaryStatementsEntry[0]);
        Assert.assertNull("Testcase 3.4 - old typeid of precautionaryStatementsEntry must be null", precautionaryStatementsEntry[1]);
        Assert.assertEquals("Testcase 3.4 - new typeid of precautionaryStatementsEntry must be 13", HazardInformation.PRECAUTIONARY_STATEMENT, precautionaryStatementsEntry[2]);
        Assert.assertNull("Testcase 3.4 - old remarks of precautionaryStatementsEntry must be null", precautionaryStatementsEntry[3]);
        Assert.assertEquals("Testcase 3.4 - new remarks of precautionaryStatementsEntry must be 'P-Statement after Edit'", "P-Statement after Edit", precautionaryStatementsEntry[4]);
    }

    @Test
    public void test004_getSimilarMaterialNames() {
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
      
        Project p = creationTools.createProject();
        Structure m = creationTools.createStructure(p);
        Integer idOfMatGeneralRights = p.getUserGroups().getId();
        instance.saveMaterialToDB(m, idOfMatGeneralRights, p.getDetailTemplates(),publicUser);

        List<String> nameSuggestions = instance.getSimilarMaterialNames("Test-Str", userBean.getCurrentAccount());
        Assert.assertEquals(2, nameSuggestions.size());
        nameSuggestions = instance.getSimilarMaterialNames("Test-Structure", userBean.getCurrentAccount());
        Assert.assertEquals(1, nameSuggestions.size());
    }

    @Test
    public void test005_saveTaxonomy() {
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
     
        creationTools = new CreationTools("", "", "", memberService, projectService);
        Project p = creationTools.createProject();

        List<TaxonomyLevel> levels = taxoService.loadTaxonomyLevel();
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("rose", "en", 1));
        names.add(new MaterialName("Rose", "de", 2));
        names.add(new MaterialName("Rosa ", "la", 3));

        Taxonomy t = new Taxonomy(0, names, new HazardInformation(), new StorageClassInformation(), new ArrayList<>(), memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID), new Date());
        t.setLevel(levels.get(0));
        instance.saveMaterialToDB(t, p.getUserGroups().getId(), new HashMap<>(),publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("red rose", "en", 1));
        Taxonomy t2 = new Taxonomy(1, names, new HazardInformation(), new StorageClassInformation(), new ArrayList<>(), publicUser, new Date());
        t2.getTaxHierachy().add(t);
        t2.setLevel(levels.get(1));
        instance.saveMaterialToDB(t2, p.getUserGroups().getId(), new HashMap<>(),publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("small red rose", "en", 1));
        Taxonomy t3 = new Taxonomy(2, names, new HazardInformation(), new StorageClassInformation(), new ArrayList<>(),publicUser, new Date());
        t3.getTaxHierachy().add(t);
        t3.getTaxHierachy().add(t2);
        t3.setLevel(levels.get(2));
        instance.saveMaterialToDB(t3, p.getUserGroups().getId(), new HashMap<>(),publicUser);

        List<Integer> results = (List) entityManagerService.doSqlQuery("SELECT parentid FROM effective_taxonomy WHERE taxoid=0");
        Assert.assertTrue(results.isEmpty());

        results = (List) entityManagerService.doSqlQuery(String.format("SELECT parentid FROM effective_taxonomy WHERE taxoid=%d", t2.getId()));
        Assert.assertEquals(1, results.size());

        results = (List) entityManagerService.doSqlQuery(String.format("SELECT parentid FROM effective_taxonomy WHERE taxoid=%d", t3.getId()));
        Assert.assertEquals(2, results.size());

        cleanMaterialsFromDB();
        cleanProjectFromDB(p, false);
    }

    @Test
    public void test006_getReadableMaterials() {

        User testUser = createUser("testUser", "testUser");
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(testUser);
        membershipService.addMembership(memberService.loadGroupById(GlobalAdmissionContext.PUBLIC_GROUP_ID), testUser);

        creationTools = new CreationTools("", "", "", memberService, projectService);
        Project project1 = creationTools.createAndSaveProject("biochemical-test-project");
        Project project2 = creationTools.createAndSaveProject("administration-test-project");

        //Create a structure
        Structure struture1 = creationTools.createStructure(project1);
        StorageClassInformation si = new StorageClassInformation(instance.loadStorageClasses());
        si.setRemarks("test remarks");
        si.setStorageClass(si.getPossibleStorageClassById(1));
        struture1.setStorageInformation(si);

        instance.saveMaterialToDB(struture1, GlobalAdmissionContext.getPublicReadACL().getId(), project1.getDetailTemplates(),testUser);

        //Create a structure which is not readable
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        ACList noRightsAcl = new ACList();
        noRightsAcl.setName("No Rights");
        noRightsAcl = aclistService.save(noRightsAcl);
        Structure structure2 = creationTools.createStructure(project2);
        structure2.getNames().clear();
        structure2.getNames().add(new MaterialName("structure2", "de", 1));
        structure2.getIndices().clear();
        instance.saveMaterialToDB(structure2, noRightsAcl.getId(), project1.getDetailTemplates(),publicUser);
        userBean.setCurrentAccount(testUser);
        //Create a biomaterial

        //Only one of the two materials must be found because user has no read right for the other one
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);

        requestBuilder.build();
        SearchResult result = instance.getReadableMaterials(requestBuilder.build());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        List<Structure> structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, structures.size());
        Material loadedMaterial = structures.get(0);
        Assert.assertEquals(struture1.getId(), loadedMaterial.getId());

        //load materials of project with name 'biochemical'
        requestBuilder.setProjectName("biochemical");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());
        loadedMaterial = structures.get(0);
        Assert.assertEquals(struture1.getId(), loadedMaterial.getId());

        //load materials of project with name 'xyz'
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setProjectName("xyz");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());

        Assert.assertEquals(0, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(0, structures.size());

        //load only structures
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.addMaterialType(MaterialType.STRUCTURE);
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());
        Assert.assertEquals(struture1.getId(), loadedMaterial.getId());

        //load only biomaterials
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.addMaterialType(MaterialType.BIOMATERIAL);
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(0, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(0, structures.size());

        //load only structures or biomaterials
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.addMaterialType(MaterialType.STRUCTURE);
        requestBuilder.addMaterialType(MaterialType.BIOMATERIAL);
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());
        Assert.assertEquals(struture1.getId(), loadedMaterial.getId());

        //load only materials with name matches pattern 'Test-Struc'
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setMaterialName("Test-Struc");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());

        //load only materials with name matches pattern 'Test-Fail'
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setIndex("Test-Fail");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(0, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(0, structures.size());

        //load material by id
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setId(String.format("%d", struture1.getId()));
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());

        //load material by id without right to read
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setId(String.format("%d", structure2.getId()));
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(0, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(0, structures.size());

        //load material of user
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setUserName(testUser.getName());
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());

        //load material of user (which not exists)
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setUserName("xz");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(0, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(0, structures.size());

        //load material by index
        requestBuilder = new MaterialSearchRequestBuilder(testUser, 0, 25);
        requestBuilder.setIndex("Gest");
        result = instance.getReadableMaterials(requestBuilder.build());
        structures = result.getAllFoundObjects(Structure.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadMaterialAmount(requestBuilder.build()));
        Assert.assertEquals(1, structures.size());
    }

    @Test
    public void test009_saveConsumable() {
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        Project project1 = creationTools.createAndSaveProject("biochemical-test-project");
        
        List<MaterialName> names=new ArrayList<>();
        names.add(new MaterialName("test009_saveConsumable_consumable_name_de", "de", 1));
        names.add(new MaterialName("test009_saveConsumable_consumable_name_en", "en", 1));
        Consumable co=new Consumable(0, names, project1.getId(), new HazardInformation(), new StorageClassInformation());
  
    }

    @Test
    public void test008_loadStorageClasses() {
        List<StorageClass> classes = instance.loadStorageClasses();
        Assert.assertEquals(23, classes.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("MaterialServiceTest.war")
                .addClass(UserBeanMock.class)
                .addClass(ACListService.class)
                .addClass(CollectionBean.class)
                .addClass(CollectionService.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(EntityManagerService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(DocumentSearchService.class)
                .addClass(ACListService.class)
                .addClass(ProjectService.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(Navigator.class)
                .addClass(TaxonomyService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(TissueService.class)
                .addClass(WordCloudBean.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MaterialIndexHistoryEntity.class)
                .addClass(MaterialService.class);
        return UserBeanDeployment.add(deployment);
    }
}
