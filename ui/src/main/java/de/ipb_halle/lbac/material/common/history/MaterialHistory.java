/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.common.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of all edits of the material.
 *
 * @author fmauz
 */
public class MaterialHistory  implements Serializable{

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private final SortedMap<Date, List<MaterialDifference>> changes = new TreeMap<>();

    /**
     * Adds an difference to the material and puts it chronologically at the
     * right position. if there is already an difference at this timepoint the
     * difference will be merged with the existing differences.
     *
     * @param d difference to add
     */
    public void addDifference(MaterialDifference d) {
        Date mdate = d.getModificationDate();
        List<MaterialDifference> diffs;
        if (changes.get(mdate) == null) {
            diffs = new ArrayList<>();
            changes.put(mdate, diffs);
        } else {
            diffs = changes.get(mdate);
        }

        diffs.add(d);
    }

    /**
     * Returns
     *
     * @param d
     * @return
     */
    public Date getPreviousKey(Date d) {
        if (changes.isEmpty() || !changes.containsKey(d)) {
            return null;
        }
        if (changes.firstKey().equals(d)) {
            return null;
        }

        return changes.headMap(d).lastKey();
    }

    public Date getFollowingKey(Date d) {
        if (d == null) {
            return changes.firstKey();
        }

        if (changes.isEmpty() || !changes.containsKey(d)) {
            return null;
        }

        if (changes.lastKey().equals(d)) {
            return null;
        }

        ArrayList<Date> dates = new ArrayList<>(changes.keySet());

        int index = dates.indexOf(d);

        return dates.get(index + 1);
    }

    /**
     *
     * @param d
     * @return
     */
    public boolean isMostRecentVersion(Date d) {
        if (changes.isEmpty()) {
            return true;
        } else {
            return changes.lastKey().equals(d);
        }
    }

    public Date getMostFarVersion() {
        if (changes.isEmpty()) {
            return null;
        } else {
            return changes.firstKey();
        }
    }

    public SortedMap<Date, List<MaterialDifference>> getChanges() {
        return changes;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDifferenceOfTypeAtDate(Class T, Date d) {
        if (changes.get(d) == null) {
            return null;
        }
        for (MaterialDifference sd : changes.get(d)) {
            if (sd.getClass().equals(T)) {
                return (T) sd;
            }
        }
        return null;
    }

}
