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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.entity.ACList;
import java.io.Serializable;

/**
 *
 * @author fmauz
 */
public class MaterialDetail  implements Serializable{

    protected ACList acList;
    protected String name;
    protected MaterialDetailType type;
    protected int materialID;

    public MaterialDetail(String name, ACList acList) {
        this.acList = acList;
        this.name = name;
    }

}
