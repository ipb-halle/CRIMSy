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
package de.ipb_halle.lbac.items.bean.aliquot.createsolution.consumepartofitem;

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
public class ConsumePartOfItemStep4ControllerTest {
    private static final double DELTA = 1e-6;

    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();

    @BeforeEach
    public void before() {
        messagePresenter.resetMessages();
    }

    @Test
    public void test_defaultsGettersAndSetters() {
        ConsumePartOfItemStep4Controller controller = new ConsumePartOfItemStep4Controller(null, null, null);

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
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep3Controller step3Controller = new ConsumePartOfItemStep3Controller(step1Controller, null,
                null);
        ConsumePartOfItemStep4Controller controller = new ConsumePartOfItemStep4Controller(step1Controller,
                step3Controller, messagePresenter);

        // container size larger than dispensed volume
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));
        step3Controller.setDispensedVolume(10.0);
        controller.setDirectContainerSize(15.0);
        controller.actionOnChangeDirectContainerSize();
        assertNull(messagePresenter.getLastErrorMessage());

        // container too small
        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));
        step3Controller.setDispensedVolume(10.0);
        controller.setDirectContainerSize(5.0);
        controller.actionOnChangeDirectContainerSize();
        assertEquals("itemCreateSolution_error_containerTooSmall", messagePresenter.getLastErrorMessage());
    }

    /*
     * Tests for presentContainerTooSmallError()
     */
    @Test
    public void test_presentContainerTooSmallError() {
        Item item = new Item();
        ConsumePartOfItemStep1Controller step1Controller = new ConsumePartOfItemStep1Controller(item, null);
        ConsumePartOfItemStep3Controller step3Controller = new ConsumePartOfItemStep3Controller(step1Controller, null,
                null);
        ConsumePartOfItemStep4Controller controller = new ConsumePartOfItemStep4Controller(step1Controller,
                step3Controller, messagePresenter);
        assertNull(messagePresenter.getLastErrorMessage());

        step1Controller.setTargetVolumeUnit(Unit.getUnit("ml"));
        step3Controller.setDispensedVolume(10.0);
        controller.presentContainerTooSmallError();
        assertEquals("itemCreateSolution_error_containerTooSmall", messagePresenter.getLastErrorMessage());
    }
}