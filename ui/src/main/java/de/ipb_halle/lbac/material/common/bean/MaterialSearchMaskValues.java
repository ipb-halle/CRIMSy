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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.sequence.search.SequenceSearchInformation;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class MaterialSearchMaskValues {

    public String materialName;
    public Integer id;
    public String projectName;
    public String userName;
    public String index;
    public Set<MaterialType> type = new HashSet<>();
    public String molecule;
    public Boolean deactivated;
    public SequenceSearchInformation sequenceInfos;
}
