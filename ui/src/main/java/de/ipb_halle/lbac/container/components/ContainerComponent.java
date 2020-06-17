/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.container.components;

import de.ipb_halle.lbac.container.Container;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@FacesComponent("ContainerComponent")
public class ContainerComponent extends UIComponentBase implements Serializable{

    private final Map<String, Renderer> renderer = new HashMap<>();
    private final Logger logger = LogManager.getLogger(this.getClass().getName());

    public ContainerComponent() {
        renderer.put("WELLPLATE", new WellPlateRenderer());
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {

        Object o = getAttributes().get("container");
        Container container = null;
        if (o != null) {
            container = (Container) o;
        }

        ResponseWriter responseWriter = context.getResponseWriter();
        if (container == null) {
            responseWriter.append("No Container choosen");
        } else {
            Renderer r = renderer.get(container.getType().getName());
            if (r != null) {
                r.render(responseWriter, container, this);
            }
        }
        responseWriter.flush();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void decode(FacesContext context) {
        Object positionAttribute = getAttributes().get("positions");
        if (positionAttribute != null) {

            Set positions = (Set) positionAttribute;
            for (String o : context.getExternalContext().getRequestParameterMap().keySet()) {
                if (o.startsWith("containerPlace")) {
                    boolean isOn = context.getExternalContext().getRequestParameterMap().get(o).equals("on");
                    if (isOn) {
                        positions.add(o.split(":")[1]);
                    }
                }
            }
        }

    }

    @Override
    public String getFamily() {
        return "Container";
    }
}
