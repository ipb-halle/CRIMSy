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
package de.ipb_halle.lbac.plugin; 

import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.exp.LinkedDataAgent;
import de.ipb_halle.lbac.exp.LinkedDataHolder;
import de.ipb_halle.lbac.exp.LinkText;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@FacesComponent(value="UIAugmentedText")
public class UIAugmentedText extends UIOutput {

    private Logger logger;

    /**
     * Default constructor
     */
    public UIAugmentedText() {
        super();
        logger = LogManager.getLogger(this.getClass().getName());
        setRendererType(null); 
    }

    /**
     * decode browser response
     */
    @Override
    public void decode(FacesContext context) {

        Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
        String clientId = getClientId(context);

        try {
            String index = requestMap.get("linkedDataIndex");
            if (index == null) {
                // abort on requests not originating from links
                return;
            }

            LinkedDataAgent agent = (LinkedDataAgent) getAttributes().get("agent");
            LinkedDataHolder holder = (LinkedDataHolder) getAttributes().get("linkedDataHolder");

            agent.setLinkedData(holder
                .getLinkedData()
                .get(Integer.valueOf(index)));

        } catch(Exception e) {
            this.logger.warn("decode caught an exception: ", (Throwable) e);
        }
    }

    /**
     * encode editor / viewer elements in HTML
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!this.isRendered()) return;

        ResponseWriter writer = context.getResponseWriter();
        String clientId = this.getClientId(context);

        Map<String, LinkedData> linkMap;
        try {
            LinkedDataHolder holder = (LinkedDataHolder) getAttributes().get("linkedDataHolder");
            linkMap = setup(holder); 

        } catch(Exception e) {
            this.logger.warn("Could not obtain linked data");
            linkMap = new HashMap<> ();
        }

        
        findLinks(writer, linkMap, clientId, getValue().toString()); 

        writer.flush();
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
    }


    /**
     * Escape string for HTML / XML. 
     * @param s string to escape
     * @return escaped string
     */
    private String escape(String s) {
        if(s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
          .replace("\"", "\\\"")
          .replace("\n", "\\n")
          .replace("\r", "");
    }

    /**
     * scan a (HTML) String and transform it into a HTML output
     * string augmented by link elements
     * @param writer the ResponseWriter
     * @param linkMap the map of LinkedData
     * @param clientId the clientid of this UIObject
     * @param st the input string
     */
    public void findLinks(ResponseWriter writer, Map<String, LinkedData> linkMap, String clientId, String st) 
        throws IOException {

        // does not recognize tokens at end of input
        // when there is no trailing comma, dot, whitespace!
        Pattern pattern = Pattern.compile("#\\w+[,\\.\\s]{1}");
        Matcher matcher = pattern.matcher(st);

        int start = 0;
        int end = 0;
        while(matcher.find()) {
            end = matcher.start();
            if (end > start) {
                writer.write(st.substring(start, end));
            }
            start = matcher.end();
            insertLink(writer, linkMap, clientId, st.substring(end, start));
        }
        writer.write(st.substring(start));
    }

    /**
     * construct a replacement out of the given LinkedData object
     * @param clientId the clientId of this object
     * @param data a link which may point to material, items, experiments etc.
     * @return a JavaScript action to open a matching dialog window upon a user 
     * clicking the link
     */
    private String getReplacement(String clientId, LinkedData data) {
        if (data != null) {
            StringBuilder sb = new StringBuilder("openLinkDialog('");
            sb.append(data.getLinkedDataType().toString());
            sb.append("', '");
            sb.append(clientId);
            sb.append("', ");
            sb.append(Integer.toString(data.getRank()));
            sb.append("); return false;");
            return sb.toString();
        }
        return null;
    }

    /*
     * @param writer the ResponseWriter to receive the output
     * @param linkMap the map with LinkedData
     * @param clientId the clientId
     * @param linkMarker the link marker including a trailing character
     * (comma, dot or whitespace), which will not be rendered as a link.
     */
    public void insertLink(ResponseWriter writer, Map<String, LinkedData> linkMap, String clientId, String linkMarker) 
        throws IOException { 
        int len = linkMarker.length();
        String replacement = getReplacement(clientId, 
            linkMap.get(linkMarker.substring(1, len - 1)));
        if (replacement != null) {
            writer.startElement("a", this);
            writer.writeAttribute("href", "#", null);
            writer.writeAttribute("onclick", replacement, null);
            writer.writeText(linkMarker.substring(0, len - 1), null);
            writer.endElement("a");
            writer.writeText(linkMarker.substring(len - 1), null);
        } else {
            writer.writeText(linkMarker, null);
        }
    }

    private Map<String, LinkedData> setup(LinkedDataHolder holder) {
        Map<String, LinkedData> map = new HashMap<> ();
        for (LinkedData data : holder.getLinkedData()) {
            switch (data.getLinkedDataType()) {
                case LINK_DOCUMENT:
                case LINK_MATERIAL :
                case LINK_ITEM :
                case LINK_EXPERIMENT :
                    map.put(
                        ((LinkText) data.getPayload()).getText(), 
                        data);
            }
        }
        return map;
    }

}
