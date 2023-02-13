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
package de.ipb_halle.lbac.timezone;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Wrapper class for time zones that supplies functionalities for displaying and
 * i18ning them.
 * 
 * @author flange
 */
public class ZoneIdDisplayWrapper {
    private final ZoneId zoneId;

    /**
     * Wrapper constructor.
     * 
     * @param zoneId time-zone ID
     * @throws NullPointerException if {@code zoneId} is {@code null}
     */
    public ZoneIdDisplayWrapper(ZoneId zoneId) {
        if (zoneId == null) {
            throw new NullPointerException();
        }
        this.zoneId = zoneId;
    }

    /**
     * Returns id of wrapped time zone object.
     * 
     * @return id
     */
    public String getId() {
        return zoneId.getId();
    }

    /**
     * Returns the internationalized name of the wrapped time zone.
     * 
     * @param locale the locale to use
     * @return textual representation of the zone
     */
    public String getDisplayName(Locale locale) {
        return zoneId.getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * Returns the offset object of the wrapped time zone.
     * 
     * @return offset
     */
    public ZoneOffset getOffset() {
        /*
         * code snippet from
         * https://mkyong.com/java8/java-display-all-zoneid-and-its-utc-offset/
         */
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        return zonedDateTime.getOffset();
    }

    /**
     * Returns the offset of the wrapped time zone as String, e.g. "-02:00".
     * 
     * @return offset string
     */
    public String getOffsetString() {
        // replace "Z" with "+00:00"
        return getOffset().getId().replaceAll("Z", "+00:00");
    }
}
