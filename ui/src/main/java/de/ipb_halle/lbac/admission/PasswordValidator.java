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

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@FacesValidator("PasswordValidator")
public class PasswordValidator implements Validator, Serializable {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    /*
 * infoService could be used to allow configuration of the password validation 
 * algorithm, i.e. enabling Cracklib
 *
    @Inject
    private InfoEntityService   infoService;
     */
    private Logger logger;

    /**
     * default constructor
     */
    public PasswordValidator() {
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /*
     * checks the password complexity of the new password using 
     * cracklib.
     * @throws ValidatorException if password is to easy to guess
     *
     * This code patch can use cracklib to enforce password strength
     *
    public void validateFacist(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String result = null;
        try {
            String cracklib_dict = Config.getConfigStr("CRACKLIB_DICT");
            Packer p = new Packer(cracklib_dict, "r");
            result = CrackLib.fascistLook(p, value.toString());
        } catch(Exception e) {
            throw new ValidatorException(
              UIMessage.getErrorMessage(MESSAGE_BUNDLE, "admission_error", new Object[] {"CrackLib"}));
        }
        if(result != null) {
            // 123456789012345678901234
            // PASSWORD_ERROR_DICTIONRY
            String msg_key = result.substring(0,24);
            String msg_arg = result.substring(24);
            msg_arg = (msg_arg.matches("")) ? null : msg_arg;
            throw new ValidatorException(
              UIMessage.getErrorMessage(MESSAGE_BUNDLE, msg_key, new Object[] { msg_arg }));
        }
    }
     */
    /**
     * Perform password validation according to the password rules
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        logger.info("start::validation::" + component.getId());

        String pw = value.toString().trim();
        if (pw.length() < 8) {
            throw new ValidatorException(
                    UIMessage.getErrorMessage(MESSAGE_BUNDLE, "PASSWORD_ERROR_TOO_SHORT", null));
        }

        String repeat = "invalid";
        try {
            /*
             * UIInput ui = (UIInput) component.getAttributes().get("passwordRepeat");
             * repeat = (String) ui.getSubmittedValue();
             * 
             * The code given below is is a workaround against crashes 
             * upon the second call to the userManagement.xhtml, when validation 
             * uses the binding attribute for the password repeat input element:
             */
            Map<String, String> map = context.getExternalContext().getRequestParameterMap();
            if (map.get("frmModalUserDialog") != null) {
                repeat = map.get("input_frmModalUserDialog:tempPasswordRepeat");
            } else {
                repeat = map.get("input_frmModalPassword:tempPasswordRepeat");
            }
        } catch (Exception e) {
            this.logger.warn("validate caught an exception", (Throwable) e);
        }

        if (!pw.equals(repeat)) {
            throw new ValidatorException(
                    UIMessage.getErrorMessage(MESSAGE_BUNDLE, "PASSWORD_ERROR__MISMATCH", null));
        }
    }
}
