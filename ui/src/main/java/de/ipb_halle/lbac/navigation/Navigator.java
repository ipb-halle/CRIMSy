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
package de.ipb_halle.lbac.navigation;

import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.globals.NavigationConstants;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;


@Named("navigator")
@SessionScoped
@DependsOn({"userBean","systemSettings"})
public class Navigator implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nextPage = NavigationConstants.LOGIN;

    @Inject
    protected UserBean userBean;

    @Inject
    private SystemSettings systemSettings;

    /**
     * Initialize the Navigator for the session. 
     */
    @PostConstruct
    private void initNavigator() {
        initStartPage();
    }

    /**
     * check if access is granted. 
     * @return true if either login is not enforced (SETTING_FORCE_LOGIN is false) 
     * or current account is not the public account
     */
    public boolean getAccess() {
        return getStartPage().equals(NavigationConstants.DEFAULT);
    }

    private String getStartPage() {
        if (systemSettings.getBoolean(SystemSettings.SETTING_FORCE_LOGIN) 
          && userBean.getCurrentAccount().isPublicAccount()) {
            return NavigationConstants.LOGIN;
        } else {
            return NavigationConstants.DEFAULT;
        }
    }

    public String getNextPage() {
        return NavigationConstants.TEMPLATE_FOLDER + nextPage + NavigationConstants.TEMPLATE_EXT;
    }

    public void initStartPage() { 
        this.nextPage = getStartPage(); 
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    /**
     * Performs navigation actions
     *
     * @param destination
     * @return string navigation outcome
     */
    public String navigate(String destination) {

        if (!destination.isEmpty()) {
            nextPage = destination;
            return NavigationConstants.SUCCESS;
        }
        return NavigationConstants.FAILURE;
    }
}
