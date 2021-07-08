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
package de.ipb_halle.lbac.admission;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the ACListService class.
 */
public class CredentialHandlerTest {

    /**
     * Tests the CredentialHandler. Test strings can be produced in a Tomcat
     * environment by calling the digest.sh script:
     *
     * digest.sh -a ALGORITHM -i ITERATIONS CLEARTEXT_PASSWORD
     *
     */
    private final static String algo = "SHA-256";
    private Map<String, String> credentials;
    private CredentialHandler handler;

    @Before
    public void setUp() {
        this.handler = new CredentialHandler().setDigestAlgorithm("SHA-256");
        this.credentials = new HashMap<>();
        this.credentials.put("Leibniz",
                "0c60bfb78a89ad8ec03375701fb04f159f460181540e98a5f973d00f462296ca$5$55e45095b0a4f1ffd87edacbebd9f8818ec1941cf26147a060c5ff5e020e9815");
        this.credentials.put("Halle",
                "606d024da097179638b67917e3c2f278c43b6ae9f5e8e92e7c7104ce053bf0c5$1$1fd344d12cbfcaf9fd62fe72bacd0353f3c6e31fb4dbb0ebec99afbe2ee9cc25");
    }

    /**
     * Test algorithm on some known sample cases
     */
    @Test
    public void testMatch() {
        Iterator<String> iter = this.credentials.keySet().iterator();
        while (iter.hasNext()) {
            String cred = iter.next();
            assertTrue("Password matches", this.handler.match(cred, this.credentials.get(cred)));
        }
    }
}
