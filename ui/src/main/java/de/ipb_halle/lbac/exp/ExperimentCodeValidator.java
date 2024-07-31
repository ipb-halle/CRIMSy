package de.ipb_halle.lbac.exp;


import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

@FacesValidator("experimentCodeValidator")
public class ExperimentCodeValidator implements Validator<String> {

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, String s) throws ValidatorException {
        if (s == null || s.trim().isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation failed", "Experiment Code is required.");
            throw new ValidatorException(msg);
        }
    }
}
