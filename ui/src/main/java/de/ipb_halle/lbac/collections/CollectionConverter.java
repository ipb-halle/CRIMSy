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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.file.FileUploadBean;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Named
@FacesConverter(value = "collectionConverter")
public class CollectionConverter implements Converter<Collection> {

    @Inject
    private CollectionService collectionService;
    
    @Inject 
    private CollectionBean collectionBean;

    @Inject
    private FileUploadBean bean;

    Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public Collection getAsObject(FacesContext fc, UIComponent uic, String value) {
        logger.info("THIS IS THE VALUE IN CONVERTER:" + value);
        for (Collection c : collectionBean.getCreatableLocalCollections()) {
            if (c.getName().equals(value)) {
                logger.info("Would conver to " + c);
                return c;
            }
        }
        logger.info("Would conver to null");
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Collection object) {
        return object.getName();
    }
}
