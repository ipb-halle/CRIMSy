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
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class TaxonomyServiceTest extends TestBase {

    Project project;
    User owner;
    Integer userGroups;
    Integer ownerid;

    @Inject
    private TaxonomyService service;

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

        ownerid = owner.getId();
    }

    @Test
    public void test001_loadTaxonomyLevels() {
        List<TaxonomyLevel> levels = service.loadTaxonomyLevel();
        Assert.assertEquals("test001: 21 levels must be found", 21, levels.size());
    }

    @Test
    public void test0015_loadSelectedTaxonomyByIDandDepth() {
        project = creationTools.createProject();
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());

        List<Taxonomy> loaded_Taxonomies = service.loadSelectedTaxonomyByIDandDepth(1, 1);

        List<Integer> resultList = loaded_Taxonomies.stream().map(x -> x.getId()).toList();
        List<Integer> ids = Arrays.asList(1, 2, 3, 8, 14, 15, 16, 17);
        Assert.assertTrue(resultList.containsAll(ids));

    }

    @Test
    public void test0016_loadSelectedTaxonomyByIDandDepth() {
        project = creationTools.createProject();
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());

        List<Taxonomy> loaded_Taxonomies = service.loadSelectedTaxonomyByIDandDepth(8, 0);

        List<Integer> resultList = loaded_Taxonomies.stream().map(x -> x.getId()).toList();
        List<Integer> ids = Arrays.asList(8, 11);
        Assert.assertTrue(resultList.containsAll(ids));

    }

    @Test
    public void test002_loadTaxonomies() {
        project = creationTools.createProject();
        createTaxonomyTreeInDB(project.getUserGroups().getId(), owner.getId());
        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        Assert.assertEquals("test001: 21 taxonomies must be found", 21, taxonomies.size());

        Taxonomy life = taxonomies.get(0);
        Assert.assertTrue(life.getTaxHierarchy().isEmpty());
        Assert.assertEquals(1, life.getLevel().getId());
        Assert.assertEquals("Leben_de", life.getFirstName());

        Taxonomy wulstlinge = taxonomies.get(16);
        Assert.assertEquals(7, wulstlinge.getLevel().getId());
        Assert.assertEquals("Wulstlinge_de", wulstlinge.getFirstName());
        Assert.assertEquals(6, (int) wulstlinge.getId());
        Assert.assertEquals(5, (int) wulstlinge.getTaxHierarchy().size());
        Assert.assertEquals(5, (int) wulstlinge.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(4, (int) wulstlinge.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(3, (int) wulstlinge.getTaxHierarchy().get(2).getId());
        Assert.assertEquals(2, (int) wulstlinge.getTaxHierarchy().get(3).getId());
        Assert.assertEquals(1, (int) wulstlinge.getTaxHierarchy().get(4).getId());

        Taxonomy ohrlappenpilze = taxonomies.get(9);
        Assert.assertEquals(6, ohrlappenpilze.getLevel().getId());
        Assert.assertEquals("Gallerttränenverwandte_de", ohrlappenpilze.getFirstName());
        Assert.assertEquals(11, (int) ohrlappenpilze.getId());
        Assert.assertEquals(3, (int) ohrlappenpilze.getTaxHierarchy().size());
        Assert.assertEquals(8, (int) ohrlappenpilze.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(2, (int) ohrlappenpilze.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(1, (int) ohrlappenpilze.getTaxHierarchy().get(2).getId());
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

        editedTaxonomy.getTaxHierarchy().clear();
        editedTaxonomy.getTaxHierarchy().add(taxonomies.get(3));
        editedTaxonomy.getTaxHierarchy().addAll(taxonomies.get(3).getTaxHierarchy());

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
        Assert.assertEquals(2, t.getTaxHierarchy().size());
        Assert.assertEquals(16, (int) t.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(1, (int) t.getTaxHierarchy().get(1).getId());

        Assert.assertEquals(3, t.getLevel().getId());

        Material loadedMaterial = materialService.loadMaterialById(editedTaxonomy.getId());
        Assert.assertEquals(1, loadedMaterial.getHistory().getChanges().size());
        for (Taxonomy tr : taxonomies) {
            System.out.println(tr.getFirstName());
        }
        //Scenario : change the parent of 'Champignonartige' from 'Agaricomycetes' to 'Dacrymytes'
        Taxonomy champignonartigeOrig = taxonomies.get(7).copyMaterial();
        Taxonomy champignonartigeEdit = taxonomies.get(7).copyMaterial();
        champignonartigeEdit.getTaxHierarchy().clear();
        champignonartigeEdit.getTaxHierarchy().add(taxonomies.get(6));
        champignonartigeEdit.getTaxHierarchy().add(taxonomies.get(1));
        champignonartigeEdit.getTaxHierarchy().add(taxonomies.get(0));

        champignonartigeEdit.getTaxHierarchy();

        materialService.saveEditedMaterial(
                champignonartigeEdit,
                champignonartigeOrig,
                null,
                owner.getId());

        Set<Integer> parents = getParentsOfTaxo(taxonomies.get(14).getId());
        Assert.assertEquals(5, parents.size());
        Assert.assertTrue(String.format("test003: has no 'Leben(%d)' as parent", taxonomies.get(0).getId()), parents.remove(taxonomies.get(0).getId()));
        Assert.assertTrue(String.format("test003: has no 'Pilze(%d)' as parent", taxonomies.get(2).getId()), parents.remove(taxonomies.get(2).getId()));
        Assert.assertTrue(String.format("test003: has no 'Dacrymytes(%d)' as parent", taxonomies.get(6).getId()), parents.remove(taxonomies.get(6).getId()));
        Assert.assertTrue(String.format("test003: has no 'Champignonartige(%d)' as parent", taxonomies.get(9).getId()), parents.remove(taxonomies.get(9).getId()));
        Assert.assertTrue(String.format("test003: has no 'Wulstlingsverwandte(%d)' as parent", taxonomies.get(12).getId()), parents.remove(taxonomies.get(12).getId()));
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
        List<Taxonomy> hierarchy = t2.getTaxHierarchy();
        hierarchy.add(0, t2);
        t3_copy.setTaxHierachy(hierarchy);

        materialService.saveEditedMaterial(t3_copy, t3, project.getACList().getId(), owner.getId());

        t3 = service.loadTaxonomyById(3);
        Assert.assertEquals(2, (int) t3.getTaxHierarchy().size());
        Assert.assertEquals(2, (int) t3.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(0, (int) t3.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(14, (int) t3.getLevel().getId());

        Taxonomy t4 = service.loadTaxonomyById(4);
        Assert.assertEquals(3, (int) t4.getTaxHierarchy().size());
        Assert.assertEquals(3, (int) t4.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(2, (int) t4.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(0, (int) t4.getTaxHierarchy().get(2).getId());
        Assert.assertEquals(18, (int) t4.getLevel().getId());

        Taxonomy t5 = service.loadTaxonomyById(5);
        Assert.assertEquals(3, (int) t5.getTaxHierarchy().size());
        Assert.assertEquals(3, (int) t5.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(2, (int) t5.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(0, (int) t5.getTaxHierarchy().get(2).getId());
        Assert.assertEquals(18, (int) t5.getLevel().getId());

        Taxonomy t6 = service.loadTaxonomyById(6);
        Assert.assertEquals(3, (int) t6.getTaxHierarchy().size());
        Assert.assertEquals(3, (int) t6.getTaxHierarchy().get(0).getId());
        Assert.assertEquals(2, (int) t6.getTaxHierarchy().get(1).getId());
        Assert.assertEquals(0, (int) t6.getTaxHierarchy().get(2).getId());
        Assert.assertEquals(18, t6.getLevel().getId());
    }

    @Test
    public void test005_getDirectChildrenOfTaxo() {
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId();
        createTaxonomyTreeInDB(userGroups, owner.getId());
        List<Taxonomy> taxonomies = service.loadTaxonomy(new HashMap<>(), true);
        List<Taxonomy> directChildren = service.loadDirectChildrenOf(taxonomies.get(0).getId());
        Assert.assertEquals(3, directChildren.size());
    }

    @Test
    public void test006_loadRootTaxonomy() {
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId();
        createTaxonomyTreeInDB(userGroups, owner.getId());
        Assert.assertNotNull(service.loadRootTaxonomy());
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
        WebArchive deployment = prepareDeployment("TaxonomyServiceTest.war");
        return MaterialDeployment.add(UserBeanDeployment.add(deployment));
    }
}
