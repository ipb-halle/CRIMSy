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
package de.ipb_halle.lbac.plugin.imageAnnotation;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the {@link DataURIImage} annotation.
 * 
 * @author flange
 */
public class DataURIImageTest {
    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private class TestBean {
        @DataURIImage
        private final String image;

        public TestBean(String image) {
            this.image = image;
        }
    }

    @Test
    public void testValidation() {
        Set<ConstraintViolation<TestBean>> constraintViolations;

        List<String> validImages = new ArrayList<>();
        List<String> invalidImages = new ArrayList<>();

        validImages.add("");
        invalidImages.add("data:null");
        invalidImages.add("data:image/png;base64,");
        invalidImages.add("data:image/png;base64,A");
        invalidImages.add("data:image/png;base64,AB");
        invalidImages.add("data:image/png;base64,AB=");
        invalidImages.add("data:image/png;base64,ABC=");

        // minimal png from https://stackoverflow.com/a/6018673
        validImages.add(
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        validImages.add(
                "data:,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        validImages.add(
                "data:,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII");
        validImages.add(
                "data:,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYI");
        invalidImages.add(
                "data:iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");
        invalidImages.add(
                "ata:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=");

        // minimal gif from https://stackoverflow.com/a/13139830
        validImages.add(
                "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

        // minimal gif from https://stackoverflow.com/a/12483396
        validImages.add(
                "data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==");

        // let's try to do some XSS stuff ...
        invalidImages.add("http://evil.com");

        // from https://security.stackexchange.com/a/135518
        invalidImages.add("a onerror=alert('XSS')");
        invalidImages.add("data:, onerror=alert('XSS')");
        invalidImages.add("data:\", onerror=alert('XSS');");

        for (String image : validImages) {
            constraintViolations = validator.validate(new TestBean(image));
            assertEquals(
                    "Expected no validation errors for image \"" + image + "\"",
                    0, constraintViolations.size());
        }

        for (String image : invalidImages) {
            constraintViolations = validator.validate(new TestBean(image));
            assertEquals(
                    "Expected a validation error for image \"" + image + "\"",
                    1, constraintViolations.size());
        }
    }

    private class TestBeanWithMessage {
        @DataURIImage(message = "this is a message")
        private final String image;

        public TestBeanWithMessage(String image) {
            this.image = image;
        }
    }

    @Test
    public void testMessage() {
        String invalidImage = "data:image/png;base64,";
        Set<ConstraintViolation<TestBean>> constraintViolations = validator
                .validate(new TestBean(invalidImage));
        assertEquals(1, constraintViolations.size());
        assertEquals("invalid DataURI-encoded image",
                constraintViolations.iterator().next().getMessage());

        Set<ConstraintViolation<TestBeanWithMessage>> constraintViolationsWithMessage = validator
                .validate(new TestBeanWithMessage(invalidImage));
        assertEquals(1, constraintViolations.size());
        assertEquals("this is a message",
                constraintViolationsWithMessage.iterator().next().getMessage());
    }
}