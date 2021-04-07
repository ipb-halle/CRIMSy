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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.servlet.http.Part;

import org.omnifaces.el.functions.Numbers;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Components;
import org.omnifaces.util.FacesLocal;
import org.omnifaces.util.Messages;

/**
 * This inputFile component behaves the same way like its parent component
 * {@link org.omnifaces.component.input.InputFile} except that it already
 * converts the incoming {@link Part} object(s) to String(s) in JSF's conversion
 * process. Thus, converters, validators and backing beans will receive a String
 * or a List&lt;String&gt; as value object. Additionally, converters are
 * invoked, which is not done in JSF's
 * {@link javax.faces.component.html.HtmlInputFile} component (at least in the
 * MyFaces implementation).
 * <p>
 * Most of this class' code is adoped from <a href=
 * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java">OmniFaces'
 * InputFile component</a>, which is licensed under Apache License, Version 2.0.
 * 
 * @author flange
 */
@FacesComponent(InputFile.COMPONENT_TYPE)
public class InputFile extends org.omnifaces.component.input.InputFile {
    public static final String COMPONENT_TYPE = "imageAnnotation.InputFile";

    /**
     * Converts the incoming {@link Part} object(s) to String(s) and delegates
     * it to the specified converter (if any). This code is adopted from
     * <a href=
     * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java#L313">OmniFaces'
     * implementation</a>
     * 
     * @return String or unmodifiable List&lt;String&gt;
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Object getConvertedValue(FacesContext context,
            Object submittedValue) throws ConverterException {
        try {
            if (isMultiple()) {
                List<Object> convertedParts = new ArrayList<>();

                for (Part submittedPart : (List<Part>) submittedValue) {
                    String partAsString = processFile(submittedPart);
                    if (getConverter() == null) {
                        convertedParts.add(partAsString);
                    } else {
                        convertedParts.add(getConverter().getAsObject(context,
                                this, partAsString));
                    }
                }

                return Collections.unmodifiableList(convertedParts);
            }

            String partAsString = processFile((Part) submittedValue);
            if (getConverter() == null) {
                return partAsString;
            } else {
                return getConverter().getAsObject(context, this, partAsString);
            }
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Converts the content of a {@link Part} object to a String and deletes its
     * underlying storage.
     */
    private static String processFile(Part part) throws IOException {
        String result = "";
        if (part != null) {
            long size = part.getSize();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(part.getInputStream()))) {
                /*
                 * Not sure if it is wise to do this number conversion. Anyways,
                 * we are not expecting such big file sizes.
                 */
                StringBuilder sb = new StringBuilder(
                        Long.valueOf(size).intValue());
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    sb.append(readLine);
                }

                result = sb.toString();
            } finally {
                part.delete();
            }
        }

        return result;
    }

    /**
     * Validates <code>maxsize</code>. It does not validate <code>accept</code>.
     * This code is adopted from <a href=
     * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java#L338">OmniFaces'
     * implementation</a>
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void validateValue(FacesContext context, Object convertedValue) {
        Collection<String> convertedParts = null;

        if (convertedValue instanceof String) {
            convertedParts = Collections.singleton((String) convertedValue);
        } else if (convertedValue instanceof List) {
            convertedParts = (List<String>) convertedValue;
        }

        if (convertedParts != null) {
            validateParts(context, convertedParts);
        }

        if (isValid()) {
            super.validateValue(context, convertedValue);
        } else if (FacesLocal.isAjaxRequest(context)) {
            Ajax.update(getMessageComponentClientId());
        }
    }

    /**
     * This code is adopted from <a href=
     * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java#L530">OmniFaces'
     * implementation</a>
     */
    private void validateParts(FacesContext context, Collection<String> parts) {
        Long maxsize = getMaxsize();

        if (maxsize == null) {
            return;
        }

        for (String part : parts) {
            validatePart(context, part, maxsize);
        }
    }

    /**
     * This code is adopted from <a href=
     * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java#L543">OmniFaces'
     * implementation</a>
     */
    private void validatePart(FacesContext context, String part, Long maxsize) {
        String message = null;
        String param = null;

        if (message == null && maxsize != null && part.length() > maxsize) {
            message = getMaxsizeMessage();
            param = Numbers.formatBytes(maxsize);
        }

        if (message != null) {
            Messages.addError(getClientId(context), message,
                    Components.getLabel(this), "", param);
            setValid(false);
        }
    }

    private String messageComponentClientId;

    /**
     * This code is adopted from <a href=
     * "https://github.com/omnifaces/omnifaces/blob/d7455e6a306edb36a5759bdef14678c561ecc957/src/main/java/org/omnifaces/component/input/InputFile.java#L568">OmniFaces'
     * implementation</a>
     */
    private String getMessageComponentClientId() {
        if (messageComponentClientId != null) {
            return messageComponentClientId;
        }

        UIComponent component = Components.getMessageComponent(this);

        if (component == null || component.getId() == null) {
            component = Components.getMessagesComponent();
        }

        messageComponentClientId = (component != null
                && component.getId() != null) ? component.getClientId() : null;
        return messageComponentClientId;
    }
}