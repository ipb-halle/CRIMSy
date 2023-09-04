/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.items.Solvent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author swittche
 */
@FacesConverter(value = "solventConverter")

public class SolventConverter implements Converter {

    @Inject
    private ItemBean itemBean;

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        for (Solvent s : itemBean.getAvailableSolvents()) {
            if (s.getLocalizedName().equals(string)) {
                return s;
            }
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object t) throws ConverterException {
        Solvent solvent = (Solvent) t;
        return solvent.getLocalizedName();
    }
}
