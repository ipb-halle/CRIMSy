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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class TissueServiceTest extends TestBase {

    Project project;
    User owner;
    Integer userGroups;
    Integer ownerid;

    @Inject
    private TaxonomyService taxoService;

    @Inject
    private MaterialService materialService;
    @Inject
    private ProjectService projectService;

    private CreationTools creationTools;
    @Inject
    private TissueService tissueService;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        // Initialisieng the userbean for ownership of material
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ownerid = owner.getId();
        project = creationTools.createProject();
    }

    @Test
    public void test001_saveAndloadTissues() {
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("Wurzel", "de", 1));
        names.add(new MaterialName("Root", "en", 2));
        names.add(new MaterialName("Radix", "la", 3));
        for (Taxonomy t : taxoService.loadTaxonomyByIdAndDepth(1, 99)) {
            System.out.println(t.getFirstName());
        }
        //Filtering by name would be better
        Taxonomy taxo = taxoService.loadTaxonomyByIdAndDepth(1, 99).get(3);
        Tissue tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Hyphen", "de", 1));
        names.add(new MaterialName("flocci, hyphae", "la", 2));
        //Filtering by name would be better
        taxo = taxoService.loadTaxonomyByIdAndDepth(1, 99).get(1);
        tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Blüte", "de", 1));
        //Filtering by name would be better
        Taxonomy seerose = taxoService.loadTaxonomyByIdAndDepth(1, 99).get(11);
        tissue = new Tissue(100, names, seerose);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Stützrippe", "de", 1));
        taxo = taxoService.loadTaxonomyByIdAndDepth(1, 99).get(18);
        tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);

        List<Tissue> loadedTissues = tissueService.loadTissues();
        Assert.assertEquals(4, loadedTissues.size());

        List<Tissue> contrainedTissues = tissueService.loadTissues(seerose);
        Assert.assertEquals(2, contrainedTissues.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("TissueServiceTest.war")
                .addClass(ProjectService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(TissueService.class)
                .addClass(TaxonomyService.class);
        return MaterialDeployment.add(UserBeanDeployment.add(deployment));
    }
}
