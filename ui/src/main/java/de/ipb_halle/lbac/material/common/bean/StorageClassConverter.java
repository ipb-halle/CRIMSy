package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.List;
import static javassist.CtMethod.ConstParameter.string;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Beans;

/**
 *
 * @author fmauz
 */
@FacesConverter("StorageClassConverter")
public class StorageClassConverter implements Converter {
    private List<StorageClass> storageClasses;
    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    public StorageClassConverter() {
        storageClasses= Beans.getReference(MaterialBean.class).getStorageInformationBuilder().getPossibleStorageClasses();
    }

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        if (string == null||string.isEmpty()) {
            return null;
        }
        for(StorageClass sc:storageClasses){
            if(sc.getName().startsWith(string)){
                return sc;
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) throws ConverterException {
        if (o == null) {
            return "";
        }
        if (o instanceof StorageClass) {
            StorageClass sc = (StorageClass) o;
            return sc.name;
        } else {
            return "";
        }
    } 
}
