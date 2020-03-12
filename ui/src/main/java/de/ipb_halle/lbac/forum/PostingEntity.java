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
package de.ipb_halle.lbac.forum;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "postings")
public class PostingEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column(name = "topic_id")
    private UUID topic;

    @Column
    private String text;

    @Column(name = "owner_id")
    private UUID owner;

    @Column
    private Date created;

    public PostingEntity() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }

    public UUID getOwner() {
        return owner;
    }

    public Date getCreated() {
        return created;
    }

    public PostingEntity setCreated(Date created) {
        this.created = created;
        return this;
    }

    public PostingEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public PostingEntity setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public PostingEntity setText(String text) {
        this.text = text;
        return this;
    }

    public PostingEntity setTopic(UUID topic) {
        this.topic = topic;
        return this;
    }

}
