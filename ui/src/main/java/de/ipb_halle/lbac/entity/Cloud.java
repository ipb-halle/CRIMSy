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

import javax.xml.bind.annotation.*;

/**
 * Represents a Cloud (i.e. a group of nodes)
 *
 * @author fbroda
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Cloud implements Serializable, DTO {

    private final static long serialVersionUID = 1L;

    private Long id;

    private String name;

    /* default constructor */
    public Cloud() {

    }

    public Cloud(String n) {
        this.name = n;
    }

    public Cloud(CloudEntity entity) {
        this.name = entity.getName();
        this.id = entity.getId();
    }

    @Override
    public CloudEntity createEntity() {
        CloudEntity entity=new CloudEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setId(Long i) {
        this.id = i;
    }

    public void setName(String n) {
        this.name = n;
    }

}
