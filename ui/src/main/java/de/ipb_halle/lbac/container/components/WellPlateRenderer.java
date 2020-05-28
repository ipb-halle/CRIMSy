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
import javax.faces.component.UIComponentBase;
import javax.faces.context.ResponseWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class WellPlateRenderer implements Renderer {

    Logger logger = logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public void render(
            ResponseWriter responseWriter,
            Container container,
            UIComponentBase component) throws IOException {
        int x = Integer.valueOf(container.getDimension().split(";")[0]);
        int y = Integer.valueOf(container.getDimension().split(";")[1]);

        responseWriter.append("<table>");
        responseWriter.append("<tr>");
        for (int i = 0; i <= x; i++) {
            if (i > 0) {
                responseWriter.append("<th>");
                responseWriter.append(Integer.toString(i));
                responseWriter.append("</th>");
            } else {
                responseWriter.append("<th>");

                responseWriter.append("</th>");
            }
        }
        responseWriter.append("</tr>");
        for (int j = 0; j < y; j++) {
            responseWriter.append("<tr>");
            for (int i = 0; i < x; i++) {
                if (i == 0) {
                    responseWriter.append("<th>");
                   
                    responseWriter.append(Character.toString((char) (65 + j)));
                    responseWriter.append("</th>");
                }
                responseWriter.append("<th>");
                responseWriter.startElement("input", component);
                responseWriter.writeAttribute("type", "checkbox", null);
                responseWriter.writeAttribute("name", "containerPlace:" + i + "-" + j, "name");
                if (container.getItemAtPos(i, j, 0) != null) {
                    responseWriter.writeAttribute("disabled", container.getItemAtPos(i, j, 0) != null, "disabled");
                    String toolTip = "Item id: " + container.getItemAtPos(i, j, 0).getId();
                    responseWriter.writeAttribute("title", toolTip, "title");
                }
                try {

                } catch (Exception e) {
                    logger.warn(e);
                }
                responseWriter.endElement("input");
                responseWriter.append("</th>");

            }
            responseWriter.append("</tr>");
        }
        responseWriter.append("</table>");
    }

}
