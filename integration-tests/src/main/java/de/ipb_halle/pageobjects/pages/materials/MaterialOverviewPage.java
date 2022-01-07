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
package de.ipb_halle.pageobjects.pages.materials;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.table.DataTable;

/**
 * Page object for /ui/web/WEB-INF/templates/material/materials.xhtml
 * 
 * @author flange
 */
public class MaterialOverviewPage extends AbstractPage {
    private static final SelenideElement NEW_MATERIAL_BUTTON = $(
            testId("materialOverview:newMaterial"));
    private static final SelenideElement RESULTS_TABLE = $(
            testId("materialOverview:resultsTable"));
    private static final SelenideElement FIRST_PAGE_BUTTON = $(
            testId("materialOverview:firstPageButton"));
    private static final SelenideElement PREV_PAGE_BUTTON = $(
            testId("materialOverview:prevPageButton"));
    private static final SelenideElement NEXT_PAGE_BUTTON = $(
            testId("materialOverview:nextPageButton"));
    private static final SelenideElement LAST_PAGE_BUTTON = $(
            testId("materialOverview:lastPageButton"));
    private static final SelenideElement PAGINATION_INFO_TEXT = $(
            testId("materialOverview:paginationInfoText"));

    /*
     * Actions
     */
    public MaterialOverviewPage search(MaterialSearchMaskModel model) {
        getMaterialSearchMask().applyModel(model).search();
        return this;
    }

    public MaterialOverviewPage search() {
        getMaterialSearchMask().search();
        return this;
    }

    public MaterialEditPage newMaterial() {
        NEW_MATERIAL_BUTTON.click();
        return page(MaterialEditPage.class);
    }

    public MaterialOverviewPage firstPage() {
        FIRST_PAGE_BUTTON.click();
        return this;
    }

    public MaterialOverviewPage prevPage() {
        PREV_PAGE_BUTTON.click();
        return this;
    }

    public MaterialOverviewPage nextPage() {
        NEXT_PAGE_BUTTON.click();
        return this;
    }

    public MaterialOverviewPage lastPage() {
        LAST_PAGE_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public MaterialSearchMaskPage getMaterialSearchMask() {
        return page(MaterialSearchMaskPage.class);
    }

    public DataTable getResultsTable() {
        return DataTable.extract(RESULTS_TABLE);
    }

    public SelenideElement getPaginationInfoText() {
        return PAGINATION_INFO_TEXT;
    }
}