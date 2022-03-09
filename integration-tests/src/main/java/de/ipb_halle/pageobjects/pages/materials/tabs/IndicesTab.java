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
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/WEB-INF/templates/material/components/indices.xhtml
 * 
 * @author flange
 */
public class IndicesTab extends AbstractPage<IndicesTab> implements MaterialEditTab {
    private static final SelenideElement INDEX_CATEGORY_SELECTION = $(testId("select", "indicesTab:indexCategory"));
    private static final SelenideElement INDEX_VALUE_INPUT = $(testId("input", "indicesTab:indexValue"));
    private static final SelenideElement ADD_INDEX_BUTTON = $(testId("indicesTab:addIndex"));
    private static final IndicesTable INDICES_TABLE = new IndicesTable("indicesTab:indicesTable");

    /*
     * Actions
     */
    public IndicesTab addIndex(String category, String value) {
        INDEX_CATEGORY_SELECTION.selectOption(category);
        INDEX_VALUE_INPUT.setValue(value);
        ADD_INDEX_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public IndicesTable getIndicesTable() {
        return INDICES_TABLE;
    }
}