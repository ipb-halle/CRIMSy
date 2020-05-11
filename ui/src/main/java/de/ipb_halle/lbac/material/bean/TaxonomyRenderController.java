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

import de.ipb_halle.lbac.material.bean.TaxonomyBean.Mode;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;

/**
 *
 * @author fmauz
 */
public class TaxonomyRenderController {

    protected TaxonomyBean taxonomyBean;
    protected TaxonomyNameController nameController;
    TaxonomyLevelController levelController;

    public TaxonomyRenderController(
            TaxonomyBean taxonomyBean,
            TaxonomyNameController nameController,
            TaxonomyLevelController levelController) {
        this.taxonomyBean = taxonomyBean;
        this.nameController = nameController;
        this.levelController = levelController;
    }

    public boolean isFirstButtonDisabled() {

        if (taxonomyBean.getMode() == TaxonomyBean.Mode.SHOW && taxonomyBean.getSelectedTaxonomy() != null) {
            Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            return t.getLevel().getRank() == levelController.getHighestRank();

        }
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT) {
            return false;
        } else {
            return true;
        }
    }

    public String getEditButtonLabel() {
        if (taxonomyBean.getMode() == Mode.CREATE || taxonomyBean.getMode() == Mode.EDIT) {
            return "Cancel";
        } else {
            return "Edit";
        }
    }

    public boolean isMaterialNameOperationEnabled(
            MaterialName mn,
            String b) {
        return isNameEditable() && nameController.isEnabled(mn, b, nameController.getNames());
    }

    public boolean isNameEditable() {
        boolean isEditable = false;
        if (taxonomyBean.getMode() == Mode.EDIT) {
            isEditable = true;
        }
        if (taxonomyBean.getMode() == Mode.CREATE) {
            isEditable = true;
        }
        return isEditable;
    }

    public boolean isNamesVisible() {

        if (taxonomyBean.getMode() == Mode.CREATE) {
            return true;
        }
        if (taxonomyBean.getSelectedTaxonomy() == null) {
            return false;
        }
        return true;
    }

    public boolean isHistoryVisible() {
        boolean isVisible = false;

        if (taxonomyBean.getMode() == Mode.SHOW && taxonomyBean.getSelectedTaxonomy() != null) {
            isVisible = true;
        }
        if (taxonomyBean.getMode() == Mode.HISTORY) {
            isVisible = true;
        }
        return isVisible;
    }

    public String getCurrentAction() {
        if (taxonomyBean.getMode() == Mode.SHOW || taxonomyBean.getMode() == Mode.HISTORY) {
            if (taxonomyBean.getSelectedTaxonomy() != null) {
                Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
                return "Detail information for " + t.getFirstName();
            }
        }
        if (taxonomyBean.getMode() == Mode.CREATE) {
            return "Creating a new taxonomy entry";
        }
        if (taxonomyBean.getMode() == Mode.EDIT) {
            return "Editing taxonomy " + taxonomyBean.getTaxonomyToEdit().getFirstName();
        }
        return "";
    }

    public String getApplyButtonText() {
        if (taxonomyBean.getMode() == Mode.CREATE) {
            return "Save";
        }
        return "Create New Taxonomy";
    }

    public boolean isApplyButtonDisabled() {
        if (taxonomyBean.getMode() == Mode.HISTORY) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCategoryVisible() {
        boolean isVisible = false;
        if (taxonomyBean.getMode() == Mode.SHOW && taxonomyBean.getSelectedTaxonomy() != null) {
            isVisible = true;
        }
        return isVisible;
    }

    public boolean isCategorySelectionVisible() {
        boolean isVisible = false;
        if (taxonomyBean.getMode() == Mode.EDIT) {
            isVisible = true;
        }
        if (taxonomyBean.getMode() == Mode.CREATE && taxonomyBean.getSelectedTaxonomy() != null) {
            isVisible = true;
        }
        return isVisible;
    }

    public String getLabelForParentTaxonomy() {
        if (taxonomyBean.getParentOfNewTaxo() == null) {
            return "no parent choosen";
        } else {
            return taxonomyBean.getParentOfNewTaxo().getNames().get(0).getValue();
        }
    }

    public boolean isParentVisible() {
        if (taxonomyBean.getMode() == Mode.CREATE && taxonomyBean.getSelectedTaxonomy() != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getParentFirstName() {
        if (taxonomyBean.getMode() == Mode.EDIT) {
            return taxonomyBean.getTaxonomyToEdit().getTaxHierachy().get(0).getFirstName();
        }
        if (taxonomyBean.getSelectedTaxonomy() != null) {
            Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            return t.getFirstName();
        } else {
            return "";
        }
    }

    public boolean isNewParentRendered() {
        return taxonomyBean.getMode() == Mode.EDIT;
    }

    public String getCategoryOfChoosenTaxo() {
        if (taxonomyBean.getSelectedTaxonomy() != null) {
            Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            return t.getLevel().getId() + " - " + t.getLevel().getName();
        } else {
            return "";
        }
    }
}
