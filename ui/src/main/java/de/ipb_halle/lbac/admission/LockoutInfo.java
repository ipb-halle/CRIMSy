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
package de.ipb_halle.lbac.admission;

import java.util.Date;

public class LockoutInfo {

    // 20 minutes
    private final static long LOCKOUT_MILLIS = 1000 * 60 * 20;
    private final static int LOCKOUT_COUNT = 5;

    private long time; 
    private int count;

    /**
     * constructor
     */
    public LockoutInfo() {
        this.time = new Date().getTime();
        this.count = 1;
    }

    /**
     * check the lock status of this record
     * @return true if record is locked
     */
    public boolean check() {
        this.time = new Date().getTime();
        return (this.count > LOCKOUT_COUNT);
    }

    /**
     * check if a lock record is expired
     * @param t the time to check 
     * @param return true if is record is already expired
     */
    public boolean expired(long t) {
        return ((this.time + LOCKOUT_MILLIS) < t);
    }

    /**
     * increment the lock record
     */
    public void lock() {
        this.count++;
    }

    /**
     * unlock a record
     */
    public void unlock() {
        this.count = 0;
    }
}
