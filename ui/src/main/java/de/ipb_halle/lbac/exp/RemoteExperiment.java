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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class RemoteExperiment implements Searchable {

    private int id;
    private String code;
    private String description;
    private int projectId;
    private User owner;
    private Date creationTime;

    public RemoteExperiment() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean isEqualTo(Object other) {
        if (other instanceof RemoteExperiment) {
            RemoteExperiment otherExp = (RemoteExperiment) other;
            return otherExp.getId() == id;
        }
        return false;
    }

    @Override
    public String getNameToDisplay() {
        return code;
    }

    @Override
    public Type getTypeToDisplay() {
        return new Type(SearchTarget.EXPERIMENT);
    }

}
