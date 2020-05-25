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
package de.ipb_halle.lbac.material.subtype.taxonomy;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.material.bean.TaxonomyBean;
import de.ipb_halle.lbac.material.bean.TaxonomyBean.Mode;
import de.ipb_halle.lbac.material.common.MaterialName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class TaxonomyRenderController {

    protected TaxonomyBean taxonomyBean;
    protected TaxonomyNameController nameController;
    TaxonomyLevelController levelController;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public TaxonomyRenderController(
            TaxonomyBean taxonomyBean,
            TaxonomyNameController nameController,
            TaxonomyLevelController levelController) {
        this.taxonomyBean = taxonomyBean;
        this.nameController = nameController;
        this.levelController = levelController;
    }

    /**
     * Calculates if the first button (EDIT or CANCEL) is disabled.
     *
     * @return
     */
    public boolean isFirstButtonDisabled() {
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.SHOW
                && taxonomyBean.getSelectedTaxonomy() != null) {
            Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
            return t.getLevel().getRank() == levelController.getHighestRank();
        }
        if (taxonomyBean.getMode() == TaxonomyBean.Mode.EDIT
                || taxonomyBean.getMode() == TaxonomyBean.Mode.CREATE) {
            return false;
        } else {
            return true;
        }
    }

    public String getEditButtonLabel() {
        if (taxonomyBean.getMode() == Mode.CREATE || taxonomyBean.getMode() == Mode.EDIT) {

            return Messages.getString(MESSAGE_BUNDLE, "taxonomy_button_cancel", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "taxonomy_button_edit", null);
        }
    }

    public String getSecondButtonLabel() {
        if (taxonomyBean.getMode() == Mode.SHOW) {
            return Messages.getString(MESSAGE_BUNDLE, "taxonomy_button_add", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "taxonomy_button_save", null);
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
                return "Detail information for " + t.getFirstName() + " (" + t.getId() + ")";
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

    public boolean isSecondButtonDisabled() {
        if (taxonomyBean.getMode() == Mode.HISTORY) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCategoryVisible() {
        boolean isVisible = false;
        if ((taxonomyBean.getMode() == Mode.SHOW
                || taxonomyBean.getMode() == Mode.HISTORY)
                && taxonomyBean.getSelectedTaxonomy() != null) {
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

    public boolean isHistoryBackButtonDisabled() {
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        boolean noHistory = t.getHistory().getChanges().isEmpty();
        boolean atMostFarDate = taxonomyBean.getHistoryController().getDateOfShownHistory() == null;
        return noHistory || atMostFarDate;
    }

    public boolean isHistoryForwardButtonDisabled() {
        Taxonomy t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        boolean noHistory = t.getHistory().getChanges().isEmpty();
        boolean atMostRecentDate = t.getHistory().isMostRecentVersion(taxonomyBean.getHistoryController().getDateOfShownHistory());
        return noHistory || atMostRecentDate;
    }
}
