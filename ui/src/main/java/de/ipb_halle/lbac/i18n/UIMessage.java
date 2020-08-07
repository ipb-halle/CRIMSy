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
package de.ipb_halle.lbac.i18n;

import com.corejsf.util.Messages;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/*
import net.bootsfaces.utils.FacesMessages;
 */

/**
 * This class provides global a message notification. Messages will be published
 * in bootstrap growl style (@see &lt;b:growl/&gt; tag). This replaces the
 * &lt;b:alert/&gt; tag.
 */
public class UIMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * default message bundle for the Leibniz Bioactives Cloud
     */
    private static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    /**
     * Prepare a FacesMessage with message severity SEVERITY_ERROR from the
     * given message bundle
     *
     * @param bundle the message bundle, defaults to MESSAGE_BUNDLE
     * @param msgKey the key for the message bundle
     * @param msgArgs optional arguments to the message, may be null
     * @return the FacesMessage with severity SEVERITY_ERROR
     */
    public static FacesMessage getErrorMessage(String bundle, String msgKey, Object[] msgArgs) {
        FacesMessage msg = Messages.getMessage(bundle != null ? bundle : MESSAGE_BUNDLE, msgKey, msgArgs);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        return msg;
    }

    /**
     * Prepare a FacesMessage with message severity <code>severity</code> for
     * client <code>client</code>.
     *
     * @param severity the message severity
     * @param client the client id or null
     * @param bundle the message bundle
     * @param msgKey the key for the message bundle
     * @param msgArgs optional arguments for the message (i.e. in messages like
     * 'Hi {0}. We have {1} messages for you.')
     */
    private static void add(
            FacesMessage.Severity severity,
            UIClient client,
            String bundle,
            String msgKey,
            Object[] msgArgs) {
        FacesMessage msg = Messages.getMessage(bundle != null ? bundle : MESSAGE_BUNDLE, msgKey, msgArgs);
        msg.setSeverity(severity);
        FacesContext.getCurrentInstance().addMessage(client != null ? client.getClientId() : null, msg);
    }

    // error
    public static void error(String msgKey) {
        error(null, MESSAGE_BUNDLE, msgKey, null);
    }

    public static void error(String msgKey, Object[] msgArgs) {
        error(null, MESSAGE_BUNDLE, msgKey, msgArgs);
    }

    public static void error(String bundle, String msgKey) {
        error(null, bundle, msgKey, null);
    }

    public static void error(UIClient client, String bundle, String msgKey) {
        error(client, bundle, msgKey, null);
    }

    public static void error(UIClient client, String bundle, String msgKey, Object[] msgArgs) {
        add(FacesMessage.SEVERITY_ERROR, client, bundle, msgKey, msgArgs);
    }

    // fatal
    public static void fatal(String msgKey) {
        fatal(null, MESSAGE_BUNDLE, msgKey, null);
    }

    public static void fatal(String msgKey, Object[] msgArgs) {
        fatal(null, MESSAGE_BUNDLE, msgKey, msgArgs);
    }

    public static void fatal(String bundle, String msgKey) {
        fatal(null, bundle, msgKey, null);
    }

    public static void fatal(UIClient client, String bundle, String msgKey) {
        fatal(client, bundle, msgKey, null);
    }

    public static void fatal(UIClient client, String bundle, String msgKey, Object[] msgArgs) {
        add(FacesMessage.SEVERITY_FATAL, client, bundle, msgKey, msgArgs);
    }

    // info
    public static void info(String msgKey) {
        info(null, MESSAGE_BUNDLE, msgKey, null);
    }

    public static void info(String msgKey, Object[] msgArgs) {
        info(null, MESSAGE_BUNDLE, msgKey, msgArgs);
    }

    public static void info(String bundle, String msgKey) {
        info(null, bundle, msgKey, null);
    }

    public static void info(UIClient client, String bundle, String msgKey) {
        info(client, bundle, msgKey, null);
    }

    public static void info(UIClient client, String bundle, String msgKey, Object[] msgArgs) {
        add(FacesMessage.SEVERITY_INFO, client, bundle, msgKey, msgArgs);
    }

    //warn
    public static void warn(String msgKey) {
        warn(null, MESSAGE_BUNDLE, msgKey, null);
    }

    public static void warn(String msgKey, Object[] msgArgs) {
        warn(null, MESSAGE_BUNDLE, msgKey, msgArgs);
    }

    public static void warn(String bundle, String msgKey) {
        warn(null, bundle, msgKey, null);
    }

    public static void warn(UIClient client, String bundle, String msgKey) {
        warn(client, bundle, msgKey, null);
    }

    public static void warn(UIClient client, String bundle, String msgKey, Object[] msgArgs) {
        add(FacesMessage.SEVERITY_WARN, client, bundle, msgKey, msgArgs);
    }

}
