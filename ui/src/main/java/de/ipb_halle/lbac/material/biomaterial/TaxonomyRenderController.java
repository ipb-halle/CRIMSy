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

import de.ipb_halle.lbac.material.biomaterial.TaxonomyBean.Mode;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class TaxonomyRenderController implements Serializable {

    protected TaxonomyBean taxonomyBean;
    protected TaxonomyNameController nameController;
    TaxonomyLevelController levelController;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private MemberService memberService;
    private SimpleDateFormat SDF = new SimpleDateFormat(" yyyy-MM-dd HH:mm");
    private MessagePresenter messagePresenter;

    public TaxonomyRenderController(
            TaxonomyBean taxonomyBean,
            TaxonomyNameController nameController,
            TaxonomyLevelController levelController,
            MemberService memberService,
            MessagePresenter messagePresenter) {
        this.taxonomyBean = taxonomyBean;
        this.nameController = nameController;
        this.levelController = levelController;
        this.memberService = memberService;
        this.messagePresenter = messagePresenter;
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

            return messagePresenter.presentMessage("taxonomy_button_cancel");
        } else {
            return messagePresenter.presentMessage("taxonomy_button_edit");
        }
    }

    public String getSecondButtonLabel() {
        if (taxonomyBean.getMode() == Mode.SHOW) {

            return messagePresenter.presentMessage("taxonomy_button_add");
        } else {
            return messagePresenter.presentMessage("taxonomy_button_save");
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

    public String getInfoHeader() {
        if (taxonomyBean.getMode() == Mode.SHOW || taxonomyBean.getMode() == Mode.HISTORY) {
            return messagePresenter.presentMessage("taxonomy_label_detail");
        }
        if (taxonomyBean.getMode() == Mode.CREATE) {
            return messagePresenter.presentMessage("taxonomy_label_new");
        }
        if (taxonomyBean.getMode() == Mode.EDIT) {
            return messagePresenter.presentMessage("taxonomy_label_edit");
        }
        return "";
    }

    public String getOwnerInfoForSelectedTaxonomy() {
        if (shouldTaxonomyInfosBeDisplayed()) {
            return messagePresenter.presentMessage("taxonomy_label_created")
                    + SDF.format(selectTaxonomyToShow().getCreationTime());
        }
        return "";
    }

    public String getEditInfoForSelectedTaxonomy() {
        if (shouldTaxonomyInfosBeDisplayed()) {
            if (selectTaxonomyToShow().getHistory().getChanges().isEmpty()) {
                return "";
            }
            return messagePresenter.presentMessage("taxonomy_label_edit_by")
                    + SDF.format(selectTaxonomyToShow().getHistory().getChanges().lastKey());
        }
        return "";
    }

    public String getInfoForSelectedTaxonomy() {
        if (shouldTaxonomyInfosBeDisplayed()) {
            return selectTaxonomyToShow().getFirstName()
                    + " (ID: "
                    + selectTaxonomyToShow().getId()
                    + ")";
        }
        return "";
    }

    private Taxonomy selectTaxonomyToShow() {
        Taxonomy t;
        if (taxonomyBean.getMode() == Mode.EDIT) {
            t = taxonomyBean.getTaxonomyBeforeEdit();
        } else {
            t = (Taxonomy) taxonomyBean.getSelectedTaxonomy().getData();
        }
        return t;
    }

    private boolean shouldTaxonomyInfosBeDisplayed() {
        return taxonomyBean.getMode() == Mode.SHOW
                || taxonomyBean.getMode() == Mode.HISTORY
                || taxonomyBean.getMode() == Mode.EDIT;
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
