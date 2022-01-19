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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;

import static de.ipb_halle.lbac.material.common.Invalidity.NO_MATERIAL_NAME;
import static de.ipb_halle.lbac.material.common.Invalidity.NO_SEQUENCETYPE_CHOSEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author fmauz
 */
public class SequenceValidatorTest {
    private MateriaBeanMock materialBean;
    private SequenceValidator validator;

    @Before
    public void init() {
        materialBean = new MateriaBeanMock();
        materialBean.setMaterialNameBean(new MaterialNameBean());
        materialBean.setSequenceInfos(new SequenceInformation());

        validator = new SequenceValidator();
    }

    @Test
    public void test001_noNames_noSequenceType() {
        assertFalse(validator.checkValidity(materialBean));
        assertThat(validator.getInvalidities(), hasSize(2));
        assertThat(validator.getInvalidities(), containsInAnyOrder(NO_MATERIAL_NAME, NO_SEQUENCETYPE_CHOSEN));
    }

    @Test
    public void test002_validNames_noSequenceType() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name1", "de", 0));

        assertFalse(validator.checkValidity(materialBean));
        assertThat(validator.getInvalidities(), hasSize(1));
        assertThat(validator.getInvalidities(), containsInAnyOrder(NO_SEQUENCETYPE_CHOSEN));
    }

    @Test
    public void test003_noNames_withSequenceType() {
        materialBean.getSequenceInfos().setSequenceType(SequenceType.DNA);

        assertFalse(validator.checkValidity(materialBean));
        assertThat(validator.getInvalidities(), hasSize(1));
        assertThat(validator.getInvalidities(), containsInAnyOrder(NO_MATERIAL_NAME));
    }

    @Test
    public void test004_validNames_withSequenceType() {
        materialBean.getMaterialNameBean().getNames().add(new MaterialName("name1", "de", 0));
        materialBean.getSequenceInfos().setSequenceType(SequenceType.DNA);

        assertTrue(validator.checkValidity(materialBean));
        assertThat(validator.getInvalidities(), empty());
    }

    @Test
    public void test005_checkValidity_resetsErrors() {
        for (int i = 0; i < 5; i++) {
            assertFalse(validator.checkValidity(materialBean));
            assertThat(validator.getInvalidities(), hasSize(2));
            assertThat(validator.getInvalidities(), containsInAnyOrder(NO_MATERIAL_NAME, NO_SEQUENCETYPE_CHOSEN));
        }
    }
}
