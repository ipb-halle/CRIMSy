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
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.util.WebXml;
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
    protected static final String WEBXML_AVAILABLE_MOLPLUGINTYPES = "de.ipb_halle.lbac.AvailableMolPluginTypes";

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private UserBean userBean;

    private List<String> availableMolPluginTypes;

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
        List<String> supported = getSupportedMolPluginTypes();
        List<String> available = getAvailableMolPluginTypesFromWebXml();

        // intersection of both lists
        available.retainAll(supported);

        this.availableMolPluginTypes = Collections.unmodifiableList(available);

        if (availableMolPluginTypes.size() > 0) {
            defaultMolPluginType = availableMolPluginTypes.get(0);
        }
    }

    /**
     * Returns a set of chemical structure plugins supported by MolecularFaces.
     */
    private List<String> getSupportedMolPluginTypes() {
        List<String> supported = new ArrayList<>();
        for (PluginType pt : PluginType.values()) {
            supported.add(pt.toString());
        }

        return supported;
    }

    /**
     * Returns a set of chemical structure plugins made available in web.xml.
     */
    private List<String> getAvailableMolPluginTypesFromWebXml() {
        List<String> available = new ArrayList<>();

        String webXmlString = WebXml
                .getContextParam(WEBXML_AVAILABLE_MOLPLUGINTYPES);
        if ((webXmlString != null) && !webXmlString.isEmpty()) {
            for (String pluginType : webXmlString.split(",")) {
                available.add(pluginType);
            }
        }

        return available;
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
     * @return flag indicating that the plugin type was set as preference
     */
    public boolean setPreferredMolPluginType(String pluginType) {
        if (availableMolPluginTypes.contains(pluginType)) {
            preferenceService.setPreference(userBean.getCurrentAccount(),
                    MOLPLUGINTYPE_PREFERENCE_KEY, pluginType);
            return true;
        } else {
            logger.warn("Could not set the plugin type '" + pluginType
                    + "' as preferred plugin type. The available plugin types are "
                    + availableMolPluginTypes);
            return false;
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

    /**
     * Returns the default chemical structure plugin type.
     * 
     * @return default chemical structure plugin type or empty string if no
     *         plugin types are available.
     */
    protected String getDefaultMolPluginType() {
        return defaultMolPluginType;
    }
}
