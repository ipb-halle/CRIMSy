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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ContainerPositionServiceTest extends TestBase {

    Container c0;
    Container c1;

    @Inject
    private ItemService itemService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ContainerPositionService positionService;

    @Inject
    private ProjectService projectService;

    @Inject
    private GlobalAdmissionContext globalContext;
    private User owner;
    private CreationTools creationTools;
    private Project project;

    String INSERT_MATERIAL_SQL = "INSERT INTO MATERIALS VALUES("
            + "1,"
            + "1,"
            + "now(),"
            + "%d,"
            + "%d,"
            + "false,%d)";

    @Test
    public void test001() {
        containerService.saveContainer(c0);
        containerService.saveContainer(c1);
        createAndSaveMaterial();
        Item item=createItem();
        itemService.saveItem(item);
    }
    
  

    @Before
    @Override
    public void setUp() {
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = creationTools.createProject();
        c0 = new Container();
        c0.setBarCode(null);
        c0.setDimension("3;3;1");
        c0.setFireSection("F1");
        c0.setGmosavety("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90, false, true));

        c1 = new Container();
        c1.setBarCode("9845893457");
        c1.setDimension("2;2;1");
        c1.setFireSection(c0.getFireSection());
        c1.setGmosavety(c0.getGmosavety());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90, true, false));
    }

    @After
    public void finish() {
        super.cleanItemsFromDb();
        super.cleanMaterialsFromDB();

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerPositionService.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private Item createItem() {
        Structure s = new Structure("", 0d, 0d, 1, new ArrayList<>(), project.getId(), new HazardInformation(), new StorageClassInformation(), null);
        Item item = new Item();
        item.setAmount(23d);
        item.setACList(globalContext.getAdminOnlyACL());
        item.setUnit("kg");
        item.setArticle(null);
        item.setConcentration(32d);
        item.setContainerSize(100d);
        item.setDescription("description");
        item.setMaterial(s);
        item.setOwner(owner);
        item.setProject(project);
        item.setPurity("rein");
        item.setcTime(new Date());
        item.setSolvent(null);
        return item;
    }

    private void createAndSaveMaterial() {

        entityManagerService.doSqlUpdate(String.format(
                INSERT_MATERIAL_SQL,
                project.getUserGroups().getId(),
                owner.getId(),
                project.getId()));
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(1,'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(1,1,'')");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(1,1,1,'TESTMATERIAL','de',0)");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(2,1,1,'TESTMATERIA2','en',0)");
    }

}
