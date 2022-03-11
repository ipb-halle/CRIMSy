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
package de.ipb_halle.lbac.exp;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class ExperimentCodeTest {
    
    @Test
    public void test001_createNewExperimentCode(){
        ExperimentCode code=ExperimentCode.createNewInstance("XPB");
        code.setSuffix("ExperimentCodeTest:test001_createNewExperimentCode");
        String expCode=code.generateNewExperimentCode(1);
        Assert.assertEquals("XPB0001-ExperimentCodeTest:test001_createNewExperimentCode",expCode);
    }
    @Test
    public void test002_createExperimentCodeFromExisting(){
        ExperimentCode code=ExperimentCode.createInstanceOfExistingExp("XPB9876-test002_createExperimentCodeFromExsting");
        code.setSuffix("test002_createExperimentCodeFromExsting-edited");
        String expCode=code.generateExistingExperimentCode();
        Assert.assertEquals("XPB9876-test002_createExperimentCodeFromExsting-edited",expCode);
    }
    @Test
    public void test003_createExperimentCodeFromExistingWithMinus(){
        ExperimentCode code=ExperimentCode.createInstanceOfExistingExp("XPB9876--test003_createExperimentCodeFromExistingWithMinus");
        Assert.assertEquals("-test003_createExperimentCodeFromExistingWithMinus",  code.getSuffix());
      
        code.setSuffix("-test003_createExperimentCodeFromExistingWithMinus-edited");
        
        String expCode=code.generateExistingExperimentCode();
        Assert.assertEquals("XPB9876--test003_createExperimentCodeFromExistingWithMinus-edited",expCode);
    }
}
