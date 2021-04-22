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
package de.ipb_halle.lbac.util.jsf;

import java.util.Date;

import org.omnifaces.el.functions.Dates;
import org.omnifaces.util.Beans;

import de.ipb_halle.lbac.admission.UserBean;

/**
 * EL functions for dates.
 * 
 * @author flange
 */
public final class DateFunctions {
    protected static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    private DateFunctions() {
    }

    /**
     * Formats a {@link Date} using the preferred time zone of the client.
     * 
     * @param date date to be formatted
     * @return formatted date or {@code null} if param {@code date} is {@code null}
     */
    public static String formatDateWithTimezone(Date date) {
        if (date == null) {
            return null;
        }

        // get UserBean programmatically
        UserBean userBean = Beans.getReference(UserBean.class);
        if (userBean == null) {
            // fallback: return formatted date in the time zone of the server
            return Dates.formatDate(date, DATE_FORMAT);
        }

        String timeZone = userBean.getTimeZoneSettings().getPreferredTimeZone();
        return Dates.formatDateWithTimezone(date, DATE_FORMAT, timeZone);
    }
}