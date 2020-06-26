/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.ACListService;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ContainerNestingServiceTest extends TestBase {
    
    @Inject
    private ContainerNestingService containerNestingService;
    
    @Inject
    private ContainerService containerService;
    
    @After
    public void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM nested_containers");
        entityManagerService.doSqlUpdate("DELETE FROM containers");
    }
    
    @Test
    public void test001_loadNestedInContainers() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.loadNestedInObjects(ids[0]);
        Assert.assertEquals(4, nestedIds.size());
        nestedIds.contains(ids[1]);
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
        nestedIds.contains(ids[5]);
    }
    
    @Test
    public void test002_loadAllSubContainer() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.getNestingService().loadAllSubObjects(ids[5]);
        Assert.assertEquals(4, nestedIds.size());
        nestedIds.contains(ids[1]);
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
        nestedIds.contains(ids[0]);
    }
    
    @Test
    public void test003_loadSubpath() {
        int[] ids = initializeContainer();
        Set<Integer> nestedIds = containerNestingService.getNestingService().loadSubpath(ids[1], ids[4]);
        Assert.assertEquals(2, nestedIds.size());
        nestedIds.contains(ids[2]);
        nestedIds.contains(ids[4]);
    }
    
    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ContainerNestingServiceTest.war")
                .addClass(ContainerService.class)
                .addClass(ACListService.class)
                .addClass(SystemSettings.class)
                .addClass(ItemService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(ArticleService.class)
                .addClass(KeyManager.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(LdapProperties.class)
                .addClass(ContainerNestingService.class)
                .addClass(ProjectService.class);
    }
    
    private int[] initializeContainer() {
        Container c0 = new Container();
        c0.setType(new ContainerType("ROOM", 100));
        c0.setLabel("C0");
        containerService.saveContainer(c0);
        Container c1 = new Container();
        c1.setType(new ContainerType("ROOM", 99));
        c1.setLabel("C1");
        c1.setParentContainer(c0);
        containerService.saveContainer(c1);
        Container c2 = new Container();
        c2.setType(new ContainerType("ROOM", 99));
        c2.setLabel("C2");
        containerService.saveContainer(c2);
        Container c3 = new Container();
        c3.setType(new ContainerType("ROOM", 98));
        c3.setLabel("C3");
        c3.setParentContainer(c1);
        containerService.saveContainer(c3);
        Container c4 = new Container();
        c4.setType(new ContainerType("ROOM", 97));
        c4.setLabel("C4");
        c4.setParentContainer(c3);
        containerService.saveContainer(c4);
        Container c5 = new Container();
        c5.setType(new ContainerType("ROOM", 96));
        c5.setLabel("C5");
        c5.setParentContainer(c4);
        containerService.saveContainer(c5);
        
        return new int[]{c5.getId(), c4.getId(), c3.getId(), c2.getId(), c1.getId(), c0.getId()};
    }
}
