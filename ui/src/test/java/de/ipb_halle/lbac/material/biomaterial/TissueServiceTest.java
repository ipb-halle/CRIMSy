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
import java.util.Arrays;
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
    public void test001_loadTissues() {
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());
        List<Taxonomy> taxonomies = taxoService.loadTaxonomyByIdAndDepth(1, 99);
        Taxonomy champignonTaxo = getTaxonomyByName("Champignonartige_de", taxonomies);
        Taxonomy mushroomTaxo = getTaxonomyByName("Pilze_de", taxonomies);
        Taxonomy waterLillyKind = getTaxonomyByName("Seerosenartige_de", taxonomies);
        Taxonomy waterLilly = getTaxonomyByName("Seerosengewächse_de", taxonomies);

        createTissue(champignonTaxo, "Wurzel", "root", "radix");
        createTissue(mushroomTaxo, "Hyphen", "flocci, hyphae");
        createTissue(waterLillyKind, "Blüte");
        createTissue(waterLillyKind, "Stützrippe");

        List<Tissue> loadedTissues = tissueService.loadTissues();
        Assert.assertEquals(4, loadedTissues.size());

        List<Tissue> contrainedTissues = tissueService.loadTissues(waterLilly);
        Assert.assertEquals(2, contrainedTissues.size());
    }

    private Tissue createTissue(Taxonomy taxo, String... names) {
        Tissue tissue = new Tissue(100, createMaterialNames(names), taxo);
        materialService.saveMaterialToDB(tissue, project.getUserGroups().getId(), new HashMap<>(), publicUser);
        return tissue;
    }

    private Taxonomy getTaxonomyByName(String name, List<Taxonomy> taxonomies) {
        for (Taxonomy t : taxonomies) {
            if (t.getNames().stream().map(tName -> tName.getValue()).toList().contains(name)) {
                return t;
            }
        }
        throw new RuntimeException("Could not find taxonomy with name " + name);
    }

    private List<MaterialName> createMaterialNames(String... names) {
        List<MaterialName> materialNames = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            materialNames.add(new MaterialName(names[i], "language " + i, i + 1));
        }

        return materialNames;
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
