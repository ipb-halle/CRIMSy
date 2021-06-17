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
package de.ipb_halle.lbac.admission;

import java.io.Serializable;

public class ResourcePermission implements Serializable {

    private final static long serialVersionUID = 1L;

    private String              resource;
    private ACPermission        permission;

    public ResourcePermission(String r, ACPermission p) {
        this.resource = r;
        this.permission = p;
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) 
          && o.getClass().equals(this.getClass())) {
            ResourcePermission rp = (ResourcePermission) o;
            return rp.resource.equals(this.resource) 
              && (rp.permission == this.permission);
        }
        return false;
    }

    @Override 
    public int hashCode() {
        return this.resource.hashCode() + permission.hashCode();
    }

}

