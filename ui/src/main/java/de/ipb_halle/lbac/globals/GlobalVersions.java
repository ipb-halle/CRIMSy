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
package de.ipb_halle.lbac.globals;

import java.io.IOException;
import java.io.Serializable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * holds the version numbers of used LBAC components
 */
@Named("versions")
@ApplicationScoped
public class GlobalVersions implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServletContext context;

    Logger logger;

    private Attributes     attributes;

    //*** getter and setter ***

    public Attributes getAttributes() {
        return attributes;
    }

    public String getBuildNumber() {
        return attributes.getValue("Implementation-Build");
    }

    public String getVersionNumber() {
        return attributes.getValue("Implementation-Version");
    }

    public String getGitSha1() {
        return attributes.getValue("git-sha-1");
    }

    public GlobalVersions() {
    }

    @PostConstruct
    public void GlobalVersionsInit(){
        try {
            logger = LogManager.getLogger(GlobalVersions.class);
            if (context != null) {
                Manifest mf = new Manifest();
                mf.read(context.getResourceAsStream("META-INF/MANIFEST.MF"));
                attributes = mf.getMainAttributes();
            } else {
                logger.warn("Inject servlet context failed.");
            }
        } catch (IOException e) {
        }
    }
}
