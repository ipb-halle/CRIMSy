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
package de.ipb_halle.lbac.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assertion methods useful for writing tests with JSON data.
 * 
 * @author flange
 */
public class JsonAssert {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonAssert() {
    }

    /**
     * Asserts that two JSON strings are equal by comparing their complete JSON
     * trees. If they are not, an {@link AssertionError} is thrown with the
     * given message.
     *
     * @param message  the identifying message for the {@link AssertionError}
     *                 (<code>null</code> okay)
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     */
    public static void assertJsonEquals(String message, String expected,
            String actual) {
        try {
            JsonNode expectedJsonNode = mapToJsonNode(expected);
            JsonNode actualJsonNode = mapToJsonNode(actual);

            // The JsonNode class properly implements equals(Object).
            assertEquals(message, expectedJsonNode, actualJsonNode);
        } catch (IOException e) {
            throw new AssertionError("Cannot deserialize JSON", e);
        }
    }

    /**
     * Asserts that two JSON strings are equal by comparing their complete JSON
     * trees. If they are not, an {@link AssertionError} without a message is
     * thrown.
     *
     * @param expected expected value
     * @param actual   the value to check against <code>expected</code>
     */
    public static void assertJsonEquals(String expected, String actual) {
        assertJsonEquals(null, expected, actual);
    }

    /**
     * Asserts that two JSON strings are <b>not</b> equal by comparing their
     * complete JSON trees. If they are, an {@link AssertionError} is thrown
     * with the given message.
     *
     * @param message    the identifying message for the {@link AssertionError}
     *                   (<code>null</code> okay)
     * @param unexpected unexpected value to check
     * @param actual     the value to check against <code>unexpected</code>
     */
    public static void assertJsonNotEquals(String message, String unexpected,
            String actual) {
        try {
            JsonNode unexpectedJsonNode = mapToJsonNode(unexpected);
            JsonNode actualJsonNode = mapToJsonNode(actual);

            // The JsonNode class properly implements equals(Object).
            assertNotEquals(message, unexpectedJsonNode, actualJsonNode);
        } catch (IOException e) {
            throw new AssertionError("Cannot deserialize JSON", e);
        }
    }

    /**
     * Asserts that two JSON strings are <b>not</b> equals. If they are, an
     * {@link AssertionError} without a message is thrown.
     *
     * @param unexpected unexpected value to check
     * @param actual     the value to check against <code>unexpected</code>
     */
    public static void assertJsonNotEquals(String unexpected, String actual) {
        assertJsonNotEquals(null, unexpected, actual);
    }

    private static JsonNode mapToJsonNode(String json) throws IOException {
        return MAPPER.readTree(json);
    }
}
