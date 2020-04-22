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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.entity.ACList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class MaterialIndex extends MaterialDetail {

    private List<String[]> indices = new ArrayList<>();

    public MaterialIndex(String name, ACList acList) {
        super(name, acList);
        type = MaterialDetailType.INDEX;
    }

    public MaterialIndex addIndex(String indexName, String indexValue) {
        indices.add(new String[]{indexName, indexValue});
        return this;
    }

    public List<String[]> getIndices() {
        return indices;
    }

    public void setIndices(List<String[]> indices) {
        this.indices = indices;
    }

}
