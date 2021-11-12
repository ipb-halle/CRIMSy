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

import de.ipb_halle.lbac.material.common.Invalidity;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SequenceValidatorTest {

    MateriaBeanMock beanMock;
    SequenceValidator validator = new SequenceValidator();

    @Before
    public void init() {
        beanMock = new MateriaBeanMock();
        beanMock.setMaterialNameBean(new MaterialNameBean());
        beanMock.setSequenceInfos(new SequenceInformation());

    }

    @Test
    public void test001_checkValidity_invalide() {
        Assert.assertFalse(validator.checkValidity(beanMock));
        Assert.assertEquals(2, validator.getInvalidities().size());
        Assert.assertTrue(validator.getInvalidities().contains(Invalidity.NO_MATERIAL_NAME));
        Assert.assertTrue(validator.getInvalidities().contains(Invalidity.NO_SEQUENCETYPE_CHOOSEN));

    }

    @Test
    public void test001_checkValidity_invalid() {

        beanMock.getMaterialNameBean().getNames().add(new MaterialName("name1", "de", 0));
        beanMock.getSequenceInfos().setSequenceType(SequenceType.DNA);

        Assert.assertTrue(validator.checkValidity(beanMock));
        Assert.assertEquals(0, validator.getInvalidities().size());

    }
}
