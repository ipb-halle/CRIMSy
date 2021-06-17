/*
 * Text eXtractor
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
package de.ipb_halle.tx.text;

import de.ipb_halle.tx.text.properties.TextProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class TextRecord implements Cloneable {

    private String                                  text;
    private Map<String, SortedSet<TextProperty>>    properties;

    private enum ADJ_FETCH { FETCH_PROP, FETCH_ADJ, FETCH_BOTH, FINISHED };

    /**
     * default constructor
     */
    public TextRecord() {
        this("", new HashMap<String, SortedSet<TextProperty>> ());
    }

    /**
     * constructor
     */
    public TextRecord(String st) {
        this(st, new HashMap<String, SortedSet<TextProperty>> ());
    }

    /**
     * constructor
     */
    public TextRecord(String st, Map<String, SortedSet<TextProperty>> props) {
        this.text = st;
        this.properties = props;
    }


    /**
     * add a property to this record
     */
    public TextRecord addProperty(TextProperty prop) {
        String type = prop.getType();
        SortedSet<TextProperty> set = this.properties.get(type);
        if (set == null) {
            set = new TreeSet<TextProperty> ();
            this.properties.put(type, set);
        }
        set.add(prop);
        return this;
    }

    /**
     * Apply an adjustment to a single property if property and
     * adjustment overlap. The overlap condition is met if either
     * start or end or both of the text property fall into the
     * range of the adjustment (after applying accumulated deltas).
     * If the overlap condition is not met, the method requests
     * either the next property or the next adjustment to be fetched.
     * @param prop a text property
     * @param current adjustment currently worked on
     * @param preview of the next adjustment
     * @return fetch condition
     */
    private ADJ_FETCH adjust(TextProperty prop, Adjustment current, Adjustment next) {

        if (prop == null) {
            throw new IllegalArgumentException ("prop is null");
        }
        int propStart = prop.getStart();
        propStart -= current.getAccumulatedDelta();
        int propEnd = prop.getEnd() - current.getAccumulatedDelta();
        int startDelta = 0;
        int endDelta = 0;

        if ((next != null) && (propStart >= next.getStart())) {
            return ADJ_FETCH.FETCH_ADJ;
        }

        if (propStart > current.getStart()) {
            if (propStart < current.getEnd()) {
                startDelta = current.getStart() - propStart;
            } else {
                startDelta = current.getDelta();
            }
        } 
        if (propEnd > current.getStart()) {
            if (propEnd < current.getEnd()) {
                endDelta = Integer.min(0, (current.getEnd() - current.getDelta() - propEnd)); 
            } else {
                endDelta = current.getDelta();
            }
        }
        if ((startDelta != 0) || (endDelta != 0)) {
            prop.adjust(startDelta, endDelta);
        }
        return ADJ_FETCH.FETCH_PROP;
    }


    /**
     * adjust all offsets which are affected by changes
     * in the range between start and end.
     * @param adjustments a list of adjustments to be applied
     */
    public void adjustAll(List<Adjustment> adjustments) {

        // do it separately for each set of properties
        for(SortedSet<TextProperty> set : this.properties.values()) {

            ListIterator<Adjustment> adjIter = adjustments.listIterator();
            Iterator<TextProperty> propIter = set.iterator();
            ADJ_FETCH state = ADJ_FETCH.FETCH_BOTH;
            Adjustment current = new Adjustment(0, 0, 0, 0);
            Adjustment next = null;
            TextProperty prop = null;

            while (state != ADJ_FETCH.FINISHED) {
                switch(state) {
                    case FETCH_ADJ :
                        if (adjIter.hasNext()) {
                            current = next;
                            next = adjIter.next();
                        } else {
                            next = null;
                        }
                        break;
                    case FETCH_PROP :
                        if (propIter.hasNext()) {
                            prop = propIter.next();
                        } else {
                            state = ADJ_FETCH.FINISHED;
                        }
                        break;
                    case FETCH_BOTH :
                        // initial round only
                        if (adjIter.hasNext() && propIter.hasNext()) {
                            next = adjIter.next();
                            prop = propIter.next();
                        } else {
                            state = ADJ_FETCH.FINISHED;
                        }
                }
                if (state != ADJ_FETCH.FINISHED) {
                    state = adjust(prop, current, next);
                }
            }
        }
    }

    /**
     * Appends the text and the properties of another TextRecord 
     * to this record. The properties will be adjusted.
     * @param rec the other TextRecord
     */
    public TextRecord append(TextRecord rec) {
        int len = this.text.length();
        this.text += rec.getText();
        for(SortedSet<TextProperty> set : rec.getAllProperties().values()) {
            for(TextProperty prop : set) {
                addProperty(prop.adjust(len));
            }
        }
        return this;
    }

    /**
     * Return a clone of this object.
     * This method does not call <code>super.clone()</code> as most work 
     * lies in the deep copying of the properties map which is not done 
     * by clone() method.
     */
    public Object clone() throws CloneNotSupportedException {
        Map<String, SortedSet<TextProperty>> map = new HashMap<String, SortedSet<TextProperty>> ();
        for(Map.Entry<String, SortedSet<TextProperty>> entry : this.properties.entrySet()) {
            SortedSet<TextProperty> set = new TreeSet<TextProperty> ();
            map.put(entry.getKey(), set);
            for(TextProperty prop : (SortedSet<TextProperty>) entry.getValue()) {
                set.add((TextProperty) prop.clone());
            }
        }
        return new TextRecord(this.text, map);
    }

    /**
     * return the length of the text
     */
    public int length() { 
        return this.text.length();
    }

    public Map<String, SortedSet<TextProperty>> getAllProperties() {
        return this.properties;
    }

    public SortedSet<TextProperty> getProperties(String type) {
        SortedSet<TextProperty> set = this.properties.get(type);
        if (set == null) {
            set = new TreeSet<TextProperty> ();
        }
        return set;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String st) {
        this.text = st;
    }
}
