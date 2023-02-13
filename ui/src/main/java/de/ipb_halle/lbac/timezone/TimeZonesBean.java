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

import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 * This bean offers common time-zone-related data.
 * 
 * @author flange
 */
@Named
@ApplicationScoped
public class TimeZonesBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ZoneIdDisplayWrapper> availableTimeZones;

    private String defaultTimeZone = ZoneId.of("Europe/Berlin").getId();

    /**
     * Initializes the bean state.
     */
    @PostConstruct
    public void init() {
        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        List<ZoneIdDisplayWrapper> zones = new ArrayList<>(zoneIds.size());

        for (String zone : ZoneId.getAvailableZoneIds()) {
            zones.add(new ZoneIdDisplayWrapper(ZoneId.of(zone)));
        }

        // sort list: (1) by offset to UTC, (2) by name of time zone
        zones.sort(Comparator.comparing(ZoneIdDisplayWrapper::getOffset)
                .reversed().thenComparing(ZoneIdDisplayWrapper::getId));

        availableTimeZones = Collections.unmodifiableList(zones);
    }

    /**
     * Returns all available time zones.
     * 
     * @return unmodifiable list of time zones, which is sorted by the TZ offset
     *         and the TZ name
     */
    public List<ZoneIdDisplayWrapper> getAvailableTimeZones() {
        return availableTimeZones;
    }

    /**
     * Returns the default time zone.
     * 
     * @return valid time zone id according to {@link ZoneId#getId()}
     */
    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }
}
