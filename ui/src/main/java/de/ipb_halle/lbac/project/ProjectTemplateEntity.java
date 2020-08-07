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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.message.LocalUUIDConverter;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.johnzon.mapper.JohnzonConverter;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "projectTemplates")
public class ProjectTemplateEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    public ProjectTemplateEntity(int materialTypeId, UUID acListId, int projectId) {
        this.materialDetailTypeId = materialTypeId;
        this.acListId = acListId;
        this.projectId = projectId;
    }

    public ProjectTemplateEntity() {
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private int materialDetailTypeId;

    @JohnzonConverter(LocalUUIDConverter.class)
    @Column
    private UUID acListId;

    @Column
    private int projectId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getAcListId() {
        return acListId;
    }

    public void setAcListId(UUID acListId) {
        this.acListId = acListId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getMaterialDetailTypeId() {
        return materialDetailTypeId;
    }

    public void setMaterialDetailTypeId(int materialDetailTypeId) {
        this.materialDetailTypeId = materialDetailTypeId;
    }

}
