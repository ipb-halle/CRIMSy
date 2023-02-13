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
package de.ipb_halle.lbac.webservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timer;
import jakarta.ejb.Timeout;
import jakarta.ejb.TimerService;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Updater schedules queries to the master node for an updated list of nodes and
 * to the individual nodes for a list of collections. In later versions, this
 * class might also update users, groups and access permissions.
 * <p>
 * Updates are currently scheduled once per hour, this might be changed into
 * configurable intervals. The timer in TomEE obviously survives undeployment /
 * redeployment cycle - some means might be necessary to disable the timer in
 * case of undeployment.
 */
@Singleton
@Startup
public class Updater {

    private final static long initialDelay = 60 * 1000;                 // 60 seconds after start
    private final static long intervalDuration = 60 * 60 * 1000;        // every hour later on
    private final static String TIMER_NAME = "BackendUpdateTimer";

    private Timer timer;

    @Resource
    private TimerService timerService;

    private List<IUpdateWebClient> updateWebClients;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * initalize the update timer.
     * <p>
     * xxxxx ToDo: Timer configuration possibly should come from a properties
     * file (or be otherwise configurable).
     */
    @PostConstruct
    public void postConstruct() {
        this.updateWebClients = Collections.synchronizedList(new ArrayList<IUpdateWebClient>());
        this.timer = timerService.createTimer(initialDelay, intervalDuration, TIMER_NAME);

    }

    /**
     * register an IUpdateWebClient for scheduled execution
     *
     * @param uwc
     */
    public void register(IUpdateWebClient uwc) {
        this.updateWebClients.add(uwc);
    }

    /**
     * cancel timer upon undeployment
     */
    @PreDestroy
    public void stop() {
        this.timer.cancel();
    }

    /**
     * After the specified timeintervall all registered services will be
     * executed
     */
    @Timeout
    public void timeout() {
        synchronized (this.updateWebClients) {
            ListIterator<IUpdateWebClient> li = this.updateWebClients.listIterator();
            while (li.hasNext()) {
                li.next().update();
            }
        }
    }
}
