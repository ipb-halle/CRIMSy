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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
public class TaxonomyLevelController implements Serializable {

    protected TaxonomyBean taxonomyBean;
    protected List<TaxonomyLevel> levels;
    private TaxonomyLevel selectedLevel;

    public TaxonomyLevelController(TaxonomyBean taxonomyBean) {
        this.taxonomyBean = taxonomyBean;
    }

    public List<TaxonomyLevel> getPossibleLevels() {
        if (taxonomyBean.getSelectedTaxonomy() == null) {
            return levels;
        }

        List<TaxonomyLevel> valideLevels = new ArrayList<>();

        Taxonomy currentTaxo = getTaxonomyByMode();

        for (TaxonomyLevel level : levels) {
            if (checkIfTaxoLevelIsValide(level, currentTaxo, valideLevels)) {
                valideLevels.add(level);
            }
        }
        return valideLevels;
    }

    public TaxonomyLevel getSelectedLevel() {
        return selectedLevel;
    }

    public void setSelectedLevel(TaxonomyLevel selectedLevel) {
        this.selectedLevel = selectedLevel;
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

    public TaxonomyLevel getRootLevel() {
        return levels.get(0);
    }

    private boolean checkIfTaxoLevelIsValide(TaxonomyLevel level, Taxonomy currentTaxo, List<TaxonomyLevel> valideLevels) {
        return (level.getRank() > currentTaxo.getLevel().getRank()
                && level.getRank() < getHighestPossibleRank());
    }

    private Taxonomy getTaxonomyByMode() {
        Taxonomy t;
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
            t = taxonomyBean.getTaxonomyToEdit().getTaxHierarchy().get(0);
        } else {
            t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        }
        return t;
    }

    private int getHighestPossibleRank() {
        int highestLevel = Integer.MAX_VALUE;
        if (taxonomyBean.getSelectedTaxonomy() == null || taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE) {
            return highestLevel;
        }
        for (Object b : taxonomyBean.getSelectedTaxonomy().getChildren()) {
            Taxonomy t = (Taxonomy) ((TreeNode) b).getData();
            highestLevel = Math.min(highestLevel, t.getLevel().getRank());
        }
        return highestLevel;
    }
}
