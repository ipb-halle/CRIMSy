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

import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;

/**
 * Node This class represents a single node in the Bioactives Cloud. A node is
 * described by its id, baseUrl, institution. Additionally it may be either
 * local or remote. (Serialized) node objects can be queried from the master
 * node to get information about all existing nodes.
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "nodes")
public class NodeEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column
    @NotNull
    private String baseUrl;

    @Column
    @NotNull
    @AttributeTag(type=AttributeType.INSTITUTION)
    private String institution;

    @Column
    @NotNull
    private Boolean local;

    @Column
    @NotNull
    private Boolean publicNode;

    @Column
    private String version;

    /**
     * @return node id
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * @return base url for communicating with this node
     */
    public String getBaseUrl() {
        return this.baseUrl;
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

    /**
     * @param n the node id (usually an uuid)
     * @return
     */
    public NodeEntity setId(UUID n) {
        this.id = n;
        return this;
    }

    /**
     * @param n the base URL of this node
     * @return
     */
    public NodeEntity setBaseUrl(String n) {
        this.baseUrl = n;
        return this;
    }

    /**
     * @param n the institution name
     * @return
     */
    public NodeEntity setInstitution(String n) {
        this.institution = n;
        return this;
    }

    /**
     * @param b true, if this node represents the local node instance
     * @return
     */
    public NodeEntity setLocal(boolean b) {
        this.local = b;
        return this;
    }

    /**
     * @param b true if node is the public node
     * @return
     */
    public NodeEntity setPublicNode(boolean b) {
        this.publicNode = b;
        return this;
    }

    public NodeEntity setVersion(String version) {
        this.version = version;
        return this;
    }
    
    

}
