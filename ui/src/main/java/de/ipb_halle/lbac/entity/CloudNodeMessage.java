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
package de.ipb_halle.lbac.entity;

/**
 * Instances of this class are exchanged in the process of node announcement and 
 * discovery in communication with the master node. The class encapsules the name 
 * of the cloud and the local node when sent as a request and a list of nodes 
 * when received as a reply.
 */
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CloudNodeMessage implements Serializable {

    private final static long serialVersionUID = 1L;

    private String cloudName;

    private Node origin;

    private String publicKey;

    @XmlElementWrapper(name="cloudNodeList")
    @XmlElements(
            @XmlElement(name = "cloudNode", type = CloudNode.class))
    private List<CloudNode> cloudNodeList;

    /* empty default constructor */
    public CloudNodeMessage() {
    }

    /**
     * constructs the CloudNodeMessage as a request object
     * @param cn cloud name
     * @param n the node sending the request
     */
    public CloudNodeMessage(String cn, Node n, String pubkey) {
        this.cloudName = cn;
        this.origin = n;
        this.publicKey = pubkey;
    }


    /**
     * Constructs a CloudNodeMessage object as a reply
     * @param cnl the list of cloud member nodes
     */
    public CloudNodeMessage(List<CloudNode> cnl) {
        this.cloudNodeList = cnl;
    }

    public String getCloudName() {
        return this.cloudName;
    }

    public List<CloudNode> getCloudNodeList() {
        return this.cloudNodeList;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public Node getOrigin() {
        return this.origin;
    }

    public void setCloudName(String n) {
        this.cloudName = n;
    }

    public void setCloudNodeList(List<CloudNode> l) {
        this.cloudNodeList = l;
    }

    public void setOrigin(Node n) {
        this.origin = n;
    }

    public void setPublicKey(String pk) {
        this.publicKey = pk;
    }
}
