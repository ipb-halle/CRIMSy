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
package de.ipb_halle.lbac.util;

import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * This class will provide some test cases for the HexUtil class 
 */
public class UnitTest {


    /**
     * basic tests
     */
    @Test
    public void test001_testUnit() {

        Unit cm = Unit.getUnit("cm");
        Unit mm = Unit.getUnit("mm");

        assertEquals("mm.transform(cm): conversion error ",
                0.1, mm.transform(cm), 0.0001);

        assertEquals("cm.transform(mm): conversion error ",
                10.0, cm.transform(mm), 0.0001);
    }
    
    @Test
    public void test002_getUnitsByQuality(){
        List<Unit> units=Unit.getUnitsOfQuality(Quality.MASS);
        Assert.assertEquals(4,units.size());
        
        units=Unit.getUnitsOfQuality(Quality.MASS,Quality.VOLUME,Quality.PIECES);
        Assert.assertEquals(9,units.size());
    }

}
