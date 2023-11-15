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
 * Node This class represents a single node in the Bioactives Cloud. A node is
 * described by its id, baseUrl, institution. Additionally it may be either
 * local or remote. (Serialized) node objects can be queried from the master
 * node to get information about all existing nodes.
 */
import de.ipb_halle.crimsy_api.DTO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Node implements Serializable, DTO {

    private final static long serialVersionUID = 1L;

    private UUID id;

    private String baseUrl;

    private String institution;

    private Boolean local;

    private Boolean publicNode;

    private String version;

    /**
     * default constructor
     */
    public Node() {
        // especially necessary for test cases
        id = UUID.randomUUID();
        version = "00005";
        publicNode = false;
    }

    /**
     * Constructor
     *
     * @param entity
     */
    public Node(NodeEntity entity) {
        this.baseUrl = entity.getBaseUrl();
        this.id = entity.getId();
        this.institution = entity.getInstitution();
        this.local = entity.getLocal();
        this.publicNode = entity.getPublicNode();
        this.version = entity.getVersion();
    }

    @Override
    public NodeEntity createEntity() {
        return new NodeEntity()
                .setBaseUrl(baseUrl)
                .setId(id)
                .setVersion(version)
                .setInstitution(institution)
                .setLocal(local)
                .setPublicNode(publicNode);
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof Node)) {
            Node n = (Node) o;
            if (this.id.equals(n.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return base url for communicating with this node
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * @return node id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * @return institution
     */
    public String getInstitution() {
        return this.institution;
    }

    /**
     * @return true, if this Node object represents the local node.
     */
    public Boolean getLocal() {
        return this.local;
    }

    /**
     * @return true if node is the public node
     */
    public Boolean getPublicNode() {
        return this.publicNode;
    }

    /**
     * @return the software / database schema version of this node
     */
    public String getVersion() {
        return this.version;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    /**
     * @param n the node id (usually an uuid)
     */
    public void setId(UUID n) {
        this.id = n;
    }

    /**
     * @param n the base URL of this node
     */
    public void setBaseUrl(String n) {
        this.baseUrl = n;
    }

    /**
     * @param n the institution name
     */
    public void setInstitution(String n) {
        this.institution = n;
    }

    /**
     * @param b true, if this node represents the local node instance
     */
    public void setLocal(boolean b) {
        this.local = b;
    }

    /**
     * @param b true if node is the public node
     */
    public void setPublicNode(boolean b) {
        this.publicNode = b;
    }

    /**
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return string representation of this node
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node {");
        sb.append("id=");
        sb.append(this.id);
        sb.append(", baseURL=");
        sb.append(this.baseUrl);
        sb.append(this.local ? "local" : "remote");
        sb.append("}");
        return sb.toString();
    }

}
