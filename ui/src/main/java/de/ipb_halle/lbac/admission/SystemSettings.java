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

import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Named("systemSettings")
@ApplicationScoped
public class SystemSettings implements Serializable {

    private final static long serialVersionUID = 1L;

    public final static String SETTING_FORCE_LOGIN = "SETTING_FORCE_LOGIN";
    public final static String SETTING_AGENCY_SECRET = "SETTING_AGENCY_SECRET";
    public static final String SETTING_LOGIN_CUSTOM_TEXT = "SETTING_LOGIN_CUSTOM_TEXT";
    public static final String SETTING_INSTITUTION_WEB = "SETTING_INSTITUTION_WEB";
    public static final String SETTING_GDPR_CONTACT = "SETTING_GDPR_CONTACT";

    private transient Logger logger;
    private Map<String, InfoObject> stringSettings;
    private Map<String, InfoObject> boolSettings;

    @Inject
    protected InfoObjectService infoObjectService;

    @Inject
    protected GlobalAdmissionContext globalAdmissionContext;

    protected MessagePresenter messagePresenter;

    public SystemSettings() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.stringSettings = new LinkedHashMap<>();
        this.boolSettings = new LinkedHashMap<>();
    }

    @PostConstruct
    public void SystemSettingsInit() {

        //*** set default values ***
        initProperty(this.stringSettings, SETTING_GDPR_CONTACT, "Name, Vorname");
        initProperty(this.stringSettings, SETTING_INSTITUTION_WEB, "Homepage");
        initProperty(this.boolSettings, SETTING_FORCE_LOGIN, "True");
        initProperty(this.stringSettings, SETTING_LOGIN_CUSTOM_TEXT, "");
        messagePresenter = JsfMessagePresenter.getInstance();
    }

    /**
     * Checks if the given property is in the database and returns its value. If
     * its absent returns false
     *
     * @param prop
     * @return value of property or false if not in database
     */
    public Boolean getBoolean(String prop) {
        if (boolSettings.containsKey(prop)) {
            return Boolean.valueOf(boolSettings.get(prop).getValue());
        } else {
            return false;
        }
    }

    /**
     * Returns the value of a given property. If not present in db returns an
     * empty string
     *
     * @param prop
     * @return value of property or empty string if property is not in db
     */
    public String getString(String prop) {
        if (stringSettings.containsKey(prop)) {
            return stringSettings.get(prop).getValue();
        } else {
            return "";
        }
    }

    public List<InfoObject> getBoolSettings() {
        return new ArrayList<>(boolSettings.values());
    }

    public List<InfoObject> getStringSettings() {
        return new ArrayList<>(stringSettings.values());
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
        List<InfoObject> items = new ArrayList<>();
        items.addAll(this.stringSettings.values());
        items.addAll(this.boolSettings.values());

        ListIterator<InfoObject> iter = items.listIterator();
        while (iter.hasNext()) {
            InfoObject ie = iter.next();
            infoObjectService.save((InfoObject) ie
                    .setOwner(this.globalAdmissionContext.getAdminAccount())
                    .setACList(this.globalAdmissionContext.getAdminOnlyACL()));
        }
        messagePresenter.info("SETTINGS_SAVED");
    }
}
