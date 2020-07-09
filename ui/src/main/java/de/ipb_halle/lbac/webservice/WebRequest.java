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
package de.ipb_halle.lbac.webservice;

import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import java.io.Serializable;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author fmauz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class WebRequest implements Serializable{

    protected User user;
    protected WebRequestSignature signature;
    protected UUID nodeIdOfRequest;
    protected String cloudName;
    private String statusCode = "200:OK";

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public WebRequestSignature getSignature() {
        return signature;
    }

    public void setSignature(WebRequestSignature signature) {
        this.signature = signature;
    }

    public String getCloudName() {
        return this.cloudName;
    }

    public void setCloudName(String n) {
        this.cloudName = n;
    }

    public UUID getNodeIdOfRequest() {
        return nodeIdOfRequest;
    }

    public void setNodeIdOfRequest(UUID nodeIdOfRequest) {
        this.nodeIdOfRequest = nodeIdOfRequest;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String errorCode) {
        this.statusCode = errorCode;
    }

}
