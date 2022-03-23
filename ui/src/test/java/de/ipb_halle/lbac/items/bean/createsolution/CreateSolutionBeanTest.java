/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.createsolution;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Date;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class CreateSolutionBeanTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private CreateSolutionBean bean;

    @Inject
    private Navigator navigator;

    @Inject
    private UserBeanMock userBeanMock;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    private Item parentItem;

    @BeforeEach
    public void before() {
        userBeanMock.setCurrentAccount(publicUser);
        parentItem = createParentItem();
    }

    private Item createParentItem() {
        Project p = creationTools.createProject();
        Structure material = creationTools.createStructure(p);
        materialService.saveMaterialToDB(material, p.getACList().getId(), p.getDetailTemplates(), publicUser);

        Item item = new Item();
        item.setAmount(10.0);
        item.setUnit(Unit.getUnit("kg"));
        item.setACList(GlobalAdmissionContext.getPublicReadACL());
        item.setMaterial(material);
        item.setOwner(publicUser);
        item.setProject(p);
        item.setcTime(new Date());

        item = itemService.saveItem(item);
        return item;
    }

    /*
     * Tests for actionStartCreateSolution()
     */
    @Test
    public void test_actionStartCreateSolution() {
        bean.actionStartCreateSolution(parentItem);

        assertTrue(parentItem.isEqualTo(bean.getParentItem()));
        assertTrue(bean.isParentItemHasMolarMass());
        assertThat(navigator.getNextPage(), containsString("/item/createSolution/createSolution"));
    }

    /*
     * Test for actionCancel()
     */
    @Test
    public void test_actionCancel() {
        bean.actionCancel();

        assertThat(navigator.getNextPage(), containsString("item/items"));
    }

    /*
     * Tests for isItemNotSoluble()
     */
    @Test
    public void test006_isItemNotSoluble() {
        CreateSolutionBean bean = new CreateSolutionBean();
        Item item = new Item();

        // unit is null
        assertTrue(bean.isItemNotSoluble(item));

        // wrong unit quality
        item.setUnit(Unit.getUnit("ml"));
        assertTrue(bean.isItemNotSoluble(item));

        // correct unit quality
        item.setUnit(Unit.getUnit("g"));
        assertFalse(bean.isItemNotSoluble(item));

        // item is already a solution
        item.setConcentration(42d);
        assertTrue(bean.isItemNotSoluble(item));

        item.setConcentration(null);
        item.setConcentrationUnit(Unit.getUnit("mM"));
        assertTrue(bean.isItemNotSoluble(item));

        item.setConcentrationUnit(null);
        item.setSolvent(new Solvent());
        assertTrue(bean.isItemNotSoluble(item));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment.add(ItemDeployment.add(prepareDeployment("CreateSolutionBeanTest.war")
                .addClass(CreateSolutionBean.class)
                .addClass(ConsumePartOfItemStrategyController.class)));
    }
}
