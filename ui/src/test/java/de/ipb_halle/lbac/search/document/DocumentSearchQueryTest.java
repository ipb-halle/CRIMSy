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
package de.ipb_halle.lbac.search.document;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * This is a conventional JUnit Test for the SearchQuery class. It performs only
 * a simple test for the clone() method of SearchQuery.
 */
public class DocumentSearchQueryTest {

    @Test
    public void testSearchQuery() {
        DocumentSearchQuery q = new DocumentSearchQuery();
        q.setQuery("Test");

        try {
            DocumentSearchQuery p = (DocumentSearchQuery) q.clone();

            // p and q must be different objects
            assertNotSame(p, q);

            // but the query string must be the same
            assertEquals(p.getQuery(), q.getQuery());

        } catch (CloneNotSupportedException e) {
            fail(e.getMessage());
        }
    }

}
