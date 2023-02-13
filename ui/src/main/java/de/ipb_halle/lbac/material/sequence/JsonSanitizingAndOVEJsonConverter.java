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

import java.io.IOException;
import java.io.Serializable;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import de.ipb_halle.lbac.util.JsonSanitizingConverter;

/**
 * FacesConverter that converts JSON strings to {@link SequenceData} objects and
 * vice-versa. Sanitation of the JSON happens in-between.
 * 
 * @author flange
 */
@FacesConverter("jsonSanitizingAndOVEJsonConverter")
public class JsonSanitizingAndOVEJsonConverter implements Converter, Serializable {
    private static final long serialVersionUID = 1L;

    private final transient JsonSanitizingConverter jsonSanitizer = new JsonSanitizingConverter();
    private final transient OpenVectorEditorJsonConverter oveConverter = new OpenVectorEditorJsonConverter();

    private SequenceType sequenceType;

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }

    /**
     * Converts the incoming JSON string to a {@link SequenceData} object with a
     * JSON sanitation in-between.
     * 
     * @return {@link SequenceData} object or {@code null} if the value to convert
     *         is {@code null}
     * @throws ConverterException if {@code sequenceType} is {@code null} or
     *                            {@link OpenVectorEditorJsonConverter#jsonToSequenceData(String, SequenceType)}
     *                            fails
     */
    @Override
    public SequenceData getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        if (value == null) {
            return null;
        }
        if (sequenceType == null) {
            throw new ConverterException("This converter requires a sequenceType.");
        }

        String sanitizedJson = (String) jsonSanitizer.getAsObject(context, component, value);
        try {
            return oveConverter.jsonToSequenceData(sanitizedJson, sequenceType);
        } catch (OpenVectorEditorJsonConverterException | IOException e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Converts the incoming {@link SequenceData} object to a sanitized JSON string.
     * 
     * @return sanitized JSON or empty String if {@code value} is {@code null} or
     *         not a {@link SequenceData} instance
     * @throws ConverterException if
     *                            {@link OpenVectorEditorJsonConverter#sequenceDataToJson(SequenceData)}
     *                            fails
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
        if ((value == null) || !(value instanceof SequenceData)) {
            return "";
        }
        SequenceData data = (SequenceData) value;

        try {
            String converted = oveConverter.sequenceDataToJson(data);
            return jsonSanitizer.getAsString(context, component, converted);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }
}
