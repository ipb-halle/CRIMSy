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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.i18n.UIMessage;
import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@FacesValidator("CollectionInputValidator")
public class CollectionInputValidator implements Validator,Serializable{

    @Inject
    private CollectionService collectionService;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private static final String COLLECTION_NAME_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9_]+";
    private static final String[] RESERVED_NAMES = {"configsets"};

    //***  todo: configsets

    private Pattern                     pattern;
    private Matcher                     matcher;
    private transient Logger            logger;

    /**
     * default constructor
     */
    public CollectionInputValidator() {
        this.pattern = Pattern.compile(COLLECTION_NAME_PATTERN);
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Validate a collection name. A valid collection name must only contain 
     * characters A-Za-z, digits and underscore. It must not be equal to a
     * reserved name (currently 'configsets'). It must be unique to the local 
     * node. NOTE: This method will only validate fields with name 'name'.
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (component.getId().equals("name")) {

            // check for invalid characters
            matcher = pattern.matcher(value.toString());
            if (!matcher.matches()) {
                throw new ValidatorException(
                  UIMessage.getErrorMessage(MESSAGE_BUNDLE, "collection_validation_invalid_char", null));
            }

            // check for reserved words
            for (String name: RESERVED_NAMES){
                if ( value.toString().equalsIgnoreCase(name)){
                    throw new ValidatorException(
                      UIMessage.getErrorMessage(MESSAGE_BUNDLE, "collection_validation_reserved", null));
                }
            }

            // check for uniqueness
            List<Collection> collections = null;
            try { 
                Map<String, Object> cmap = new HashMap<String, Object>();
                cmap.put("name", value.toString());
                cmap.put("local", true);
                collections = collectionService.load(cmap);
            } catch (Exception e) {
                this.logger.warn("validate() caught an exception: ", (Throwable) e);
                throw new ValidatorException(
                  UIMessage.getErrorMessage(MESSAGE_BUNDLE, "INTERNAL_ERROR", null)); 
            }
            if (! collections.isEmpty()) {
                throw new ValidatorException(
                  UIMessage.getErrorMessage(MESSAGE_BUNDLE, "collection_validation_non_unique", null));
            }

        }
    }
}
