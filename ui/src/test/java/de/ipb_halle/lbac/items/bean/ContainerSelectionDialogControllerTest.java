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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ContainerSelectionDialogControllerTest extends TestBase {
    private static final long serialVersionUID = 1L;

    @Inject
    private UserBeanMock userBean;

    private ContainerSelectionDialogController dialogController;

    @Inject
    private ContainerService containerService;

    MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    private Container c0, c1, c2;

    @BeforeEach
    public void init() {
        createContainers();
        userBean.setCurrentAccount(publicUser);
        dialogController = new ContainerSelectionDialogController(
                containerService.loadContainersWithoutItems(adminUser), null, messagePresenter);
    }

    @Test
    public void test001_getAvailableContainers_checkLocalizedNames() {
        List<Container> containers = dialogController.getAvailableContainers();

        assertEquals(3, containers.size());
        assertTrue(containers.get(0).isEqualTo(c0));
        assertTrue(containers.get(1).isEqualTo(c1));
        assertTrue(containers.get(2).isEqualTo(c2));

        assertEquals("container_type_ROOM", containers.get(0).getType().getLocalizedName());
        assertEquals("container_type_CUPBOARD", containers.get(1).getType().getLocalizedName());
        assertEquals("container_type_CARTON", containers.get(2).getType().getLocalizedName());
    }

    @Test
    public void test002_getDimensionString() {
        c0.setItems(containerService.loadItemIdsOfContainer(c0));
        c1.setItems(containerService.loadItemIdsOfContainer(c1));
        c2.setItems(containerService.loadItemIdsOfContainer(c2));

        assertEquals("3 x 3", dialogController.getDimensionString(c0));
        assertEquals("2 x 1", dialogController.getDimensionString(c1));
        assertEquals("-", dialogController.getDimensionString(c2));
    }

    @Test
    public void test003_actionOnSelect() {
        AtomicBoolean wasCalledCorrectly = new AtomicBoolean(false);
        Consumer<Container> callback = (c) -> {
            if (c.isEqualTo(c2)) {
                wasCalledCorrectly.set(true);
            } else {
                wasCalledCorrectly.set(false);
            }
        };
        ContainerSelectionDialogController controller = new ContainerSelectionDialogController(
                containerService.loadContainersWithoutItems(adminUser), callback, messagePresenter);

        controller.actionOnSelect(c2);
        assertTrue(wasCalledCorrectly.get());

        controller.actionOnSelect(c0);
        assertFalse(wasCalledCorrectly.get());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerModalBeanTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private void createContainers() {
        c0 = new Container();
        c0.setBarCode(null);
        c0.setColumns(3);
        c0.setRows(3);
        c0.setFireArea("F1");
        c0.setGmoSafetyLevel("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90, false, true));
        containerService.saveContainer(c0);

        c1 = new Container();
        c1.setBarCode("9845893457");
        c1.setColumns(1);
        c1.setRows(2);
        c1.setFireArea(c0.getFireArea());
        c1.setGmoSafetyLevel(c0.getGmoSafetyLevel());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90, true, false));
        containerService.saveContainer(c1);

        c2 = new Container();
        c2.setBarCode("43753456");
        c2.setFireArea(c1.getFireArea());
        c2.setGmoSafetyLevel(c1.getGmoSafetyLevel());
        c2.setLabel("Karton3");
        c2.setParentContainer(c1);
        c2.setType(new ContainerType("CARTON", 90, true, false));
        containerService.saveContainer(c2);
    }
}
