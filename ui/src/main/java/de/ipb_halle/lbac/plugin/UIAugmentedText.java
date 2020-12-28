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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import javax.el.ValueExpression;
// import javax.el.ELContext;
// import javax.faces.component.EditableValueHolder;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
// import javax.faces.render.FacesRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



@FacesComponent(value="UIAugmentedText")
public class UIAugmentedText extends UIInput {    

    private Logger logger;

    /**
     * Default constructor
     */
    public UIAugmentedText() {
        super();
        logger = LogManager.getLogger(this.getClass().getName());
        logger.info("UIAugmentedText constructor");

        setRendererType(null); 
    }

        /**
         * decode browser response
         */
      @Override
    public void decode(FacesContext context) {
                Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
                String clientId = this.getClientId(context);

/*
        String pt = (String) getAttributes().get("pluginType");
        setPluginType(clientId, pt);
*/

        String value = requestMap.get(clientId);
        this.logger.info("decode(): " + value);
        setSubmittedValue(value);
        setValid(true);
    }

    /**
     * encode editor / viewer elements in HTML
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!this.isRendered()) return;

        ResponseWriter writer = context.getResponseWriter();
        String clientId = this.getClientId(context);

        writer.write("HALLO");

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

/*
 * transform LinkedData records into clickable links
 * (h:commandLink etc.)
 *
    public void setup() {
        linkMap = new HashMap<> ();
        linkMap.put("foo", "http://foofoo.foo");
        linkMap.put("bar", "http://barbar.bar");
    }

    public String findLinks(String st) {
        StringBuilder sb = new StringBuilder();

        // does not recognize tokens at end of input
        // when there is no trailing comma, dot, whitespace!
        Pattern pattern = Pattern.compile("#\\w+[,\\.\\s]{1}");
        Matcher matcher = pattern.matcher(st);

        int start = 0;
        int end = 0;
        while(matcher.find()) {
            end = matcher.start();
            if (end > start) {
                sb.append(st.substring(start, end));
            }
            start = matcher.end();
            insertLink(sb, st.substring(end, start));
        }
        sb.append(st.substring(start));
        return sb.toString();
    }

     *
     * @param sb the StringBuilder to which the link should be appended
     * @param linkMarker the link marker including a trailing character
     * (comma, dot or whitespace), which will not be rendered as a link.
     *
    public void insertLink(StringBuilder sb, String linkMarker) {
        int len = linkMarker.length();
        String replacement = this.linkMap.get(linkMarker.substring(1, len - 1));
        if (replacement != null) {
            sb.append("<a href='");
            sb.append(replacement);
            sb.append("'>");
            sb.append(linkMarker.substring(0, len - 1));
            sb.append("</a>");
            sb.append(linkMarker.substring(len - 1));
        } else {
            sb.append(linkMarker);
        }
    }
*/
}
