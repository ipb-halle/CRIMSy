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
package de.ipb_halle.pageobjects.pages.items;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.items.models.ItemSearchMaskModel;

/**
 * Page object for the search mask /ui/web/WEB-INF/templates/item/items.xhtml
 * 
 * @author flange
 */
public class ItemSearchMaskPage {
    private static final SelenideElement MATERIAL_NAME_INPUT = $(testId("itemSearchMask:materialName"));
    private static final SelenideElement LABEL_INPUT = $(testId("input", "itemSearchMask:label"));
    private static final SelenideElement USER_INPUT = $(testId("itemSearchMask:user"));
    private static final SelenideElement PROJECT_INPUT = $(testId("itemSearchMask:project"));
    private static final SelenideElement LOCATION_INPUT = $(testId("itemSearchMask:location"));
    private static final SelenideElement DESCRIPTION_INPUT = $(testId("input", "itemSearchMask:description"));
    private static final SelenideElement CLEAR_BUTTON = $(testId("itemSearchMask:clear"));
    private static final SelenideElement SEARCH_BUTTON = $(testId("itemSearchMask:search"));

    /*
     * Actions
     */
    /**
     * Applies the search mask model.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public ItemSearchMaskPage applyModel(ItemSearchMaskModel model) {
        applyValue(model.getMaterialName(), MATERIAL_NAME_INPUT);
        applyValue(model.getLabel(), LABEL_INPUT);
        applyValue(model.getUserName(), USER_INPUT);
        applyValue(model.getProjectName(), PROJECT_INPUT);
        applyValue(model.getLocation(), LOCATION_INPUT);
        applyValue(model.getDescription(), DESCRIPTION_INPUT);

        return this;
    }

    public ItemSearchMaskPage clear() {
        CLEAR_BUTTON.click();
        return this;
    }

    public ItemSearchMaskPage search() {
        SEARCH_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public ItemSearchMaskModel getModel() {
        return new ItemSearchMaskModel()
                .materialName(MATERIAL_NAME_INPUT.text())
                .label(LABEL_INPUT.text())
                .userName(USER_INPUT.text())
                .projectName(PROJECT_INPUT.text())
                .location(LOCATION_INPUT.text())
                .description(DESCRIPTION_INPUT.text());
    }
}