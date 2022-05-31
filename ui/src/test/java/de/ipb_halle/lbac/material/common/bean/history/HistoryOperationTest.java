/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common.bean.history;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.SequenceInformation;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public abstract class HistoryOperationTest extends TestBase {

    private static final long serialVersionUID = 1L;
    @Inject
    protected HazardService hazardService;
    @Inject
    protected MaterialService materialService;
    @Inject
    protected TaxonomyService taxonomyService;
    @Inject
    protected TissueService tissueService;
    @Inject
    protected ACListService aclistService;

    protected List<IndexEntry> indices;
    protected BioMaterial biomaterial;
    protected Date currentDate;
    protected MaterialEditState mes;
    protected HistoryOperation instance;
    protected MaterialIndexDifference mid;
    protected MaterialIndexBean mib;
    protected Random random = new Random();
    protected TaxonomySelectionController taxonomyController;
    protected MateriaBeanMock materialBeanMock;
    protected Date d_20001220, d_20001020;
    protected int structureId1, structureId2, biomaterialId;
    protected UserBeanMock userBean;
    protected int publicAclId;
    protected Project project;

    protected MaterialComposition composition;
    protected MaterialCompositionBean compositionBean;
    protected Taxonomy plantsTaxonomy, mushroomsTaxonomy;

    @BeforeEach
    public void init() {
        setUpDates();
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();

        setUpTaxonomy();
        createBioMaterial();

        createCompositionMaterial();
        createMaterialEditState();
        createMaterialBeanMock();
        instance = new HistoryOperation(materialBeanMock);
        materialBeanMock.setHistoryOperation(instance);

    }

    private void setUpTaxonomy() {
        createTaxonomyTreeInDB(publicAclId, publicUser.getId());
        List<Taxonomy> taxonomyList = taxonomyService.loadTaxonomy(new HashMap<>(), true);
        plantsTaxonomy = taxonomyList.get(3);
        mushroomsTaxonomy = taxonomyList.get(1);
    }

    private MaterialEditState createMaterialEditState() {
        mes = new MaterialEditState(
                new Project(),
                currentDate,
                composition,
                composition,
                new MaterialHazardBuilder(hazardService, MaterialType.COMPOSITION, true, new HashMap<>(), MessagePresenterMock.getInstance()),MessagePresenterMock.getInstance());
        mes.setCurrentVersiondate(d_20001220);
        return mes;
    }

    protected BioMaterial createBioMaterial() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("Biomaterial", "en", 0));
        biomaterial = new BioMaterial(biomaterialId, names, project.getId(), new HazardInformation(), new StorageInformation(), taxonomyService.loadRootTaxonomy(), null);
        biomaterial.getHistory().addDifference(createDiffAt20001020());
        biomaterial.getHistory().addDifference(createDiffAt20001220());
        return biomaterial;
    }

    private MaterialComposition createCompositionMaterial() {
        materialCreator = new MaterialCreator(entityManagerService);
        structureId1 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                "CCCCCCCCC",
                project.getId(),
                "Testmaterial-001");
        materialCreator.addIndexToMaterial(structureId1, 2, "Index of material 1");

        structureId2 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project.getId(),
                "Testmaterial-002");

        biomaterial = creationTools.createBioMaterial(project, "BioMaterial001", taxonomyService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(biomaterial, publicAclId, new HashMap<>(), publicUser.getId());
        composition = new MaterialComposition(project.getId(), CompositionType.EXTRACT);
        biomaterialId = biomaterial.getId();
        composition.addComponent(materialService.loadMaterialById(structureId1), 0.5d, Unit.getUnit("g"));
        composition.addComponent(biomaterial, null, null);
        composition.getHistory().addDifference(createDiffAt20001020());
        composition.getHistory().addDifference(createDiffAt20001220());
        composition.getStorageInformation().setStorageClass(new StorageClass(3, ""));
        composition.getStorageInformation().getStorageConditions().add(StorageCondition.keepFrozen);
        composition.getStorageInformation().getStorageConditions().add(StorageCondition.keepTempBelowMinus80Celsius);

        return composition;
    }

    protected void setUpDates() {
        currentDate = new Date();
        Calendar c = new GregorianCalendar();
        c.set(2000, 12, 20);
        d_20001220 = c.getTime();
        c.set(2000, 10, 20);
        d_20001020 = c.getTime();
    }

    private MateriaBeanMock createMaterialBeanMock() {
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        materialBeanMock = new MateriaBeanMock();
        materialBeanMock.setMaterialEditState(mes);
        materialBeanMock.setHazardService(hazardService);
        materialBeanMock.setTaxonomyService(taxonomyService);
        materialBeanMock.setTissueService(tissueService);
        materialBeanMock.setUserBean(userBean);
        materialBeanMock.setMaterialService(materialService);
        materialBeanMock.setAcListService(aclistService);
        materialBeanMock.setHistoryOperation(instance);
        materialBeanMock.setSequenceInfos(new SequenceInformation());
        materialBeanMock.createStorageInformationBuilder(MessagePresenterMock.getInstance(), materialService, composition);
        taxonomyController = new TaxonomySelectionController(taxonomyService, tissueService, biomaterial.getTaxonomy());
        materialBeanMock.setTaxonomyController(taxonomyController);
        compositionBean = new MaterialCompositionBean(materialService, MessagePresenterMock.getInstance(), userBean);
        compositionBean.startCompositionEdit(composition);
        materialBeanMock.setCompositionBean(compositionBean);
        materialBeanMock.setHazardController(new MaterialHazardBuilder(hazardService, MaterialType.COMPOSITION, true, new HashMap<>(),
                MessagePresenterMock.getInstance()));
        return materialBeanMock;
    }

    protected abstract MaterialDifference createDiffAt20001220();

    protected abstract MaterialDifference createDiffAt20001020();

    protected abstract void checkCurrentState();

    protected abstract void checkStateAt20001020();

    protected abstract void checkStateAt20001220();
}
