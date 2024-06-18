/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common;

import static de.ipb_halle.lbac.material.common.Invalidity.EMPTY_MATERIAL_NAME;
import static de.ipb_halle.lbac.material.common.Invalidity.NO_MATERIAL_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;

/**
 * @author fmauz
 */
public class MaterialNameValidatorTest {

    private MaterialBeanMock materialBean;
    private MaterialNameValidator validator;

    @BeforeEach
    public void init() {
        materialBean = new MaterialBeanMock(new LoggingProfiler());
        materialBean.setMaterialNameBean(new MaterialNameBean());

        validator = new MaterialNameValidator();
    }

    @Test
    public void test001_noNames() {
        // The loops make sure that checkValidity() is idempotent.
        for (int i = 0; i < 5; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(1));
            assertThat(validator.getInvalidities(), containsInAnyOrder(NO_MATERIAL_NAME));
        }
    }

    @Test
    public void test002_nullNames() {
        materialBean.getMaterialNameBean().setNames(null);
        for (int i = 0; i < 7; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(1));
            assertThat(validator.getInvalidities(), containsInAnyOrder(NO_MATERIAL_NAME));
        }
    }

    @Test
    public void test003_nameWithEmptyString() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("", "de", 0));
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name2", "en", 0));

        for (int i = 0; i < 11; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(1));
            assertThat(validator.getInvalidities(), containsInAnyOrder(EMPTY_MATERIAL_NAME));
        }
    }

    @Test
    public void test004_nameWithNullString() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName(null, "de", 0));
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name2", "en", 0));

        for (int i = 0; i < 13; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(1));
            assertThat(validator.getInvalidities(), containsInAnyOrder(EMPTY_MATERIAL_NAME));
        }
    }

    @Test
    public void test005_nameWithWhitespacedString() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("   ", "de", 0));
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name2", "en", 0));

        for (int i = 0; i < 17; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(1));
            assertThat(validator.getInvalidities(), containsInAnyOrder(EMPTY_MATERIAL_NAME));
        }
    }

    @Test
    public void test006_validNames() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name1", "de", 0));
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name2", "en", 0));

        for (int i = 0; i < 19; i++) {
            assertTrue(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), empty());
        }
    }
}
