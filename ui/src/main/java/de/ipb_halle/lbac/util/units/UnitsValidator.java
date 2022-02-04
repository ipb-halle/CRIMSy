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
package de.ipb_halle.lbac.util.units;

import de.ipb_halle.lbac.i18n.UIMessage;
import java.io.Serializable;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@FacesValidator("UnitsValidator")
public class UnitsValidator implements Validator,Serializable {

    private final static long serialVersionUID = 1L;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private Logger logger;

    /**
     * default constructor
     */
    public UnitsValidator() {
        logger = LogManager.getLogger(this.getClass().getName());
    }


    private void checkUnits(String unitString) throws ValidatorException {
        if ((unitString == null) || (unitString.trim().length() == 0)) {
            throw new ValidatorException(
                        UIMessage.getErrorMessage(MESSAGE_BUNDLE, "unitValidation_empty", null));
        }
        
        try {
            if (getUnitSet(unitString).size() == 0) {
                throw new ValidatorException(
                    UIMessage.getErrorMessage(MESSAGE_BUNDLE, "unitValidation_empty", null));
            }
        } catch(IllegalArgumentException e) {
            throw new ValidatorException(
                    UIMessage.getErrorMessage(MESSAGE_BUNDLE, "unitValidation_unrecognized", null));
        }
    }

    /**
     * Convert a comma or space separated list of units into a set of <code>Unit</code>s
     * @param unitString a string of units of measurement (e.g. "mg, g, kg")
     * @return a set of recognized <code>Unit</code> objects. The implementation uses a 
     * <code>LinkedHashSet</code> to preserve the insertion order.
     * @throws IllegalArgumentException if the string contains an unrecognized string
     */
    public static Set<Unit> getUnitSet(String unitString) {
        Set<Unit> units = new LinkedHashSet<> ();
        if (unitString != null) {
            for (String st: unitString.split("[ ,]")) {
                if (st.length() > 0) {
                        units.add(Unit.getUnit(st));
                }
            }
        }
        return units;
    }

    /**
     * This method checks for unit string validity. A valid string is non-empty and composed 
     * of units (@see <code>Unit</code>) separated by spaces and/or commas. 
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Object tmpValue = value != null ? value : "";

        // logger.info("start::validation::" + component.getId() + " --> " + tmpValue.toString());
        checkUnits(tmpValue.toString());
        // logger.info("Finished  validation.");
    }
}
