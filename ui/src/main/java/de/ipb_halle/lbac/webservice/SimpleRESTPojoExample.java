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

import de.ipb_halle.lbac.globals.GlobalVersions;
import de.ipb_halle.lbac.globals.KeyStoreFactory;

import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * This is a test class for the REST api. It provides a
 * single method which will return a string containing
 * the hostname, build and version information and the date. 
 */
@Path("/pojo")
@Stateless
public class SimpleRESTPojoExample {

    @Inject
    private GlobalVersions versions;

    @GET
    public String pojo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nWebService   : pojo test service\n");
        sb.append("Host         : ");
        sb.append(KeyStoreFactory.getInstance().getLOCAL_KEY_ALIAS());
        sb.append("\n");
        sb.append("BuildNumber  : ");
        sb.append(versions.getBuildNumber());
        sb.append("\n");
        sb.append("VersionNumber: ");
        sb.append(versions.getVersionNumber());
        sb.append("\n");
        sb.append("Date         : ");
        sb.append(new Date().toString());
        sb.append("\n");
        return sb.toString();
    }
}


