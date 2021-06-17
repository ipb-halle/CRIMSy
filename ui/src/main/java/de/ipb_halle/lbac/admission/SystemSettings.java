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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.service.InfoObjectService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@Named("systemSettings")
@ApplicationScoped
public class SystemSettings implements Serializable {

    private final static long serialVersionUID = 1L;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public final static String SETTING_FORCE_LOGIN = "SETTING_FORCE_LOGIN";
    public final static String SETTING_AGENCY_SECRET = "SETTING_AGENCY_SECRET";

    private transient Logger logger;

    /**
     * LDAP configuration properties
     */
    private Map<String, InfoObject> stringSettings;
    private Map<String, InfoObject> boolSettings;

    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    /*
     * default constructor
     */
    public SystemSettings() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.stringSettings = new LinkedHashMap<String, InfoObject>();
        this.boolSettings = new LinkedHashMap<String, InfoObject>();
    }

    @PostConstruct
    public void SystemSettingsInit() {

        //*** set default values ***
        initProperty(this.stringSettings, "SETTING_GDPR_CONTACT", "Name, Vorname");
        initProperty(this.stringSettings, "SETTING_INSTITUTION_WEB", "Homepage");
        initProperty(this.boolSettings, SETTING_FORCE_LOGIN, "True");
        initProperty(this.stringSettings, SETTING_AGENCY_SECRET, "");
    }

    public Boolean getBoolean(String prop) {
        return Boolean.valueOf(boolSettings.get(prop).getValue());
    }

    public String getString(String prop) {
        return stringSettings.get(prop).getValue();
    }

    public List<InfoObject> getBoolSettings() {
        return new ArrayList<InfoObject> (boolSettings.values());
    }

    public List<InfoObject> getStringSettings() {
        return new ArrayList<InfoObject> (stringSettings.values());
    }


    /**
     * Initialize a InfoEntity with key, value, owner and ACL
     *
     * @param list the map with all settings of equal type (e.g. string, bool)
     * @param key the key
     * @param value the value
     * @return the initialized (but detached) InfoEntity
     */
    private void initProperty(Map<String, InfoObject> map, String key, String value) {
        InfoObject ie = infoObjectService.loadByKey(key);
        if (ie == null) {
            ie = new InfoObject(key, value);
        }
        map.put(key, ie); 
    }

    /**
     * save the system settings to the database
     */
    public void save() {
        List<InfoObject> items = new ArrayList<InfoObject> ();
        items.addAll(this.stringSettings.values());
        items.addAll(this.boolSettings.values());

        ListIterator<InfoObject> iter = items.listIterator();
        while (iter.hasNext()) {
            InfoObject ie = iter.next();
            infoObjectService.save((InfoObject) ie
                    .setOwner(this.globalAdmissionContext.getAdminAccount())
                    .setACList(this.globalAdmissionContext.getAdminOnlyACL()));
        }
        UIMessage.info(MESSAGE_BUNDLE, "SETTINGS_SAVED");
    }
}
