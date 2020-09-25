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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@FacesValidator("AccountValidator")
public class AccountValidator implements Validator,Serializable {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    @Inject
    private MemberService memberService;

    @Inject
    private UserMgrBean userMgrBean;

    private Logger logger;

    /**
     * default constructor
     */
    public AccountValidator() {
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * check for duplicate accounts. Only local (i.e. LOCAL and LDAP) subsystems
     * will be checked. Duplicate accounts for different institutions must be
     * allowed (e.g. jdoe@example.com and jdoe@somewhere.com).
     *
     * @param login the user login
     * @param id the user id of the currently processed user
     * @throws ValidatorException upon duplicate accounts or on internal failure
     */
    private void checkDuplicateAccount(String login) throws ValidatorException {
        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(MemberService.PARAM_LOGIN, login);
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, 
                new AdmissionSubSystemType[]{AdmissionSubSystemType.LOCAL, AdmissionSubSystemType.LDAP});

        List<User> list = this.memberService.loadUsers(cmap);
        if (list != null) {
            if ((list.size() == 1) && list.get(0).equals(this.userMgrBean.getUser())) {
                // login exists but belongs to the currently managed user 
                return;
            }
            if (list.size() > 0) {
                throw new ValidatorException(
                        UIMessage.getErrorMessage(MESSAGE_BUNDLE, "admission_non_unique_user", null));
            }
            return;
        }
        throw new ValidatorException(
                UIMessage.getErrorMessage(MESSAGE_BUNDLE, "admission_error", new Object[]{"Database access failed."}));
    }

    /**
     * this method checks for duplicate accounts
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // logger.info("start::validation::" + component.getId() + " --> " + value.toString());
        checkDuplicateAccount(value.toString());
        // logger.info("Finished  validation.");
    }
}
