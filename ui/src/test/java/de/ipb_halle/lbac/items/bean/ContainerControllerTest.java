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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.Container.DimensionType;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import static de.ipb_halle.lbac.container.Container.DimensionType.NONE;
import static de.ipb_halle.lbac.container.Container.DimensionType.ONE_DIMENSION;
import static de.ipb_halle.lbac.container.Container.DimensionType.TWO_DIMENSION;
import static de.ipb_halle.lbac.container.Container.DimensionType.ZERO_DIMENSION;
import static de.ipb_halle.lbac.items.bean.Container2dControllerTest.assertThatItemIsAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIOutput;
import javax.faces.component.behavior.BehaviorBase;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.SelectEvent;

/**
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerControllerTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private ContainerService containerService;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    @Inject
    private UserBeanMock userBean;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();
    private Item item;
    private Container c1;
    private Container c2;
    private Item itemForPositionTests;
    private Container containerForPositionTests;

    @BeforeEach
    public void init() {
        item = new Item();
        item.setId(1);

        userBean.setCurrentAccount(publicUser);

        c1 = new Container();
        c2 = new Container();
        c1.setLabel("Container1");
        c2.setLabel("Container2");
        c1.setType(new ContainerType("ROOM", 100, false, false));
        c2.setType(new ContainerType("FREEZER", 90, false, false));
        c2.setParentContainer(c1);
        c1 = containerService.saveContainer(c1);
        c2 = containerService.saveContainer(c2);

        // only loading sets the autocomplete string
        c1 = containerService.loadContainerById(c1.getId());
        c2 = containerService.loadContainerById(c2.getId());

        containerForPositionTests = new Container();
        containerForPositionTests.setLabel("PositionTests");
        containerForPositionTests.setRows(4);
        containerForPositionTests.setColumns(4);
        containerForPositionTests.setType(new ContainerType("FREEZER", 90, false, false));
        containerForPositionTests = containerService.saveContainer(containerForPositionTests);

        Project p = creationTools.createProject();
        Structure material = creationTools.createStructure(p);
        materialService.saveMaterialToDB(material, p.getACList().getId(), p.getDetailTemplates(), publicUser);
        itemForPositionTests = new Item();
        itemForPositionTests.setId(2);
        itemForPositionTests.setMaterial(material);
        itemForPositionTests.setACList(GlobalAdmissionContext.getPublicReadACL());
        itemForPositionTests.setOwner(publicUser);
        itemForPositionTests.setAmount(10.0);
        itemForPositionTests.setUnit(Unit.getUnit("g"));
        itemForPositionTests.setcTime(new Date());
        itemForPositionTests.setContainer(containerForPositionTests);
        itemService.saveItem(itemForPositionTests, new int[] { 2, 1 });
    }

    @Test
    public void test_actionOnItemSelect() {
        ContainerController controller = new ContainerController(item, containerService, userBean, messagePresenter);
        assertNull(controller.getContainer());

        SelectEvent event = new SelectEvent(new UIOutput(), new BehaviorBase(), c1.getAutoCompleteString());
        controller.actionOnItemSelect(event);
        assertTrue(c1.isEqualTo(controller.getContainer()));

        event = new SelectEvent(new UIOutput(), new BehaviorBase(), c2.getAutoCompleteString());
        controller.actionOnItemSelect(event);
        assertTrue(c2.isEqualTo(controller.getContainer()));

        // Unparsable string sent by the event should not change the container.
        event = new SelectEvent(new UIOutput(), new BehaviorBase(), "abc");
        controller.actionOnItemSelect(event);
        assertTrue(c2.isEqualTo(controller.getContainer()));

        // NPE in ContainerService due to a null entity
//        // Parsable string, but unknown container id
//        event = new SelectEvent(new UIOutput(), new BehaviorBase(), "123456789-");
//        controller.actionOnItemSelect(event);
//        assertTrue(c2.isEqualTo(controller.getContainer()));
    }

    @Test
    public void test_actionRemoveContainer() {
        ContainerController controller = new ContainerController(item, containerService, userBean, messagePresenter);
        controller.actionChangeContainer(c1);
        assertTrue(c1.isEqualTo(controller.getContainer()));

        controller.actionRemoveContainer();
        assertNull(controller.getContainer());
    }

    @Test
    public void test_actionChangeContainer() {
        ContainerController controller = new ContainerController(item, containerService, userBean, messagePresenter);
        assertNull(controller.getContainer());
        assertEquals("", controller.getContainerInfoPresenter().getContainerName());
        assertThat(controller.getContainer2dController().getColumns(), is(empty()));
        assertThat(controller.getContainer2dController().getRows(), is(empty()));

        Container container = new Container();
        container.setId(42);
        container.setLabel("abc");
        container.setItems(new Item[3][5]);
        container.setColumns(3);
        container.setRows(5);

        controller.actionChangeContainer(container);
        assertTrue(container.isEqualTo(controller.getContainer()));
        assertEquals("abc", controller.getContainerInfoPresenter().getContainerName());
        assertThat(controller.getContainer2dController().getColumns(), hasSize(3));
        assertThat(controller.getContainer2dController().getRows(), hasSize(5));

        controller.actionChangeContainer(null);
        assertNull(controller.getContainer());
        assertEquals("", controller.getContainerInfoPresenter().getContainerName());
        assertThat(controller.getContainer2dController().getColumns(), is(empty()));
        assertThat(controller.getContainer2dController().getRows(), is(empty()));
    }

    @Test
    public void test_nameSuggestions() {
        ContainerController controller = new ContainerController(item, containerService, userBean, messagePresenter);
        String c1AutoComplete = c1.getAutoCompleteString();
        String c2AutoComplete = c2.getAutoCompleteString();

        assertThat(controller.nameSuggestions(null), is(empty()));
        assertThat(controller.nameSuggestions(""), is(empty()));
        assertThat(controller.nameSuggestions("    "), is(empty()));
        assertThat(controller.nameSuggestions("abc"), is(empty()));

        List<String> nameSuggestions = controller.nameSuggestions("AINER");
        assertThat(nameSuggestions, hasSize(2));
        assertThat(nameSuggestions, containsInAnyOrder(c1AutoComplete, c2AutoComplete));

        nameSuggestions = controller.nameSuggestions("AINER1");
        assertThat(nameSuggestions, hasSize(1));
        assertThat(nameSuggestions, contains(c1AutoComplete));

        nameSuggestions = controller.nameSuggestions("  AINER1   ");
        assertThat(nameSuggestions, hasSize(1));
        assertThat(nameSuggestions, contains(c1AutoComplete));
    }

    @Test
    public void test_isContainerSubComponentRendered() {
        ContainerController controller = new ContainerController(item, containerService, userBean, messagePresenter);

        assertNull(controller.getContainer());
        assertTrue(controller.isContainerSubComponentRendered(null));
        assertThrows(IllegalArgumentException.class,
                () -> controller.isContainerSubComponentRendered("not a valid DimensionType"));
        for (DimensionType type : DimensionType.values()) {
            assertFalse(controller.isContainerSubComponentRendered(type.toString()));
        }

        Container container = new Container();
        controller.actionChangeContainer(container);

        assertEquals(NONE, container.getDimensionType());
        assertFalse(controller.isContainerSubComponentRendered(null));
        assertTrue(controller.isContainerSubComponentRendered(NONE.toString()));
        assertFalse(controller.isContainerSubComponentRendered(ZERO_DIMENSION.toString()));
        assertFalse(controller.isContainerSubComponentRendered(ONE_DIMENSION.toString()));
        assertFalse(controller.isContainerSubComponentRendered(TWO_DIMENSION.toString()));

        container.setRows(5);
        assertEquals(ONE_DIMENSION, container.getDimensionType());
        assertFalse(controller.isContainerSubComponentRendered(null));
        assertFalse(controller.isContainerSubComponentRendered(NONE.toString()));
        assertFalse(controller.isContainerSubComponentRendered(ZERO_DIMENSION.toString()));
        assertTrue(controller.isContainerSubComponentRendered(ONE_DIMENSION.toString()));
        assertFalse(controller.isContainerSubComponentRendered(TWO_DIMENSION.toString()));

        container.setColumns(3);
        assertEquals(TWO_DIMENSION, container.getDimensionType());
        assertFalse(controller.isContainerSubComponentRendered(null));
        assertFalse(controller.isContainerSubComponentRendered(NONE.toString()));
        assertFalse(controller.isContainerSubComponentRendered(ZERO_DIMENSION.toString()));
        assertFalse(controller.isContainerSubComponentRendered(ONE_DIMENSION.toString()));
        assertTrue(controller.isContainerSubComponentRendered(TWO_DIMENSION.toString()));
    }

    @Test
    public void test_getItemPositions() {
        ContainerController controller = new ContainerController(itemForPositionTests, containerService, userBean,
                messagePresenter);
        assertThatItemIsAt(2, 1, controller.getItemPositions());
    }

    @Test
    public void test_resolveItemPositions() {
        ContainerController controller = new ContainerController(itemForPositionTests, containerService, userBean,
                messagePresenter);
        Set<int[]> positions = controller.resolveItemPositions();
        assertThat(positions, hasSize(1));
        assertArrayEquals(new int[] { 2, 1 }, positions.iterator().next());
    }

    @Test
    public void test_setItemAtPosition() {
        ContainerController controller = new ContainerController(itemForPositionTests, containerService, userBean,
                messagePresenter);
        assertThatItemIsAt(2, 1, controller.getItemPositions());

        controller.removeItemFromPosition();
        controller.setItemAtPosition(1, 0);
        assertThatItemIsAt(0, 1, controller.getItemPositions());
    }

    @Test
    public void test_removeItemFromPosition() {
        ContainerController controller = new ContainerController(itemForPositionTests, containerService, userBean,
                messagePresenter);
        assertThatItemIsAt(2, 1, controller.getItemPositions());

        controller.removeItemFromPosition();
        assertThatItemIsAt(-1, -1, controller.getItemPositions()); // matrix is false everywhere
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerControllerTest.war").addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
