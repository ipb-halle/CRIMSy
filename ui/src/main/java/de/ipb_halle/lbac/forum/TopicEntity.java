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
package de.ipb_halle.lbac.forum;

import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.entity.ACObjectEntity;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "topics")
public class TopicEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Column(name = "cloud_name")
    private String cloudName;

    @Column
    @Size(min = 0, max = 255)
    private String name;

    @Column
    @Enumerated(EnumType.STRING)
    private TopicCategory category;

    @Id
    private UUID id;

    @Column(name = "node_id")
    private UUID node;

    public TopicEntity() {
    }

    public TopicCategory getCategory() {
        return category;
    }

    public String getCloudName() {
        return cloudName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getNode() {
        return node;
    }

    public TopicEntity setCategory(TopicCategory category) {
        this.category = category;
        return this;
    }

    public TopicEntity setCloudName(String cloudName) {
        this.cloudName = cloudName;
        return this;
    }

    public TopicEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public TopicEntity setName(String name) {
        this.name = name;
        return this;
    }

    public TopicEntity setNode(UUID node) {
        this.node = node;
        return this;
    }

}
