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

import de.ipb_halle.lbac.i18n.UIMessage;
import java.io.Serializable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This validator validates email adresses according to a fixed pattern.
 * However, this pattern will fail for toplevel domains longer than 7 characters
 * (e.g. '.versicherung') or if the name or domain name contains non-ASCII
 * characters.
 */
@FacesValidator("EmailAddressValidator")
public class EmailAddressValidator implements Validator, Serializable {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private static final String EMAIL_PATTERN = "^([_a-zA-Z0-9-]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{1,7}))?$";

    private Pattern pattern;
    private Matcher matcher;
    private Logger logger;

    /**
     * default constructor
     */
    public EmailAddressValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * checks if a string matches a predefined email pattern
     *
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException upon pattern mismatch
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Object tmpValue = value != null ? value : "";

        matcher = pattern.matcher(tmpValue.toString());
        if (!matcher.matches()) {
            throw new ValidatorException(
                    UIMessage.getErrorMessage(MESSAGE_BUNDLE, "admission_invalid_email", null));
        }
        // this.logger.info("Finished email address validation --> " + tmpValue.toString());
    }
}
