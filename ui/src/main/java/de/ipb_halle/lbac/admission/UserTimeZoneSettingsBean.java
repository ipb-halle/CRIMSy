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
import java.time.DateTimeException;
import java.time.ZoneId;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ipb_halle.lbac.timezone.TimeZonesBean;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;
import de.ipb_halle.lbac.util.pref.PreferenceService;
import de.ipb_halle.lbac.util.pref.PreferenceType;

/**
 * This bean manages the user preferences for the time zone.
 *
 * @author flange
 */
@SessionScoped
public class UserTimeZoneSettingsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Key in the preferences table for the time zone.
     */
    protected static final String TIMEZONE_PREFERENCE_KEY = PreferenceType.TimeZone
            .toString();

    @Inject
    private TimeZonesBean timeZonesBean;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    private UserBean userBean;

    @Inject
    private LoggingProfiler loggingProfiler;
    
    private String pref = null;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    
    /* empty default constructor */
    public UserTimeZoneSettingsBean() {
    }
    
    /**
     * Initializes the state of this bean: does nothing.
     */
    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("UserTimeZoneSettingsBean");

        loggingProfiler.profilerStop("UserTimeZoneSettingsBean");
    }

    /**
     * Test constructor with dependency injection.
     *
     * @param preferenceService mock of {@link PreferenceService}
     * @param userBean mock of {@link UserBean}
     * @param timeZonesBean mock of {@link TimeZonesBean}
     */
    protected UserTimeZoneSettingsBean(PreferenceService preferenceService,
            UserBean userBean, TimeZonesBean timeZonesBean) {
        this.preferenceService = preferenceService;
        this.userBean = userBean;
        this.timeZonesBean = timeZonesBean;
    }

    /**
     * Resets the preference upon login of a user. The next call to
     * {@link #getPreferredTimeZone()} will reload the preference from the
     * database.
     *
     * @param evt
     */
    public void onLogin(@Observes LoginEvent evt) {
        loggingProfiler.profilerStart("UserTimeZoneSettingsBean.onLogin");
        pref = null;
        loggingProfiler.profilerStop("UserTimeZoneSettingsBean.onLogin");
    }

    /**
     * Returns the preferred time zone or the default time zone if a preference
     * does not exist.
     *
     * @return valid time zone id according to {@link ZoneId#getId()}
     */
    public String getPreferredTimeZone() {
        if (pref == null) {
            loadPreferredTimeZone();
        }

        return pref;
    }

    private void loadPreferredTimeZone() {
        String prefFromDB = preferenceService.getPreferenceValue(
                userBean.getCurrentAccount(), TIMEZONE_PREFERENCE_KEY,
                timeZonesBean.getDefaultTimeZone());

        // Check if the zone is valid and do transformations if necessary.
        try {
            pref = ZoneId.of(prefFromDB).getId();
        } catch (DateTimeException e) {
            pref = timeZonesBean.getDefaultTimeZone();
        }
    }

    /**
     * Sets the preferred time zone.
     *
     * @param timeZone time zone
     * @return flag indicating that the time zone was set as preference
     */
    public boolean setPreferredTimeZone(String timeZone) {
        if (isValidTimeZoneId(timeZone)) {
            preferenceService.setPreference(userBean.getCurrentAccount(),
                    TIMEZONE_PREFERENCE_KEY, timeZone);
            pref = timeZone;
            return true;
        } else {
            logger.warn("Could not set the time zone '" + timeZone
                    + "' as preferred time zone.");
            return false;
        }
    }

    private boolean isValidTimeZoneId(String zoneId) {
        try {
            ZoneId.of(zoneId);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
