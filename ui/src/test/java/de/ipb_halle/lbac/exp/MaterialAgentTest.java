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
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayController;
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
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialAgentTest extends TestBase {

    private User publicUser;
    private ACList publicReadAcl;
    private MaterialAgentMock materialAgent;
    private UserBeanMock userBean;

    @Inject
    private ProjectService projectService;

    @Inject
    private MaterialService materialService;

    @Inject
    private GlobalAdmissionContext context;

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

    @Before
    @Override
    public void setUp() {
        super.setUp();

        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        createTaxonomyTreeInDB(publicReadAcl.getId(), publicUser.getId());
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);

        materialService.setUserBean(userBean);

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
        taxo1 = taxonomyService.loadTaxonomy(new HashMap<>(), false).get(0);
        project = creationTools.createProject();

    }

    @Test
    public void testLoadMaterials() {
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

    }

    @After
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
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

    private BioMaterial createBiomaterial(Taxonomy taxo, Project project, String... names) {
        List<MaterialName> nameList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            nameList.add(new MaterialName(names[i], "de", i + 1));
        }
        BioMaterial biomaterial = new BioMaterial(0, nameList, project.getId(), new HazardInformation(), new StorageClassInformation(), taxo, null);
        MaterialDetailRight detailRight = new MaterialDetailRight();
        detailRight.setType(MaterialDetailType.INDEX);
        detailRight.setAcList(acListReadable);
        Map<MaterialDetailType,ACList> rights=new HashMap<>();
        rights.put(MaterialDetailType.INDEX, publicReadAcl);
        biomaterial.getDetailRights().add(detailRight);
        materialService.saveMaterialToDB(biomaterial, project.getUserGroups().getId(), rights);
        return biomaterial;

    }

    private void createStructure(String name) {

    }

}
