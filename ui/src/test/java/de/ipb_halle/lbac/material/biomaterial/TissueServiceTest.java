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
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Before
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
         for (Taxonomy t : taxoService.loadTaxonomy(new HashMap<>(), true)) {
            System.out.println(t.getFirstName());
        }
        Taxonomy taxo = taxoService.loadTaxonomy(new HashMap<>(), true).get(3);
        Tissue tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(),publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Hyphen", "de", 1));
        names.add(new MaterialName("flocci, hyphae", "la", 2));
        taxo = taxoService.loadTaxonomy(new HashMap<>(), true).get(1);
        tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(),publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Blüte", "de", 1));
       
        Taxonomy seerose = taxo = taxoService.loadTaxonomy(new HashMap<>(), true).get(11);
        tissue = new Tissue(100, names, seerose);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(),publicUser);

        names = new ArrayList<>();
        names.add(new MaterialName("Stützrippe", "de", 1));
        taxo = taxoService.loadTaxonomy(new HashMap<>(), true).get(18);
        tissue = new Tissue(100, names, taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(),publicUser);

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
        return UserBeanDeployment.add(deployment);
    }
}
