/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.admission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.molecularfaces.MolPluginCore.PluginType;

/**
 * This bean manages the user preferences for the plugin types and provides
 * lists of available plugin types.
 *
 * @author flange
 */
@Dependent
public class UserPluginSettingsBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Key in the preferences table for the chemical structure plugin type.
     */
    private static final String MOLPLUGINTYPE_PREFERENCE_KEY = "MolPluginType";

    /**
     * Name of the web.xml context-param that specifies the available chemical
     * structure plugin types.
     */
    private static final String WEBXML_AVAILABLE_MOLPLUGINTYPES = "de.ipb_halle.lbac.AvailableMolPluginTypes";

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private UserBean userBean;

    private List<String> availableMolPluginTypes = new ArrayList<>();

    private String defaultMolPluginType = "";

    private Logger logger;

    /**
     * default constructor
     */
    public UserPluginSettingsBean() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Initializes the state of this bean:
     * <ul>
     * <li>assembles a lists of available plugins types from the capabilities of
     * MolecularFaces and from the settings in web.xml and</li>
     * <li>sets the default plugin types from the first elements of those
     * lists.</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        String webXmlString = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getInitParameter(WEBXML_AVAILABLE_MOLPLUGINTYPES);

        if ((webXmlString != null) && !webXmlString.isEmpty()) {
            List<String> supported = new ArrayList<>();
            for (PluginType pt : PluginType.values()) {
                supported.add(pt.toString());
            }

            List<String> available = new ArrayList<>();
            for (String pluginType : webXmlString.split(",")) {
                if (supported.contains(pluginType)) {
                    available.add(pluginType);
                }
            }

            availableMolPluginTypes = Collections.unmodifiableList(available);

            if (available.size() > 0) {
                defaultMolPluginType = available.get(0);
            }
        }
    }

    /**
     * Returns the preferred chemical structure plugin type or the default type
     * if a preference does not exist.
     * 
     * @return chemical structure plugin type
     */
    public String getPreferredMolPluginType() {
        String pref = preferenceService.getPreferenceValue(
                userBean.getCurrentAccount(), MOLPLUGINTYPE_PREFERENCE_KEY,
                defaultMolPluginType);

        if (availableMolPluginTypes.contains(pref)) {
            return pref;
        } else {
            return defaultMolPluginType;
        }
    }

    /**
     * Sets the preferred chemical structure plugin type.
     * 
     * @param pluginType chemical structure plugin type
     */
    public void setPreferredMolPluginType(String pluginType) {
        if (availableMolPluginTypes.contains(pluginType)) {
            preferenceService.setPreference(userBean.getCurrentAccount(),
                    MOLPLUGINTYPE_PREFERENCE_KEY, pluginType);
        } else {
            /*
             * TODO: Can this ever happen??? Should we return success/fail as
             * boolean to be able to notify the user?
             */
        }
    }

    /**
     * Returns the list of available chemical structure plugin types.
     * 
     * @return an unmodifiable list of chemical structure plugin types.
     */
    public List<String> getAllMolPluginTypes() {
        return availableMolPluginTypes;
    }
}
