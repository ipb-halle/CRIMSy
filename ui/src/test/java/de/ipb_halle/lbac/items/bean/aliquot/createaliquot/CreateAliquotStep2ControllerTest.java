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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
public class CreateAliquotStep2ControllerTest {
    private static final double DELTA = 1e-6;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @BeforeEach
    public void before() {
        messagePresenter.resetMessages();
    }

    @Test
    public void test_defaultsGettersAndSetters() {
        CreateAliquotStep2Controller controller = new CreateAliquotStep2Controller(null, messagePresenter);

        assertFalse(controller.isDirectContainer());
        controller.setDirectContainer(true);
        assertTrue(controller.isDirectContainer());

        assertNull(controller.getDirectContainerSize());
        controller.setDirectContainerSize(42.0);
        assertEquals(42.0, controller.getDirectContainerSize(), DELTA);

        assertNull(controller.getDirectContainerType());
        ContainerType type = new ContainerType("abc", 0, false, false);
        controller.setDirectContainerType(type);
        assertSame(type, controller.getDirectContainerType());

        assertFalse(controller.isCustomLabel());
        controller.setCustomLabel(true);
        assertTrue(controller.isCustomLabel());

        assertNull(controller.getCustomLabelValue());
        controller.setCustomLabelValue("abc");
        assertEquals("abc", controller.getCustomLabelValue());
    }

    /*
     * Tests for actionOnChangeDirectContainerSize()
     */
    @Test
    public void test_actionOnChangeDirectContainerSize() {
        Item parentItem = new Item();
        parentItem.setAmount(1.0);
        parentItem.setUnit(Unit.getUnit("kg"));
        CreateAliquotStep1Controller step1Controller = new CreateAliquotStep1Controller(parentItem);
        CreateAliquotStep2Controller controller = new CreateAliquotStep2Controller(step1Controller, messagePresenter);

        // container size larger than amount
        step1Controller.setAmount(500.0);
        step1Controller.setAmountUnit(Unit.getUnit("g"));
        controller.setDirectContainerSize(700.0);
        controller.actionOnChangeDirectContainerSize();
        assertNull(messagePresenter.getLastErrorMessage());

        // container too small
        controller.setDirectContainerSize(300.0);
        controller.actionOnChangeDirectContainerSize();
        assertEquals("itemCreateAliquot_error_containerTooSmall", messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for presentContainerTooSmallError()
     */
    @Test
    public void test_presentContainerTooSmallError() {
        Item parentItem = new Item();
        parentItem.setAmount(1.0);
        parentItem.setUnit(Unit.getUnit("kg"));
        CreateAliquotStep1Controller step1Controller = new CreateAliquotStep1Controller(parentItem);
        step1Controller.setAmount(500.0);
        step1Controller.setAmountUnit(Unit.getUnit("g"));
        CreateAliquotStep2Controller controller = new CreateAliquotStep2Controller(step1Controller, messagePresenter);

        controller.presentContainerTooSmallError();
        assertEquals("itemCreateAliquot_error_containerTooSmall", messagePresenter.getLastErrorMessage());
    }
}