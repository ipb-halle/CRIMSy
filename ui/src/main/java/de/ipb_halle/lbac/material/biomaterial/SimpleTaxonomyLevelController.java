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
package de.ipb_halle.lbac.material.biomaterial;

import java.util.List;

/**
 *
 * @author fmauz
 */
public class SimpleTaxonomyLevelController extends TaxonomyLevelController {

    public SimpleTaxonomyLevelController(List<TaxonomyLevel> levels) {
        super(null);
        this.levels = levels;
    }

    @Override
    public List<TaxonomyLevel> getLevels() {
        return levels;
    }

    public int getLeastRank() {
        return levels.get(levels.size() - 1).getRank();
    }

    public int getHighestRank() {
        return levels.get(0).getRank();
    }

    public void setLevels(List<TaxonomyLevel> levels) {
        this.levels = levels;
    }

}
