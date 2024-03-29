package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang.exception.ExceptionUtils;
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
            logger.error(ExceptionUtils.getStackTrace(e));
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
