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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the {@link DataURIImage} annotation.
 * 
 * @author flange
 */
public class DataURIImageValidator
        implements ConstraintValidator<DataURIImage, String> {
    @Override
    public void initialize(DataURIImage constraintAnnotation) {
    }

    /**
     * Validates that {@code value} is a valid DataURI-encoded image. This is
     * achieved (a) by checking if the string starts with "data:" and (b) by
     * reading the Base64-encoded image via the {@link ImageIO} class.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        if (!value.startsWith("data:")) {
            return false;
        }

        int dataStartIndex = value.indexOf(",") + 1;
        if (dataStartIndex <= 0) {
            return false;
        }

        String data;
        try {
            data = value.substring(dataStartIndex);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException e) {
            // no valid Base64
            return false;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(decoded));
        } catch (IOException e) {
            // no valid image
            return false;
        }

        if (image == null) {
            return false;
        }

        if ((image.getHeight() <= 0) || (image.getWidth() <= 0)) {
            return false;
        }

        return true;
    }
}