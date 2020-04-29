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
package de.ipb_halle.lbac.material.service;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import de.ipb_halle.lbac.material.subtype.taxonomy.TaxonomyLevel;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
public class TaxonomyServiceTest extends TestBase {

    Project project;
    User owner;
    String userGroups;
    String ownerid;

    @Inject
    private TaxonomyService service;

    @Inject
    private MaterialService materialService;
    @Inject
    private ProjectService projectService;

    private CreationTools creationTools;

    String INSERT_MATERIAL_SQL = "INSERT INTO MATERIALS VALUES("
            + "%d,"
            + "7,"
            + "now(),"
            + "cast('%s' as UUID),"
            + "cast('%s' as UUID),"
            + "false,%d)";

    @Before
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        // Initialisieng the userbean for ownership of material
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID)));
        materialService.setUserBean(userBean);
    }

    @Test
    public void test001_loadTaxonomyLevels() {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        Assert.assertEquals("test001: 8 levels must be found", 8, levels.size());
    }

    @Test
    public void test002_loadTaxonomies() {
        createAndSaveTaxonomies();
        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals(3, taxonomies.size());
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("level", 2);
        taxonomies = service.loadTaxonomy(cmap, true);
        Assert.assertEquals(2, taxonomies.size());
        cleanTaxonomyFromDb();
    }

    private void createAndSaveTaxonomies() {
        owner = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId().toString();
        ownerid = owner.getId().toString();
        createTaxanomy(1, "Tax_1", 1);
        createTaxanomy(2, "Tax_2.1", 2, 1);
        createTaxanomy(3, "Tax_2.2", 2, 1);

    }

    private void createTaxanomy(int id, String name, int level, Integer... parents) {
        entityManagerService.doSqlUpdate(String.format(INSERT_MATERIAL_SQL, id, userGroups, ownerid, project.getId()));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO taxonomy  VALUES(%d ,%d)", id, level));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO storages VALUES(%d,1,'')", id));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO material_indices(materialid, typeid,value,language,rank) VALUES(%d,1,'" + name + "_de','de',0)", id));
        for (Integer parent : parents) {
            entityManagerService.doSqlUpdate(String.format("INSERT INTO effective_taxonomy(taxoid,parentid) VALUES(%d,%d)", id, parent));
        }

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TaxonomyServiceTest.war")
                .addClass(ProjectService.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(MaterialService.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(LdapProperties.class)
                .addClass(KeyManager.class)
                .addClass(TaxonomyService.class);
    }
}
