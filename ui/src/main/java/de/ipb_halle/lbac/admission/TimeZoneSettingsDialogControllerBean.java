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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.validator.constraints.NotBlank;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.timezone.TimeZonesBean;
import de.ipb_halle.lbac.timezone.ZoneIdDisplayWrapper;

/**
 * This JSF backing bean controls the form of the time zone user settings.
 * <p>
 * View file: WEB-INF/templates/accountSettings/timeZoneTab.xhtml
 *
 * @author flange
 */
@ViewScoped
@Named
public class TimeZoneSettingsDialogControllerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private UserBean userBean;

    @Inject
    private TimeZonesBean timeZonesBean;

    @NotBlank
    private String timeZone;

    private Locale clientLocale;

    private MessagePresenter messagePresenter;

    /**
     * default constructor
     */
    public TimeZoneSettingsDialogControllerBean() {
    }

    /**
     * Initializes the bean state:
     * <ul>
     * <li>initialize the message presenter for i18n</li>
     * <li>set the time zone from its preferred value</li>
     * <li>initialize the locale</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        messagePresenter = JsfMessagePresenter.getInstance();
        timeZone = userBean.getTimeZoneSettings().getPreferredTimeZone();
        clientLocale = FacesContext.getCurrentInstance().getViewRoot()
                .getLocale();
    }

    /**
     * Returns the selected time zone.
     * 
     * @return time zone id
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the selected time zone.
     * 
     * @param timeZone time zone
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /**
     * Returns the current time of the application server in the time zone
     * selected by the client.
     * 
     * @return server time
     */
    public String getServerTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT,
                clientLocale);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormatter.format(new Date());
    }

    /**
     * Returns all available time zones.
     * 
     * @return list of time zones
     */
    public List<ZoneIdDisplayWrapper> getAvailableTimeZones() {
        return timeZonesBean.getAvailableTimeZones();
    }

    /**
     * Returns a localized string to be displayed for the given time zone
     * wrapper object.
     * 
     * @param zone time zone wrapper
     * @return localized string
     */
    public String getDisplayForZone(ZoneIdDisplayWrapper zone) {
        return String.format("(%s) %s - %s", zone.getOffsetString(),
                zone.getId(), zone.getDisplayName(clientLocale));
    }

    /**
     * Saves the selected time zone as preferred time zone and notifies to user
     * upon success or failure.
     */
    public void actionSave() {
        User currentUser = userBean.getCurrentAccount();

        if (!currentUser.isPublicAccount()) {
            boolean result = userBean.getTimeZoneSettings()
                    .setPreferredTimeZone(timeZone);

            if (result) {
                messagePresenter.info("admission_timeZone_updated");
            } else {
                messagePresenter.error("admission_timeZone_not_updated");
            }
        }
    }
}