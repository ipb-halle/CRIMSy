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

import java.io.Serializable;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginEvent implements Serializable {

    private User currentAccount;
    private transient Logger logger;
    public static long startTime;

    /**
     * constructor
     */
    public LoginEvent(User u) {
        this.currentAccount = u;
//        this.logger = LogManager.getLogger(this.getClass().getName());
//        startTime = new Date().getTime();
    }

    /**
     * @return the User object associated with this event
     */
    public User getCurrentAccount() {

//        try {
//            throw new Exception("xxx");
//        } catch (Exception e) {
//            StackTraceElement trace[] = e.getStackTrace();
////            logger.info(String.format("Time: %d Class: %s", 
////                    new Date().getTime() - startTime,
////                    trace[1].getClassName()));
//        }

        return this.currentAccount;
    }
}
