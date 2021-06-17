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

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Represents a Cloud - Node association. A single node may be member in one or
 * more clouds. Each node can only see and access the nodes of those clouds, it
 * is member of.
 *
 * @author fbroda
 */
@Entity
@Table(name = "cloud_nodes")
public class CloudNodeEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column
    @NotNull
    private Integer rank;

    /**
     * Public rsa key in Base64 representation
     */
    @Column
    @NotNull
    private String publicKey = "";

    @Column
    @NotNull
    private int failures;

    @Column
    private long retrytime;

    @Column(name = "cloud_id")
    private Long cloud;

    @Column(name = "node_id")
    private UUID node;

    public CloudNodeEntity() {

    }

    public Long getId() {
        return this.id;
    }

    public Long getCloud() {
        return this.cloud;
    }

    public int getFailures() {
        return this.failures;
    }

    public UUID getNode() {
        return this.node;
    }

    /**
     *
     * @return public rsa key for node
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     *
     * @return
     */
    public Integer getRank() {
        return this.rank;
    }

    /**
     *
     * @return
     */
    public long getRetryTime() {
        return this.retrytime;
    }

    /**
     *
     * @param i
     * @return
     */
    public CloudNodeEntity setId(Long i) {
        this.id = i;
        return this;
    }

    /**
     *
     * @param c
     * @return
     */
    public CloudNodeEntity setCloud(Long c) {
        this.cloud = c;
        return this;
    }

    /**
     *
     * @param i
     * @return
     */
    public CloudNodeEntity setFailures(int i) {
        this.failures = i;
        return this;
    }

    /**
     *
     * @param n
     * @return
     */
    public CloudNodeEntity setNode(UUID n) {
        this.node = n;
        return this;
    }

    /**
     *
     * @param publicKey public rsa key for node
     * @return
     */
    public CloudNodeEntity setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     *
     * @param i
     * @return
     */
    public CloudNodeEntity setRank(Integer i) {
        this.rank = i;
        return this;
    }

    /**
     *
     * @param t
     * @return
     */
    public CloudNodeEntity setRetryTime(long t) {
        this.retrytime = t;
        return this;
    }
}
