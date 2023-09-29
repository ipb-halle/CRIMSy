/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.material.common.bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author swittche
 */
@FacesValidator("IndexValidator")
public class IndexValidator implements Validator<String> {

    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private MaterialIndexBean materialIndexBean;

    @Override
    public void validate(FacesContext fc, UIComponent uic, String stringToValidate) throws ValidatorException {
        String indexType = materialIndexBean.getIndexCatergory();

        try {
            createValidator(indexType).validate(stringToValidate);
        } catch (Exception ex) {

            ArrayList<FacesMessage> errorMessg = new ArrayList<FacesMessage>();
            errorMessg.add(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ": The input is not valid!"));
            throw new ValidatorException(errorMessg, ex);

        }
    }

    private IndexValidatorInterface createValidator(String indexValue) throws Exception {

        return switch (indexValue) {
            case "CAS/RN" ->
                new CASValidator();

            default ->
                doSomething("");
        };
    }

    private IndexValidatorInterface doSomething(String s) throws Exception {
        return new AlwaysTrueValidator();
    }

    private class AlwaysTrueValidator implements IndexValidatorInterface {

        @Override
        public boolean validate(String indexValue) throws Exception {
            return true;
        }

    }
}
