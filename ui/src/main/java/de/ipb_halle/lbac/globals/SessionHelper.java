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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.admission.User;

import java.io.Serializable;
import java.util.Map;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SessionHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static String CU_KEY = "currentUser";

    private Logger logger;

    public SessionHelper() {
        logger = LogManager.getLogger(SessionHelper.class);
    }

    public User getCurrentUser() {
        try {
            return (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(CU_KEY);
        } catch (Exception e) {
            logger.error("getCurrentUser() caught an exception:", (Throwable) e);
            return null;
        }
    }

    public Boolean setCurrentUser(User user) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(CU_KEY, user);
            return true;
        } catch (Exception e) {
            logger.error("setCurrentUser() caught an exception:", (Throwable) e);
            return false;
        }
    }

    public String getScope() {
        FacesContext    facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext   = facesContext.getExternalContext();

        Map scopeMap = externalContext.getRequestMap();
        if (scopeMap.containsValue(this)) return "request";

        scopeMap = facesContext.getViewRoot().getViewMap();
        if (scopeMap.containsValue(this)) return "view";

        scopeMap = externalContext.getSessionMap();
        if (scopeMap.containsValue(this)) return "session";

        scopeMap = externalContext.getApplicationMap();
        if (scopeMap.containsValue(this)) return "application";

        return "unknown";
    }
}
