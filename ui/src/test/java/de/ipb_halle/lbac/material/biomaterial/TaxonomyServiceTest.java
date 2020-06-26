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
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private TaxonomyNestingService nestingService;

    @Inject
    private TaxonomyService service;

    @Inject
    private MaterialService materialService;
    @Inject
    private ProjectService projectService;

    private CreationTools creationTools;

    @Before
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        // Initialisieng the userbean for ownership of material
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID)));
        owner = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));

        ownerid = owner.getId().toString();
        materialService.setUserBean(userBean);
    }

    @Test
    public void test001_loadTaxonomyLevels() {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        Assert.assertEquals("test001: 21 levels must be found", 21, levels.size());
    }

    @Test
    public void test002_loadTaxonomies() {
        project = creationTools.createProject();
        createTaxonomyTreeInDB(project.getUserGroups().getId().toString(), owner.getId().toString());
        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals("test001: 21 taxonomies must be found", 21, taxonomies.size());

        Taxonomy life = taxonomies.get(0);
        Assert.assertTrue(life.getTaxHierachy().isEmpty());
        Assert.assertEquals(1, life.getLevel().getId());
        Assert.assertEquals("Leben_de", life.getFirstName());

        Taxonomy wulstlinge = taxonomies.get(12);
        Assert.assertEquals(7, wulstlinge.getLevel().getId());
        Assert.assertEquals("Wulstlinge_de", wulstlinge.getFirstName());
        Assert.assertEquals(6, wulstlinge.getId());
        Assert.assertEquals(5, wulstlinge.getTaxHierachy().size());
        Assert.assertEquals(5, wulstlinge.getTaxHierachy().get(0).getId());
        Assert.assertEquals(4, wulstlinge.getTaxHierachy().get(1).getId());
        Assert.assertEquals(3, wulstlinge.getTaxHierachy().get(2).getId());
        Assert.assertEquals(2, wulstlinge.getTaxHierachy().get(3).getId());
        Assert.assertEquals(1, wulstlinge.getTaxHierachy().get(4).getId());

        Taxonomy ohrlappenpilze = taxonomies.get(10);
        Assert.assertEquals(6, ohrlappenpilze.getLevel().getId());
        Assert.assertEquals("Gallerttränenverwandte_de", ohrlappenpilze.getFirstName());
        Assert.assertEquals(11, ohrlappenpilze.getId());
        Assert.assertEquals(3, ohrlappenpilze.getTaxHierachy().size());
        Assert.assertEquals(8, ohrlappenpilze.getTaxHierachy().get(0).getId());
        Assert.assertEquals(2, ohrlappenpilze.getTaxHierachy().get(1).getId());
        Assert.assertEquals(1, ohrlappenpilze.getTaxHierachy().get(2).getId());
    }

    @Test
    public void test003_saveEditedTaxonomy() throws Exception {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId().toString();
        createTaxonomyTreeInDB(userGroups, owner.getId().toString());

        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);

        Taxonomy editedTaxonomy = taxonomies.get(20).copyMaterial();
        editedTaxonomy.getNames().get(0).setValue("Haarnixen_de_edited");
        editedTaxonomy.getNames().add(new MaterialName("english_name", "en", 2));

        editedTaxonomy.getTaxHierachy().clear();
        editedTaxonomy.getTaxHierachy().add(taxonomies.get(3));
        editedTaxonomy.getTaxHierachy().addAll(taxonomies.get(3).getTaxHierachy());

        editedTaxonomy.setLevel(levels.get(2));
        materialService.saveEditedMaterial(
                editedTaxonomy,
                taxonomies.get(20),
                null,
                owner.getId());

        taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        Taxonomy t = taxonomies.get(4);
        Assert.assertEquals("Haarnixen_de_edited", t.getNames().get(0).getValue());
        Assert.assertEquals("english_name", t.getNames().get(1).getValue());
        Assert.assertEquals(2, t.getTaxHierachy().size());
        Assert.assertEquals(16, t.getTaxHierachy().get(0).getId());
        Assert.assertEquals(1, t.getTaxHierachy().get(1).getId());

        Assert.assertEquals(3, t.getLevel().getId());

        Material loadedMaterial = materialService.loadMaterialById(editedTaxonomy.getId());
        Assert.assertEquals(1, loadedMaterial.getHistory().getChanges().size());
        for (Taxonomy tr : taxonomies) {
            System.out.println(tr.getFirstName());
        }
        //Scenario : change the parent of 'Champignonartige' from 'Agaricomycetes' to 'Dacrymytes'
        Taxonomy champignonartigeOrig = taxonomies.get(7).copyMaterial();
        Taxonomy champignonartigeEdit = taxonomies.get(7).copyMaterial();
        champignonartigeEdit.getTaxHierachy().clear();
        champignonartigeEdit.getTaxHierachy().add(taxonomies.get(6));
        champignonartigeEdit.getTaxHierachy().add(taxonomies.get(1));
        champignonartigeEdit.getTaxHierachy().add(taxonomies.get(0));

        champignonartigeEdit.getTaxHierachy();

        materialService.saveEditedMaterial(
                champignonartigeEdit,
                champignonartigeOrig,
                null,
                owner.getId());

        Set<Integer> parents = getParentsOfTaxo(taxonomies.get(14).getId());
        Assert.assertEquals(5, parents.size());
        Assert.assertTrue(String.format("test003: has no 'Leben(%d)' as parent", taxonomies.get(0).getId()), parents.remove(taxonomies.get(0).getId()));
        Assert.assertTrue(String.format("test003: has no 'Pilze(%d)' as parent", taxonomies.get(1).getId()), parents.remove(taxonomies.get(1).getId()));
        Assert.assertTrue(String.format("test003: has no 'Dacrymytes(%d)' as parent", taxonomies.get(6).getId()), parents.remove(taxonomies.get(6).getId()));
        Assert.assertTrue(String.format("test003: has no 'Champignonartige(%d)' as parent", taxonomies.get(7).getId()), parents.remove(taxonomies.get(7).getId()));
        Assert.assertTrue(String.format("test003: has no 'Wulstlingsverwandte(%d)' as parent", taxonomies.get(10).getId()), parents.remove(taxonomies.get(10).getId()));

    }

    private Set<Integer> getParentsOfTaxo(int id) {
        Set<Integer> parents = new HashSet<>();
        List<Object> results = entityManagerService.doSqlQuery(String.format("SELECT parentid FROM EFFECTIVE_TAXONOMY WHERE taxoid=%d ORDER BY parentid", id));
        for (Object o : results) {
            parents.add((Integer) o);
        }
        return parents;
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
                .addClass(TissueService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(TaxonomyService.class);
    }
}
