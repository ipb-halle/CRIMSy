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

import static de.ipb_halle.lbac.material.common.Invalidity.NO_MATERIAL_NAME;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class MaterialNameValidatorTest {

    MaterialNameValidator validator;

    @Before
    public void init() {
        validator = new MaterialNameValidator();
    }

    @Test
    public void test001_checkNoExistingName() {
        Set<Invalidity> errors = new HashSet<>();
        boolean isValid = validator.areMaterialNamesValid(new ArrayList<>(), errors);
        Assert.assertFalse(isValid);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.contains(NO_MATERIAL_NAME));
    }

    @Test
    public void test002_checkNameWithEmptyString() {
        Set<Invalidity> errors = new HashSet<>();
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("", "en", 0));
        boolean isValid = validator.areMaterialNamesValid(names, errors);
        Assert.assertFalse(isValid);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.contains(Invalidity.EMPTY_MATERIAL_NAME));
    }

    @Test
    public void test003_allNamesValide() {
        Set<Invalidity> errors = new HashSet<>();
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("test", "en", 0));
        boolean isValid = validator.areMaterialNamesValid(names, errors);
        Assert.assertTrue(isValid);
        Assert.assertEquals(0, errors.size());

    }

}
