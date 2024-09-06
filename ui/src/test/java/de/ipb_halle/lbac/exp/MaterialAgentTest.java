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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.mocks.ExperimentBeanMock;
import de.ipb_halle.lbac.exp.mocks.ItemAgentMock;
import de.ipb_halle.lbac.exp.mocks.MaterialAgentMock;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialDetailRight;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.util.ResourceUtils;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class MaterialAgentTest extends TestBase {

    private static final long serialVersionUID = 1L;

    private ACList publicReadAcl;
    private MaterialAgentMock materialAgent;
    private UserBeanMock userBean;

    @Inject
    private MaterialService materialService;

    @Inject
    private ExpRecordService expRecordService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private TaxonomyService taxonomyService;
    @Inject
    private ItemService itemService;

    private ExpRecordController holder;
    private ExperimentBeanMock experimentBean;

    private Taxonomy taxo1;
    private Project project;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        createTaxonomyTreeInDB(publicReadAcl.getId(), publicUser.getId());
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);

        materialAgent = new MaterialAgentMock();
        materialAgent.setGlobalAdmissionContext(context);
        materialAgent.setMaterialService(materialService);
        materialAgent.setUserBean(userBean);

        ItemAgent itemAgentMock = new ItemAgentMock()
                .setGlobalAdmissionContext(context)
                .setItemService(this.itemService)
                .setUserBean(userBean);

        experimentBean = new ExperimentBeanMock()
                .setExpRecordService(expRecordService)
                .setExperimentService(experimentService)
                .setGlobalAdmissionContext(context)
                .setMaterialAgent(materialAgent)
                .setMemberService(memberService)
                .setItemAgent(itemAgentMock);
        experimentBean.experimentBeanInit();
        experimentBean.setCurrentAccount(new LoginEvent(publicUser));

        Experiment experiment = new Experiment(null,
                "code",
                "desc",
                false,
                this.context.getPublicReadACL(), // aclist
                this.context.getPublicAccount(),
                new Date());
        Assay assay = new Assay();
        assay.setExperiment(experiment);
        assay.setEdit(true);
        experimentBean.setExperiment(experiment);
        experimentBean.setExpRecordIndex(0);
        experimentBean.getExpRecords().add(assay);
        experimentBean.createExpRecordController("ASSAY");

        holder = experimentBean.getExpRecordController();

        materialAgent.setMaterialHolder(holder);
        taxo1 = taxonomyService.loadRootTaxonomy();
        project = creationTools.createProject();

    }

    @Test
    public void test001_actionTriggerMaterialSearch() throws IOException {
        String benzene = ResourceUtils.readResourceFile("molfiles/Benzene.mol");
        holder.setLinkedDataIndex(0);   // select the assay target record
        List<Material> materials = materialAgent.getMaterialList();

        Assert.assertTrue("materials list should be empty ", materials.isEmpty());

        createBiomaterial(taxo1, project, "BioMat1", "Mouse Kidney");
        materialAgent.actionTriggerMaterialSearch();
        materials = materialAgent.getMaterialList();
        Assert.assertEquals("materials list size should be 1", 1, materials.size());

        createBiomaterial(taxo1, project, "BioMat2", "Mouse Kidney");
        createBiomaterial(taxo1, project, "BioMat3", "Mouse Liver");
        createBiomaterial(taxo1, project, "BioMat4", "Cat Kidney");
        createBiomaterial(taxo1, project, "BioMat5", "Cat Liver");
        createBiomaterial(taxo1, project, "BioMat6", "Dog Kidney");
        createBiomaterial(taxo1, project, "BioMat7", "Dog Liver");
        materialAgent.actionTriggerMaterialSearch();
        materials = materialAgent.getMaterialList();
        Assert.assertEquals("materials list size should be 5", 5, materials.size());

        materialAgent.clearAgent();
        materialAgent.setMoleculeSearch(benzene);
        Assert.assertEquals(benzene, materialAgent.getMoleculeSearch());
        materialAgent.actionTriggerMaterialSearch();
        Assert.assertEquals(0, materialAgent.getMaterialList().size());

        materialAgent.clearAgent();
        materialAgent.setMaterialSearch("Cat");
        Assert.assertEquals("Cat", materialAgent.getMaterialSearch());
        materialAgent.actionTriggerMaterialSearch();
        Assert.assertEquals(2, materialAgent.getMaterialList().size());

    }

    @Test
    public void test002_actionSetMaterial() {

        MaterialHolder materialHolder = materialAgent.getMaterialHolder();

        holder.setLinkedDataIndex(0);   // select the assay target record
        createBiomaterial(taxo1, project, "BioMat1", "Mouse Kidney");
        materialAgent.actionTriggerMaterialSearch();
        materialAgent.actionSetMaterial(materialAgent.getMaterialList().get(0));

        Assert.assertEquals(materialAgent.getMaterialList().get(0).getId(), materialHolder.getMaterial().getId());

    }

    @AfterEach
    public void cleanUp() {

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("MaterialAgentTest.war")
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(ItemAgent.class)
                .addClass(MaterialAgent.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(MaterialService.class)
                .addClass(ProjectService.class);
        return ExperimentDeployment.add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }

    private BioMaterial createBiomaterial(Taxonomy taxo, Project project, String... names) {
        List<MaterialName> nameList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            nameList.add(new MaterialName(names[i], "de", i + 1));
        }
        BioMaterial biomaterial = new BioMaterial(0, nameList, project.getId(), new HazardInformation(), new StorageInformation(), taxo, null);
        MaterialDetailRight detailRight = new MaterialDetailRight();
        detailRight.setType(MaterialDetailType.INDEX);
        detailRight.setAcList(acListReadable);
        Map<MaterialDetailType, ACList> rights = new HashMap<>();
        rights.put(MaterialDetailType.INDEX, publicReadAcl);
        biomaterial.getDetailRights().add(detailRight);
        materialService.saveMaterialToDB(biomaterial, project.getUserGroups().getId(), rights, publicUser);
        return biomaterial;

    }
}
