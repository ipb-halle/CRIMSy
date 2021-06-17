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
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a Cloud - Node association. A single node may be member in one or
 * more clouds. Each node can only see and access the nodes of those clouds, it
 * is member of.
 *
 * @author fbroda
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CloudNode implements Serializable, DTO {

    private final static long serialVersionUID = 1L;

    private final static long FAILURE_GRACE = 1000 * 30;
    private final static int FAILURES_MAX = 17;

    private Long id;

    private Integer rank;

    /**
     * Public rsa key in Base64 representation
     */
    private String publicKey = "";

    private int failures;

    private long retrytime;

    private Cloud cloud;

    private Node node;

    /**
     * Default constructor
     */
    public CloudNode() {
        this(null, null);
    }

    public CloudNode(Cloud c, Node n) {
        this.cloud = c;
        this.node = n;
        this.rank = Integer.valueOf(1);
        this.retrytime = 0;
        this.failures = 0;
    }

    /**
     * Constructor by entity, node and cloud
     *
     * @param entity
     * @param n
     * @param c
     */
    public CloudNode(CloudNodeEntity entity, Cloud c, Node n) {
        this.cloud = c;
        this.node = n;
        this.failures = entity.getFailures();
        this.id = entity.getId();
        this.publicKey = entity.getPublicKey();
        this.rank = entity.getRank();
        this.retrytime = entity.getRetryTime();

    }

    @Override
    public CloudNodeEntity createEntity() {
        return new CloudNodeEntity()
                .setCloud(cloud.getId())
                .setFailures(failures)
                .setId(id)
                .setNode(node.getId())
                .setPublicKey(publicKey)
                .setRank(rank)
                .setRetryTime(retrytime);
    }

    public void fail() {
        long millis = new Date().getTime();
        if (retrytime < millis) {
            if (failures == 0) {
                failures = 1;
            } else {
                if (failures < FAILURES_MAX) {
                    failures++;
                }
            }
            retrytime = millis + (1000 * (1 << failures)) - FAILURE_GRACE;
        }
    }

    public Long getId() {
        return this.id;
    }

    public Cloud getCloud() {
        return this.cloud;
    }

    public int getFailures() {
        return this.failures;
    }

    public Node getNode() {
        return this.node;
    }

    /**
     *
     * @return public rsa key for node
     */
    public String getPublicKey() {
        return publicKey;
    }

    public Integer getRank() {
        return this.rank;
    }

    public long getRetryTime() {
        return this.retrytime;
    }

    public String getStatus() {
        if (failures == 0) {
            return "CloudNodeStatusOk";
        } else {
            long t = new Date().getTime() + FAILURE_GRACE;
            if (retrytime < t) {
                return "CloudNodeStatusWaiting";
            }
        }
        return "CloudNodeStatusFailed";
    }

    public void recover() {
        this.failures = 0;
        this.retrytime = 0;
    }

    public void setId(Long i) {
        this.id = i;
    }

    public void setCloud(Cloud c) {
        this.cloud = c;
    }

    public void setFailures(int i) {
        this.failures = i;
    }

    public void setNode(Node n) {
        this.node = n;
    }

    /**
     *
     * @param publicKey public rsa key for node
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setRank(Integer i) {
        this.rank = i;
    }

    public void setRetryTime(long t) {
        this.retrytime = t;
    }

}
