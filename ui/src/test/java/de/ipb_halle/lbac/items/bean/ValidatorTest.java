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

import de.ipb_halle.lbac.container.mock.ErrorMessagePresenterMock;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.mocks.ContainerPositionServiceMock;
import de.ipb_halle.lbac.items.mocks.LabelServiceMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ValidatorTest {

    private final ContainerPositionServiceMock containerServiceMock = new ContainerPositionServiceMock();
    private final LabelServiceMock labelServiceMock = new LabelServiceMock();
    private final Item item = new Item();
    private final ContainerController containerController = new ContainerController(null, null);
    private final String customLabel = "customLabel";
    private boolean isCustomLabel;
    private Validator validator;

    @Before
    public void setUp() {
        validator = new Validator(containerServiceMock, labelServiceMock);
        validator.setMessagePresenter(new ErrorMessagePresenterMock());
        containerServiceMock.arePositionsFree = true;
        labelServiceMock.isLabelAvailable = true;
        isCustomLabel = false;

    }

    @Test
    public void test001_itemValideToSave_noCustomLabel() {
        Assert.assertTrue(validator.itemValideToSave(item, containerController, isCustomLabel, customLabel));

        labelServiceMock.isLabelAvailable = false;
        Assert.assertTrue(validator.itemValideToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test002_itemValideToSave_allowedCustomLabel() {
        isCustomLabel = true;

        Assert.assertTrue(validator.itemValideToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test003_itemValideToSave_forbiddenCustomLabel() {
        isCustomLabel = true;
        labelServiceMock.isLabelAvailable = false;

        Assert.assertFalse(validator.itemValideToSave(item, containerController, isCustomLabel, customLabel));
    }

    @Test
    public void test004_itemValideToSave_positionForbidden() {
        containerServiceMock.arePositionsFree = false;

        Assert.assertFalse(validator.itemValideToSave(item, containerController, isCustomLabel, customLabel));
    }

}
