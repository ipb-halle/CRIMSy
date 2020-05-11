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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import de.ipb_halle.lbac.material.subtype.taxonomy.TaxonomyLevel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class TaxonomyLevelController {

    protected TaxonomyBean taxonomyBean;
    protected List<TaxonomyLevel> levels;
    private TaxonomyLevel selectedLevel;

    public TaxonomyLevelController(TaxonomyBean taxonomyBean) {
        this.taxonomyBean = taxonomyBean;
    }

    public List<TaxonomyLevel> getLevels() {

        if (taxonomyBean.getSelectedTaxonomy() != null) {
            List<TaxonomyLevel> valideLevels = new ArrayList<>();
            Taxonomy t;
            if (taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
                t = taxonomyBean.getTaxonomyToEdit().getTaxHierachy().get(0);
            } else {
                t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            }
            for (TaxonomyLevel l : levels) {
                if (l.getRank() > t.getLevel().getRank()) {
                    valideLevels.add(l);
                }
            }
            return valideLevels;
        } else {
            return levels;
        }
    }

    public TaxonomyLevel getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(TaxonomyLevel selectedLevel) {
        this.selectedLevel = selectedLevel;
    }

    public void setLevels(List<TaxonomyLevel> levels) {
        this.levels = levels;
    }

}
