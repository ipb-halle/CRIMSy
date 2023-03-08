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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.material.MessagePresenter;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@FacesValidator("ShortcutValidator")
public class ShortcutValidator implements Validator,Serializable {
    /*
     * This pattern checks for alphabetic characters. Lower case is allowed in
     * the input, but the shortcut will become upper case during persistence.
     */
    private static final Pattern pattern = Pattern.compile("^[A-Za-z]+$");

    @Inject
    private MessagePresenter presenter;

    @Inject
    private MemberService memberService;

    @Inject
    private UserMgrBean userMgrBean;

    private Logger logger;

    /**
     * default constructor
     */
    public ShortcutValidator() {
        logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * test constructor
     */
    protected ShortcutValidator(MemberService memberService,
            UserMgrBean userMgrBean, MessagePresenter presenter) {
        this.memberService = memberService;
        this.userMgrBean = userMgrBean;
        this.presenter = presenter;
    }

    /**
     * Checks for duplicate shortcuts. Only local (i.e. LOCAL and LDAP) subsystems
     * will be checked. Empty shortcuts are always allowed.
     *
     * @param shortcut the shortcut to be checked
     * @throws ValidatorException upon duplicate shortcuts or on internal failure
     */
    private void checkDuplicateShortcut(String shortcut) throws ValidatorException {
        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(MemberService.PARAM_SHORTCUT, shortcut);
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE,
                new AdmissionSubSystemType[]{AdmissionSubSystemType.LOCAL, AdmissionSubSystemType.LDAP});

        List<User> list = this.memberService.loadUsers(cmap);
        if (list != null) {
            if ((list.size() == 1) && list.get(0).equals(this.userMgrBean.getUser())) {
                // shortcut exists but belongs to the currently managed user 
                return;
            }
            if (list.size() > 0) {
                throw new ValidatorException(
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                presenter.presentMessage(
                                        "admission_non_unique_shortcut"),
                                presenter.presentMessage(
                                        "admission_non_unique_shortcut_detail")));
            }
            return;
        }
        throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        presenter.presentMessage("admission_error"),
                        presenter.presentMessage("admission_error_detail",
                                "Database access failed.")));
    }

    private void checkPattern(String shortcut) throws ValidatorException {
        if (pattern.matcher(shortcut).matches()) {
            return;
        }

        throw new ValidatorException(
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        presenter.presentMessage("admission_shortcut_wrongpattern"),
                        presenter.presentMessage("admission_shortcut_wrongpattern_detail",
                                "Database access failed.")));
    }

    /**
     * This method checks for duplicate shortcuts.
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Object tmpValue = value != null ? value : "";
        String shortcut = tmpValue.toString();

        // logger.info("start::validation::" + component.getId() + " --> " + tmpValue.toString());
        if (shortcut.isEmpty()) {
            return;
        }
        checkPattern(shortcut);
        checkDuplicateShortcut(shortcut);
        // logger.info("Finished  validation.");
    }
}
