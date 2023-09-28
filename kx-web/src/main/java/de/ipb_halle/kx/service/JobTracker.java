/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.service;

import de.ipb_halle.kx.service.TextWebStatus;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Provisional class for job tracking until Job API gets refactored.
 */
@Singleton
@Startup
public class JobTracker {

    private Map<Integer, FileAnalyser> jobMap = new HashMap<> ();

    public FileAnalyser getJob(Integer id) {
        return jobMap.get(id);
    }

    public void pubJob(Integer id, FileAnalyser job) {
        jobMap.put(id, job);
    }

    public void remove(Integer id) {
        jobMap.remove(id);
    }
}
