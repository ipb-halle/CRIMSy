package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Beans;

/**
 *
 * @author fmauz
 */
@FacesConverter("HazardConverter")
public class HazardConverter implements Converter {

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    public HazardConverter() {

    }

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        if (string == null) {
            return null;
        }
        try {
            return Beans.getReference(HazardService.class).getHazardByName(string);
        } catch (Exception e) {
            logger.error("getAsObject() caught an exception:", (Throwable) e);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) throws ConverterException {
        if (o == null) {
            return "";
        }
        if (o instanceof HazardType) {
            HazardType ht = (HazardType) o;
            return ht.getName();
        } else {
            return "";
        }
    }

}
