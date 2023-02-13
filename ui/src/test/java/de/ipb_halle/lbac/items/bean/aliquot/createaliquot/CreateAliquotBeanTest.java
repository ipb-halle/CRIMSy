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
package de.ipb_halle.lbac.items.bean.aliquot.createaliquot;

import static de.ipb_halle.lbac.items.bean.aliquot.createaliquot.CreateAliquotBean.STEP1;
import static de.ipb_halle.lbac.items.bean.aliquot.createaliquot.CreateAliquotBean.STEP2;
import static de.ipb_halle.lbac.items.bean.aliquot.createaliquot.CreateAliquotBean.STEP3;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import jakarta.enterprise.event.Event;
import jakarta.faces.component.UIInput;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.bean.aliquot.common.ProjectSelectionController;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class CreateAliquotBeanTest extends TestBase {
    private static final long serialVersionUID = 1L;
    private static final double DELTA = 1e-6;

    @Inject
    private CreateAliquotBean bean;

    @Inject
    private Navigator navigator;

    @Inject
    private UserBeanMock userBeanMock;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    @Inject
    private Event<LoginEvent> loginEvent;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    private Structure material;
    private Item parentItem;

    @BeforeEach
    public void before() {
        userBeanMock.setCurrentAccount(publicUser);

        // initializes the user in ItemOverviewBean
        loginEvent.fire(new LoginEvent(publicUser));

        parentItem = createParentItem();
        bean.actionStartCreateAliquot(parentItem);
    }

    private Item createParentItem() {
        Project p = creationTools.createProject();
        material = creationTools.createStructure(p);
        materialService.saveMaterialToDB(material, p.getACList().getId(), p.getDetailTemplates(), publicUser);

        Item item = new Item();
        item.setAmount(100.0);
        item.setUnit(Unit.getUnit("g"));
        item.setPurity("super pure");
        item.setACList(GlobalAdmissionContext.getPublicReadACL());
        item.setMaterial(material);
        item.setOwner(publicUser);
        item.setProject(p);
        item.setcTime(new Date());

        item = itemService.saveItem(item);
        return item;
    }

    /*
     * Tests for onFlowProcess()
     */
    @Test
    public void test_onFlowProcess_STEP1_to_STEP2() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP1, STEP2);
        CreateAliquotStep1Controller step1Controller = bean.getStep1Controller();

        // amount too high, stay in STEP1
        step1Controller.setAmount(0.2);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));
        assertEquals(STEP1, bean.onFlowProcess(event));
        assertEquals("itemCreateAliquot_error_amountTooHigh", messagePresenter.getLastErrorMessage());

        messagePresenter.resetMessages();

        // amount is ok, proceed to STEP2
        step1Controller.setAmount(0.05);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));
        assertEquals(STEP2, bean.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_onFlowProcess_STEP2_to_STEP3() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP2, STEP3);
        CreateAliquotStep1Controller step1Controller = bean.getStep1Controller();
        CreateAliquotStep2Controller step2Controller = bean.getStep2Controller();
        step1Controller.setAmount(1.0);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));

        // proceed to STEP3 if no direct container is selected
        step2Controller.setDirectContainer(false);
        assertEquals(STEP3, bean.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());

        // container size is too small, stay in STEP2
        step2Controller.setDirectContainer(true);
        step2Controller.setDirectContainerSize(0.5);
        assertEquals(STEP2, bean.onFlowProcess(event));
        assertEquals("itemCreateAliquot_error_containerTooSmall", messagePresenter.getLastErrorMessage());

        messagePresenter.resetMessages();

        // container size is ok, proceed to STEP3
        step2Controller.setDirectContainerSize(2.0);
        assertEquals(STEP3, bean.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for actionStartCreateAliquot()
     */
    @Test
    public void test_actionStartCreateSolution() {
        bean.actionStartCreateAliquot(parentItem);

        assertTrue(parentItem.isEqualTo(bean.getParentItem()));
        assertThat(navigator.getNextPage(), containsString("/item/aliquot/createAliquot/createAliquot"));
    }

    /*
     * Tests for actionSave()
     */
    @Test
    public void test_actionSave_amountTooHigh() {
        CreateAliquotStep1Controller step1Controller = bean.getStep1Controller();

        /*
         * Preparation
         */
        // amount
        step1Controller.setAmount(1.0);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));

        /*
         * Assumptions
         */
        // only parentItem in database
        List<Item> items = findAllItems();
        assertThat(items, hasSize(1));

        /*
         * Execution
         */
        bean.actionSave();

        /*
         * Assertions
         */
        items = findAllItems();
        assertThat(items, hasSize(1));

        // parent item as same amount
        assertEquals(parentItem.getId(), items.get(0).getId());
        assertEquals(100.0, items.get(0).getAmount(), DELTA);
        assertEquals("g", items.get(0).getUnit().toString());

        // message and navigation outcome
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("itemCreateAliquot_error_amountTooHigh", messagePresenter.getLastErrorMessage());
        assertThat(navigator.getNextPage(), containsString("/item/aliquot/createAliquot/createAliquot"));
    }

    @Test
    public void test_actionSave_directContainerTooSmall() {
        CreateAliquotStep1Controller step1Controller = bean.getStep1Controller();
        CreateAliquotStep2Controller step2Controller = bean.getStep2Controller();

        /*
         * Preparation
         */
        // amount
        step1Controller.setAmount(0.04);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));

        // direct container
        step2Controller.setDirectContainer(true);
        step2Controller.setDirectContainerSize(0.01);

        /*
         * Assumptions
         */
        // only parentItem in database
        List<Item> items = findAllItems();
        assertThat(items, hasSize(1));

        /*
         * Execution
         */
        bean.actionSave();

        /*
         * Assertions
         */
        items = findAllItems();
        assertThat(items, hasSize(1));

        // parent item as same amount
        assertEquals(parentItem.getId(), items.get(0).getId());
        assertEquals(100.0, items.get(0).getAmount(), DELTA);
        assertEquals("g", items.get(0).getUnit().toString());

        // message and navigation outcome
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("itemCreateAliquot_error_containerTooSmall", messagePresenter.getLastErrorMessage());
        assertThat(navigator.getNextPage(), containsString("/item/aliquot/createAliquot/createAliquot"));
    }

    @Test
    public void test_actionSave_successful() {
        CreateAliquotStep1Controller step1Controller = bean.getStep1Controller();
        CreateAliquotStep2Controller step2Controller = bean.getStep2Controller();
        ProjectSelectionController step3Controller = bean.getStep3Controller();

        /*
         * Preparation
         */
        // amount
        step1Controller.setAmount(0.04);
        step1Controller.setAmountUnit(Unit.getUnit("kg"));

        // direct container
        ContainerType selectedContainerType = bean.getAvailableContainerTypes().get(0);
        step2Controller.setDirectContainer(true);
        step2Controller.setDirectContainerType(selectedContainerType);
        step2Controller.setDirectContainerSize(10.0);

        // select project (different from parentItem)
        Project selectedProject = creationTools.createProject("another project");
        step3Controller.setSelectedProject(selectedProject);

        // label
        step2Controller.setCustomLabel(true);
        step2Controller.setCustomLabelValue("123");

        /*
         * Assumptions
         */
        // only parentItem in database
        List<Item> items = findAllItems();
        assertThat(items, hasSize(1));

        /*
         * Execution
         */
        bean.actionSave();

        /*
         * Assertions
         */
        items = findAllItems();
        assertThat(items, hasSize(2));
        items.sort((i1, i2) -> Integer.compare(i1.getId(), i2.getId()));

        // parent item as reduced amount (= 100g - 40g)
        assertEquals(parentItem.getId(), items.get(0).getId());
        assertEquals(60.0, items.get(0).getAmount(), DELTA);
        assertEquals("g", items.get(0).getUnit().toString());

        // asserts for newly created item
        Item newItem = items.get(1);

        assertEquals(0.04, newItem.getAmount(), DELTA);
        assertEquals("kg", newItem.getUnit().toString());
        assertNull(newItem.getConcentration());
        assertNull(newItem.getConcentrationUnit());
        assertEquals(10.0, newItem.getContainerSize(), DELTA);
        assertEquals(selectedContainerType, newItem.getContainerType());
        assertTrue(material.isEqualTo(newItem.getMaterial()));
        assertTrue(selectedProject.isEqualTo(newItem.getProject()));
        assertEquals("super pure", newItem.getPurity());
        assertNull(newItem.getSolvent());
        assertEquals("123", newItem.getLabel());
        assertEquals(parentItem.getId(), newItem.getParentId());

        // message and navigation outcome
        assertEquals("itemEdit_save_new_success", messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());
        assertThat(navigator.getNextPage(), containsString("/item/items"));
    }

    private List<Item> findAllItems() {
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        SearchResult result = itemService.loadItems(builder.build());
        return result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
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
     * Tests for getAvailableContainerTypes()
     */
    @Test
    public void test_getAvailableContainerTypes() {
        assertThat(bean.getAvailableContainerTypes(), is(not(empty())));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment.add(ItemDeployment.add(prepareDeployment("CreateAliquotBeanTest.war")))
                .addClass(CreateAliquotBean.class);
    }
}
