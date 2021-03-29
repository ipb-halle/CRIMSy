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
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    Integer userGroups;
    Integer ownerid;

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
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        ownerid = owner.getId();
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
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());
        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals("test001: 21 taxonomies must be found", 21, taxonomies.size());

        Taxonomy life = taxonomies.get(0);
        Assert.assertTrue(life.getTaxHierachy().isEmpty());
        Assert.assertEquals(1, life.getLevel().getId());
        Assert.assertEquals("Leben_de", life.getFirstName());

        Taxonomy wulstlinge = taxonomies.get(12);
        Assert.assertEquals(7, wulstlinge.getLevel().getId());
        Assert.assertEquals("Wulstlinge_de", wulstlinge.getFirstName());
        Assert.assertEquals(6, (int) wulstlinge.getId());
        Assert.assertEquals(5, (int) wulstlinge.getTaxHierachy().size());
        Assert.assertEquals(5, (int) wulstlinge.getTaxHierachy().get(0).getId());
        Assert.assertEquals(4, (int) wulstlinge.getTaxHierachy().get(1).getId());
        Assert.assertEquals(3, (int) wulstlinge.getTaxHierachy().get(2).getId());
        Assert.assertEquals(2, (int) wulstlinge.getTaxHierachy().get(3).getId());
        Assert.assertEquals(1, (int) wulstlinge.getTaxHierachy().get(4).getId());

        Taxonomy ohrlappenpilze = taxonomies.get(10);
        Assert.assertEquals(6, ohrlappenpilze.getLevel().getId());
        Assert.assertEquals("Gallerttränenverwandte_de", ohrlappenpilze.getFirstName());
        Assert.assertEquals(11, (int) ohrlappenpilze.getId());
        Assert.assertEquals(3, (int) ohrlappenpilze.getTaxHierachy().size());
        Assert.assertEquals(8, (int) ohrlappenpilze.getTaxHierachy().get(0).getId());
        Assert.assertEquals(2, (int) ohrlappenpilze.getTaxHierachy().get(1).getId());
        Assert.assertEquals(1, (int) ohrlappenpilze.getTaxHierachy().get(2).getId());
    }

    @Test
    public void test003_saveEditedTaxonomy() throws Exception {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId();
        createTaxonomyTreeInDB(userGroups, owner.getId());

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
        Assert.assertEquals(16, (int) t.getTaxHierachy().get(0).getId());
        Assert.assertEquals(1, (int) t.getTaxHierachy().get(1).getId());

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

    // Move a taxonomy with a subtree to another node
    //        T0          T0
    //        |           |
    //      |   |   ->  |   |
    //      T1  T2      T1  T2
    //      |               |
    //      T3              T3
    //      |               |
    //   T4 T5 T6        T4 T5 T6
    @Test
    public void test004_moveTaxonomy() throws Exception {
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId();
        createTaxanomy(0, "T0", 1, userGroups, owner.getId());
        createTaxanomy(1, "T1", 2, userGroups, owner.getId(), 0);
        createTaxanomy(2, "T2", 2, userGroups, owner.getId(), 0);
        createTaxanomy(3, "T3", 14, userGroups, owner.getId(), 0, 1);
        createTaxanomy(4, "T4", 18, userGroups, owner.getId(), 0, 1, 3);
        createTaxanomy(5, "T5", 18, userGroups, owner.getId(), 0, 1, 3);
        createTaxanomy(6, "T6", 18, userGroups, owner.getId(), 0, 1, 3);

        Taxonomy t3 = service.loadTaxonomyById(3);
        Taxonomy t2 = service.loadTaxonomyById(2);
        Taxonomy t3_copy = t3.copyMaterial();
        List<Taxonomy> hierarchy = t2.getTaxHierachy();
        hierarchy.add(0, t2);
        t3_copy.setTaxHierachy(hierarchy);

        materialService.saveEditedMaterial(t3_copy, t3, project.getACList().getId(), owner.getId());

        t3 = service.loadTaxonomyById(3);
        Assert.assertEquals(2, (int) t3.getTaxHierachy().size());
        Assert.assertEquals(2, (int) t3.getTaxHierachy().get(0).getId());
        Assert.assertEquals(0, (int) t3.getTaxHierachy().get(1).getId());
        Assert.assertEquals(14, (int) t3.getLevel().getId());

        Taxonomy t4 = service.loadTaxonomyById(4);
        Assert.assertEquals(3, (int) t4.getTaxHierachy().size());
        Assert.assertEquals(3, (int) t4.getTaxHierachy().get(0).getId());
        Assert.assertEquals(2, (int) t4.getTaxHierachy().get(1).getId());
        Assert.assertEquals(0, (int) t4.getTaxHierachy().get(2).getId());
        Assert.assertEquals(18, (int) t4.getLevel().getId());

        Taxonomy t5 = service.loadTaxonomyById(5);
        Assert.assertEquals(3, (int) t5.getTaxHierachy().size());
        Assert.assertEquals(3, (int) t5.getTaxHierachy().get(0).getId());
        Assert.assertEquals(2, (int) t5.getTaxHierachy().get(1).getId());
        Assert.assertEquals(0, (int) t5.getTaxHierachy().get(2).getId());
        Assert.assertEquals(18, (int) t5.getLevel().getId());

        Taxonomy t6 = service.loadTaxonomyById(6);
        Assert.assertEquals(3, (int) t6.getTaxHierachy().size());
        Assert.assertEquals(3, (int) t6.getTaxHierachy().get(0).getId());
        Assert.assertEquals(2, (int) t6.getTaxHierachy().get(1).getId());
        Assert.assertEquals(0, (int) t6.getTaxHierachy().get(2).getId());
        Assert.assertEquals(18, t6.getLevel().getId());
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
        WebArchive deployment = prepareDeployment("TaxonomyServiceTest.war")
                .addClass(ProjectService.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(MaterialService.class)
                .addClass(TissueService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(TaxonomyService.class);
        return UserBeanDeployment.add(deployment);
    }
}
