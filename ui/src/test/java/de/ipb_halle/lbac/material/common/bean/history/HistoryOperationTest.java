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
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.IndexEntry;
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
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.util.Unit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
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
    protected MaterialBeanMock materialBeanMock;
    protected Date d_20001220, d_20001020;
    protected int structureId1, structureId2, biomaterialId;
    protected UserBeanMock userBean;
    protected int publicAclId;
    protected Project project;

    protected MaterialComposition composition;
    protected MaterialCompositionBean compositionBean;

    @Before
    public void init() {
        setUpDates();
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        project.setOwner(publicUser);
        project.setACList(GlobalAdmissionContext.getPublicReadACL());
        projectService.saveProjectToDb(project);
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();
        createTaxonomyTreeInDB(publicAclId, publicUser.getId());
        createCompositionMaterial();
        createMaterialEditState();
        createMaterialBeanMock();
        instance = new HistoryOperation(materialBeanMock);

    }

    private MaterialEditState createMaterialEditState() {
        mes = new MaterialEditState(
                new Project(),
                currentDate,
                composition,
                composition,
                new MaterialHazardBuilder(hazardService, MaterialType.COMPOSITION, true, new HashMap<>(), MessagePresenterMock.getInstance()));
        mes.setCurrentVersiondate(d_20001220);
        return mes;
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

        return composition;
    }

    private void setUpDates() {
        currentDate = new Date();
        Calendar c = new GregorianCalendar();
        c.set(2000, 12, 20);
        d_20001220 = c.getTime();
        c.set(2000, 10, 20);
        d_20001020 = c.getTime();
    }

    private MaterialBeanMock createMaterialBeanMock() {
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        materialBeanMock = new MaterialBeanMock();
        materialBeanMock.setMaterialEditState(mes);
        materialBeanMock.setHazardService(hazardService);
        materialBeanMock.setTaxonomyService(taxonomyService);
        materialBeanMock.setTissueService(tissueService);
        materialBeanMock.setUserBean(userBean);
        materialBeanMock.setMaterialService(materialService);
        materialBeanMock.setAcListService(aclistService);
        taxonomyController = new TaxonomySelectionController(taxonomyService, tissueService, biomaterial.getTaxonomy());
        materialBeanMock.setTaxonomyController(taxonomyController);
        compositionBean = new MaterialCompositionBean(materialService, MessagePresenterMock.getInstance(), userBean);
        compositionBean.startCompositionEdit(composition);
        materialBeanMock.setCompositionBean(compositionBean);
        return materialBeanMock;
    }

    protected abstract MaterialDifference createDiffAt20001220();

    protected abstract MaterialDifference createDiffAt20001020();

    protected abstract void checkCurrentState();

    protected abstract void checkStateAt20001020();

    protected abstract void checkStateAt20001220();
}
