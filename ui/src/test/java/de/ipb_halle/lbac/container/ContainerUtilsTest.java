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
package de.ipb_halle.lbac.container;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;

/**
 * @author flange
 */
public class ContainerUtilsTest {
    private MessagePresenter messagePresenter = MessagePresenterMock.getInstance();

    @Test
    public void test_filterLocalizeAndSortContainerTypes() {
        ContainerType type1 = new ContainerType("type1", 1, false, false);
        ContainerType type2 = new ContainerType("type2", 0, true, false);
        ContainerType type3 = new ContainerType("type3", 0, false, true);

        List<ContainerType> types = new ArrayList<>();
        types.add(type3);
        types.add(type1);
        types.add(type2);

        ContainerUtils.filterLocalizeAndSortContainerTypes(types, messagePresenter);

        assertThat(types, contains(type2, type3));
        assertEquals("container_type_type2", types.get(0).getLocalizedName());
        assertEquals("container_type_type3", types.get(1).getLocalizedName());
    }
}