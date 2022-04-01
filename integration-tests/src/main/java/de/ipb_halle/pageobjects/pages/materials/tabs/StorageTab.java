/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.pageobjects.pages.materials.tabs;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Apply.applyCheckbox;
import static de.ipb_halle.pageobjects.util.Apply.applySelection;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesSelectBooleanCheckbox;
import de.ipb_halle.pageobjects.components.PrimeFacesSelectManyCheckbox;
import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.materials.models.StorageModel;

/**
 * Page object for
 * /ui/web/WEB-INF/templates/material/components/storageClasses.xhtml
 * 
 * @author flange
 */
public class StorageTab extends AbstractPage<StorageTab> implements MaterialEditTab {
    private static final PrimeFacesSelectBooleanCheckbox STORAGE_CLASS_ACTIVATED_CHECKBOX = new PrimeFacesSelectBooleanCheckbox(
            "storageTab:storageClassActivated");
    private static final SelenideElement STORAGE_CLASS_SELECTION = $(testId("select", "storageTab:storageClass"));
    private static final SelenideElement REMARKS_INPUT = $(testId("input", "storageTab:remarks"));
    private static final PrimeFacesSelectManyCheckbox STORAGE_CONDITIONS_CHECKBOXES = new PrimeFacesSelectManyCheckbox(
            "storageTab:storageConditions");

    /*
     * Actions
     */
    public StorageTab apply(StorageModel model) {
        applyCheckbox(model.getStorageClassActivated(), STORAGE_CLASS_ACTIVATED_CHECKBOX);
        applySelection(model.getStorageClass(), STORAGE_CLASS_SELECTION);
        applyValue(model.getRemarks(), REMARKS_INPUT);

        for (Integer num : model.getActivatedStorageConditions()) {
            if (!STORAGE_CONDITIONS_CHECKBOXES.isSelected(num - 1)) {
                STORAGE_CONDITIONS_CHECKBOXES.clickCheckbox(num - 1);
            }
        }
        for (Integer num : model.getDeactivatedStorageConditions()) {
            if (STORAGE_CONDITIONS_CHECKBOXES.isSelected(num - 1)) {
                STORAGE_CONDITIONS_CHECKBOXES.clickCheckbox(num - 1);
            }
        }

        return this;
    }

    public StorageTab clickStorageClassActivatedCheckbox() {
        STORAGE_CLASS_ACTIVATED_CHECKBOX.click();
        return this;
    }

    public StorageTab selectStorageClass(String storageClass) {
        STORAGE_CLASS_SELECTION.selectOption(storageClass);
        return this;
    }

    /*
     * Getters
     */
    public PrimeFacesSelectBooleanCheckbox storageClassActivatedCheckbox() {
        return STORAGE_CLASS_ACTIVATED_CHECKBOX;
    }

    public SelenideElement storageClassSelection() {
        return STORAGE_CLASS_SELECTION;
    }

    public SelenideElement remarksInput() {
        return REMARKS_INPUT;
    }

    public PrimeFacesSelectManyCheckbox storageConditionsCheckboxes() {
        return STORAGE_CONDITIONS_CHECKBOXES;
    }
}