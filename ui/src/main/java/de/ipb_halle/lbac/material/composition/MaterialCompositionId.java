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
package de.ipb_halle.lbac.material.composition;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class MaterialCompositionId implements Serializable {

    private static final long serialVersionUID = 1L;

    private int materialid;
    private int componentid;

    public MaterialCompositionId() {
    }

    public MaterialCompositionId(int materialid, int componentid) {
        this.materialid = materialid;
        this.componentid = componentid;
    }

    public int getMaterialid() {
        return materialid;
    }

    public void setMaterialid(int materialid) {
        this.materialid = materialid;
    }

    public int getComponentid() {
        return componentid;
    }

    public void setComponentid(int componentid) {
        this.componentid = componentid;
    }

}
