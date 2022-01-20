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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
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
@ExtendWith(ArquillianExtension.class)
public class BiomaterialServiceTest extends TestBase {

    private static final long serialVersionUID = 1L;

    Project project;
    User owner;
    String userGroups;
    String ownerid;

    @Inject
    private TaxonomyService taxonomyService;

    @Inject
    private MaterialService materialService;   

    @Inject
    private ProjectService projectService;

    private CreationTools creationTools;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        // Initialisieng the userbean for ownership of material
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ownerid = owner.getId().toString();
        project = creationTools.createProject();
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());
    }

    @AfterEach
    public void finish() {
        cleanMaterialsFromDB();
    }

    @Test
    public void test001_saveAndLoadBioMaterials() {
        String materialName = "Löwnzahn";
        Taxonomy taxo = taxonomyService.loadTaxonomy(new HashMap<>(), true).get(15);
        Tissue tissue = saveTissueInDB(taxo);
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName(materialName, "de", 1));
        BioMaterial biomaterial = new BioMaterial(0, names, project.getId(), new HazardInformation(), new StorageInformation(), taxo, tissue);
        ACList materialACList = project.getUserGroups();
        materialService.saveMaterialToDB(biomaterial, materialACList.getId(), new HashMap<>(), publicUser);
        MaterialSearchRequestBuilder requestBuilder = new MaterialSearchRequestBuilder(owner, 0, 100);
        requestBuilder.addMaterialType(MaterialType.BIOMATERIAL);
        SearchResult result = materialService.loadReadableMaterials(requestBuilder.build());
        List<BioMaterial> bioMaterials = result.getAllFoundObjects(BioMaterial.class, result.getNode());
        Assert.assertEquals(1, bioMaterials.size());
        BioMaterial bm = bioMaterials.get(0);
        Assert.assertEquals(materialACList.getId(), bm.getACList().getId());
        Assert.assertNotNull(bm.getCreationTime());
        Assert.assertEquals(0, bm.getDetailRights().size());
        Assert.assertEquals(materialName, bm.getFirstName());
        Assert.assertNotNull(bm.getHazards());
        Assert.assertEquals(0, bm.getHistory().getChanges().size());
        Assert.assertEquals(0, bm.getIndices().size());
        Assert.assertEquals(materialName, bm.getNameToDisplay());
        Assert.assertEquals(1, bm.getNames().size());
        Assert.assertEquals(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID, bm.getOwner().getId());
        Assert.assertEquals(project.getId(), bm.getProjectId(), 0);
        Assert.assertNotNull(bm.getStorageInformation());
        Assert.assertEquals(taxo.getId(), bm.getTaxonomy().getId());
        Assert.assertEquals(taxo.getId(), bm.getTaxonomyId(), 0);
        Assert.assertEquals(tissue.getId(), bm.getTissueId(), 0);
        Assert.assertEquals(tissue.getNameToDisplay(), bm.getTissueName());
        Assert.assertEquals(MaterialType.BIOMATERIAL, bm.getType());
        Assert.assertEquals(SearchTarget.MATERIAL, bm.getTypeToDisplay().getGeneralType());

    }

    @Test
    public void test002_editBioMaterial() throws Exception {

        Taxonomy taxo = taxonomyService.loadTaxonomy(new HashMap<>(), true).get(15);
        Tissue tissue = saveTissueInDB(taxo);
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("Löwnzahn", "de", 1));
        BioMaterial biomaterial = new BioMaterial(0, names, project.getId(), new HazardInformation(), new StorageInformation(), taxo, tissue);
        biomaterial.setACList(project.getUserGroups());
        biomaterial.setOwner(owner);

        materialService.saveMaterialToDB(biomaterial, project.getUserGroups().getId(), new HashMap<>(), publicUser);

        BioMaterial editedBioMaterial = biomaterial.copyMaterial();
        editedBioMaterial.setTissue(null);
        Taxonomy newTaxonomy = taxonomyService.loadTaxonomy(new HashMap<>(), true).get(12);
        editedBioMaterial.setTaxonomy(newTaxonomy);
        materialService.saveEditedMaterial(editedBioMaterial, biomaterial, project.getUserGroups().getId(), owner.getId());

        BioMaterial m = (BioMaterial) materialService.loadMaterialById(editedBioMaterial.getId());
        Assert.assertEquals(newTaxonomy.getId(), m.getTaxonomy().getId());
        Assert.assertNull(m.getTissueId());

        Assert.assertFalse(m.getHistory().getChanges().isEmpty());
    }

    private Tissue saveTissueInDB(Taxonomy taxo) {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("Wurzel", "de", 1));
        names.add(new MaterialName("Root", "en", 2));
        names.add(new MaterialName("Radix", "la", 3));
        Tissue tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);
        return tissue;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("BiomaterialServiceTest.war");
        return MaterialDeployment.add(UserBeanDeployment.add(deployment));
    }
}
