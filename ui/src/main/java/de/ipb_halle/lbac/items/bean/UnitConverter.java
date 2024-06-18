/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.util.units.Unit;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/**
 *
 * @author swittche
 */
@FacesConverter(value = "unitConverter")
public class UnitConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) throws ConverterException {
        return Unit.getUnit(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object t) throws ConverterException {
        Unit unit = (Unit) t;
        return unit.getUnit();
    }

}
