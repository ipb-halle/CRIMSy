/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.material.biomaterial;

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
@FacesConverter(value="taxonomyLevelConverter")
public class TaxonomyLevelConverter implements Converter<TaxonomyLevel> {

    @Inject
    protected TaxonomyService taxonomyService;

    @Override
    public TaxonomyLevel getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        for (TaxonomyLevel l : taxonomyService.loadTaxonomyLevel()) {
            if (l.getName().equals(string)) {
                return l;
            }
        }
        throw new ConverterException("could not find object of string " + string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, TaxonomyLevel t) throws ConverterException {
        return t.getName();
    }

}
