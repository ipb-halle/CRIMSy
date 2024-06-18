/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.i18n.UIMessage;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author swittche
 */
@FacesValidator("IndexValidator")
public class IndexValidator implements Validator<String> {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private MaterialIndexBean materialIndexBean;

    @Override
    public void validate(FacesContext fc, UIComponent uic, String stringToValidate) throws ValidatorException {
        String indexType = materialIndexBean.getIndexCatergory();

        try {
            createValidator(indexType).validate(stringToValidate);
        } catch (Exception ex) {

            throw new ValidatorException(
                        UIMessage.getErrorMessage(MESSAGE_BUNDLE, "index_validator_error", null), 
                        ex);

//            ArrayList<FacesMessage> errorMessg = new ArrayList<FacesMessage>();
//            errorMessg.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ": The input is not valid!"));
//            throw new ValidatorException(errorMessg, ex);

        }
    }

    private IndexValidatorInterface createValidator(String indexValue) throws Exception {

        return switch (indexValue) {
            case "CAS/RN" ->
                new CASValidator();

            default ->
                new AlwaysTrueValidator();
        };
    }

    private class AlwaysTrueValidator implements IndexValidatorInterface {

        @Override
        public boolean validate(String indexValue) throws Exception {
            return true;
        }

    }
}
