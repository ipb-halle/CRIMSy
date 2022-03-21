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
package de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem;

import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP1;
import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP2;
import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP3;
import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP4;
import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP5;
import static de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem.ConsumePartOfItemStrategyController.STEP6;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.component.UIInput;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
class ConsumePartOfItemStrategyControllerTest extends TestBase {
    private static final long serialVersionUID = 1L;
    private static final double DELTA = 1e-6;

    @Inject
    private ConsumePartOfItemStrategyController controller;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    @Inject
    private UserBeanMock userBeanMock;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    private Structure material;
    private Item parentItem;

    @BeforeEach
    public void before() {
        userBeanMock.setCurrentAccount(publicUser);
        parentItem = createParentItem();
        controller.init(parentItem);
    }

    private Item createParentItem() {
        Project p = creationTools.createProject();
        material = creationTools.createStructure(p);
        materialService.saveMaterialToDB(material, p.getACList().getId(), p.getDetailTemplates(), publicUser);

        Item item = new Item();
        item.setAmount(10.0);
        item.setUnit(Unit.getUnit("kg"));
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
        parentItem.setAmount(100.0);
        parentItem.setUnit(Unit.getUnit("g"));
        ConsumePartOfItemStep1Controller step1Controller = controller.getStep1Controller();

        // set target mass to 0.15kg
        step1Controller.setTargetConcentration(150.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step1Controller.setTargetVolume(1.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("l"));
        step1Controller.setTargetMassUnit(Unit.getUnit("kg"));
        step1Controller.actionUpdateTargetMass();

        messagePresenter.resetMessages();

        // target mass is too high, stay in STEP1
        assertEquals(STEP1, controller.onFlowProcess(event));
        assertEquals("itemCreateSolution_error_targetMassTooHigh", messagePresenter.getLastErrorMessage());

        // set target mass to 0.05kg
        step1Controller.setTargetConcentration(50.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step1Controller.setTargetVolume(1.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("l"));
        step1Controller.setTargetMassUnit(Unit.getUnit("kg"));
        step1Controller.actionUpdateTargetMass();

        messagePresenter.resetMessages();

        // before calling onFlowProcess(): step2Controller is not initialized
        assertNull(controller.getStep2Controller().getWeighUnit());

        // target mass is ok, proceed to STEP2
        assertEquals(STEP2, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());

        // step2Controller is now initialized
        assertEquals("kg", controller.getStep2Controller().getWeighUnit().toString());
    }

    @Test
    public void test_onFlowProcess_STEP2_to_STEP3() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP2, STEP3);
        parentItem.setAmount(100.0);
        parentItem.setUnit(Unit.getUnit("g"));
        ConsumePartOfItemStep2Controller step2Controller = controller.getStep2Controller();

        // set weigh to 0.15kg
        step2Controller.setWeigh(0.15);
        step2Controller.setWeighUnit(Unit.getUnit("kg"));

        messagePresenter.resetMessages();

        // weigh is too high, stay in STEP2
        assertEquals(STEP2, controller.onFlowProcess(event));
        assertEquals("itemCreateSolution_error_weighTooHigh", messagePresenter.getLastErrorMessage());

        // set weigh to 0.05kg
        step2Controller.setWeigh(0.05);
        step2Controller.setWeighUnit(Unit.getUnit("kg"));

        messagePresenter.resetMessages();

        // needed during initialization of step3Controller
        ConsumePartOfItemStep1Controller step1Controller = controller.getStep1Controller();
        step1Controller.setTargetConcentration(1.0);
        step1Controller.setTargetConcentrationUnit(Unit.getUnit("g/l"));
        step1Controller.setTargetVolume(1.0);
        step1Controller.setTargetVolumeUnit(Unit.getUnit("l"));

        // before calling onFlowProcess(): step3Controller is not initialized
        assertNull(controller.getStep3Controller().getDispensedVolume());

        // weigh is ok, proceed to STEP3
        assertEquals(STEP3, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());

        // step3Controller is now initialized
        assertEquals(50.0, controller.getStep3Controller().getDispensedVolume(), DELTA);
    }

    @Test
    public void test_onFlowProcess_STEP3_to_STEP4() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP3, STEP4);
        assertEquals(STEP4, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_onFlowProcess_STEP4_to_STEP5() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP4, STEP5);
        ConsumePartOfItemStep4Controller step4Controller = controller.getStep4Controller();

        messagePresenter.resetMessages();

        // proceed to STEP5 if no direct container is selected
        step4Controller.setDirectContainer(false);
        assertEquals(STEP5, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());

        // set dispensed volume to 1.5l
        controller.getStep3Controller().setDispensedVolume(1.5);
        controller.getStep1Controller().setTargetVolumeUnit(Unit.getUnit("l"));

        step4Controller.setDirectContainer(true);
        // set volume of direct container to 1.0l
        step4Controller.setDirectContainerSize(1.0);

        // container size is too small, stay in STEP4
        assertEquals(STEP4, controller.onFlowProcess(event));
        assertEquals("itemCreateSolution_error_containerTooSmall", messagePresenter.getLastErrorMessage());

        // set volume of direct container to 1.7l
        step4Controller.setDirectContainerSize(1.7);

        messagePresenter.resetMessages();

        // container size is ok, proceed to STEP5
        assertEquals(STEP5, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_onFlowProcess_STEP5_to_STEP6() {
        FlowEvent event = new FlowEvent(new UIInput(), STEP5, STEP6);
        assertEquals(STEP6, controller.onFlowProcess(event));
        assertNull(messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for actionSave()
     */
    @Test
    public void test_actionSave() {
        /*
         * Preparation
         */
        createSolvents("Water");
        controller.init(parentItem);

        // set volume to 50ml
        controller.getStep3Controller().setDispensedVolume(50.0);
        controller.getStep1Controller().setTargetVolumeUnit(Unit.getUnit("ml"));

        // set weigh to 20g
        controller.getStep2Controller().setWeigh(20.0);
        controller.getStep2Controller().setWeighUnit(Unit.getUnit("g"));

        // final concentration will be 400g/l
        controller.getStep1Controller().setTargetConcentrationUnit(Unit.getUnit("g/l"));
        assertEquals(400.0, controller.getStep3Controller().getFinalConcentration(), DELTA);

        // define direct container with size of 100ml
        ContainerType selectedContainerType = controller.getAvailableContainerTypes().get(0);
        controller.getStep4Controller().setDirectContainer(true);
        controller.getStep4Controller().setDirectContainerType(selectedContainerType);
        controller.getStep4Controller().setDirectContainerSize(100.0);

        // select project (different from parentItem)
        Project selectedProject = creationTools.createProject("another project");
        controller.getStep5Controller().setSelectedProject(selectedProject);

        // select solvent
        Solvent selectedSolvent = controller.getSolvents().get(0);
        controller.getStep3Controller().setSolvent(selectedSolvent);

        // select label
        controller.getStep4Controller().setCustomLabel(true);
        controller.getStep4Controller().setCustomLabelValue("007");

        /*
         * Assumptions
         */
        // only parentItem in database
        List<Item> items = findAllItems();
        assertThat(items, hasSize(1));

        /*
         * Execution
         */
        controller.actionSave();

        /*
         * Assertions
         */
        items = findAllItems();
        assertThat(items, hasSize(2));
        items.sort((i1, i2) -> Integer.compare(i1.getId(), i2.getId()));

        // parent item as reduced amount (= 10kg - 20g)
        assertEquals(parentItem.getId(), items.get(0).getId());
        assertEquals(9.98, items.get(0).getAmount(), DELTA);
        assertEquals("kg", items.get(0).getUnit().toString());

        // asserts for newly created item
        Item newItem = items.get(1);

        assertEquals(50.0, newItem.getAmount(), DELTA);
        assertEquals("ml", newItem.getUnit().toString());
        assertEquals(400.0, newItem.getConcentration(), DELTA);
        assertEquals("g/l", newItem.getConcentrationUnit().toString());
        assertEquals(100.0, newItem.getContainerSize(), DELTA);
        assertEquals(selectedContainerType, newItem.getContainerType());
        assertTrue(material.isEqualTo(newItem.getMaterial()));
        assertTrue(selectedProject.isEqualTo(newItem.getProject()));
        assertEquals("super pure", newItem.getPurity());
        assertEquals(selectedSolvent, newItem.getSolvent());
        assertEquals("007", newItem.getLabel());
        assertEquals(parentItem.getId(), newItem.getParentId());
    }

    private List<Item> findAllItems() {
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        SearchResult result = itemService.loadItems(builder.build());
        return result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
    }

    /*
     * Tests for getSolvents()
     */
    @Test
    public void test_getSolvents_withoutSolventsInDB() {
        controller.init(parentItem);

        assertThat(controller.getSolvents(), is(empty()));
    }

    @Test
    public void test_getSolvents_withSolventsInDB() {
        createSolvents("Water", "Ethanol", "Methanol");

        controller.init(parentItem);

        List<Solvent> solvents = controller.getSolvents();
        assertThat(solvents, hasSize(3));
        List<String> solventNames = solvents.stream().map(solvent -> solvent.getName()).collect(Collectors.toList());
        assertThat(solventNames, containsInAnyOrder("Water", "Ethanol", "Methanol"));
    }

    /*
     * Tests for getAvailableContainerTypes()
     */
    @Test
    public void test_getAvailableContainerTypes() {
        assertThat(controller.getAvailableContainerTypes(), is(not(empty())));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(ItemDeployment.add(prepareDeployment("ConsumePartOfItemStrategyControllerTest.war")))
                .addClass(ConsumePartOfItemStrategyController.class);
    }
}