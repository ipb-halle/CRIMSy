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

import org.junit.Test;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class will provide some test cases for the HexUtil class 
 */
public class HexUtilTest {


    /**
     * basic tests
     */
    @Test
    public void testHexUtil() {

        assertEquals("toHex(byte): conversion error ",
                "5a", HexUtil.toHex("Z".getBytes()[0]).toLowerCase()); 

        assertEquals("toHex(array of bytes): conversion error ",
            "4352494d53792032303230", HexUtil.toHex("CRIMSy 2020".getBytes()).toLowerCase());

        assertEquals("fromHex(): conversion error ",
            "/CRIMSy/", new String(HexUtil.fromHex("2f4352494d53792F")));

        assertEquals("fromHex(): zero length argument ", 0, HexUtil.fromHex("").length);

        assertNull("fromHex(): null argument ", HexUtil.fromHex(null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testHexUtilOdd() {
        HexUtil.fromHex("5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexUtilInvalidChar() {
        HexUtil.fromHex("2g");
    }

}
