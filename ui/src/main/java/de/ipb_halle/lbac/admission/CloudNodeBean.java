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

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.service.CloudNodeService;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Named("cloudNodeBean")
@SessionScoped
public class CloudNodeBean implements Serializable {

    /**
     * This bean displays and manages known (cloud)nodes.
     *
     */
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private final static Long serialVersionUID = 1L;

    @Inject
    private CloudNodeService cloudNodeService;

    private transient Logger logger;

    /**
     * default constructor
     */
    public CloudNodeBean() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * do nothing action
     */
    public void actionDoNothing(CloudNode cn) {
        // reserved for cool stuff coming ...
    }

    /**
     * return a list of CloudNodes
     *
     */
    public List<CloudNode> getCloudNodes() {
        return this.cloudNodeService.load(null, null, null);
    }
}
