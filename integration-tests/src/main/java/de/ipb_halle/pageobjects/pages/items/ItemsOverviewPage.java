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
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;
import de.ipb_halle.pageobjects.pages.NavigablePage;
import de.ipb_halle.pageobjects.pages.items.models.ItemSearchMaskModel;

/**
 * Page object for /ui/web/WEB-INF/templates/item/items.xhtml
 * 
 * @author flange
 */
public class ItemsOverviewPage extends NavigablePage<ItemsOverviewPage> {
    private static final SelenideElement REPORT_SELECTION = $(testId("select", "itemOverview:reportSelection"));
    private static final SelenideElement REPORT_TYPE_SELECTION = $(testId("select", "itemOverview:reportType"));
    private static final SelenideElement CREATE_REPORT_BUTTON = $(testId("itemOverview:createReport"));
    private static final ItemSearchResultsTable RESULTS_TABLE = new ItemSearchResultsTable("itemOverview:resultsTable");
    private static final SelenideElement FIRST_PAGE_BUTTON = $(testId("itemOverview:firstPageButton"));
    private static final SelenideElement PREV_PAGE_BUTTON = $(testId("itemOverview:prevPageButton"));
    private static final SelenideElement NEXT_PAGE_BUTTON = $(testId("itemOverview:nextPageButton"));
    private static final SelenideElement LAST_PAGE_BUTTON = $(testId("itemOverview:lastPageButton"));
    private static final SelenideElement PAGINATION_INFO_TEXT = $(testId("itemOverview:paginationInfoText"));

    @Override
    public Navigation getNavigationItem() {
        return Navigation.ITEMS_OVERVIEW;
    }

    /*
     * Actions
     */
    public ItemsOverviewPage search(ItemSearchMaskModel model) {
        getItemSearchMask().applyModel(model).search();
        return this;
    }

    public ItemsOverviewPage search() {
        getItemSearchMask().search();
        return this;
    }

    public ItemsOverviewPage selectReport(String report) {
        REPORT_SELECTION.selectOption(report);
        return this;
    }

    public ItemsOverviewPage selectReportType(String type) {
        REPORT_TYPE_SELECTION.selectOption(type);
        return this;
    }

    public ItemsOverviewPage createReport() {
        CREATE_REPORT_BUTTON.click();
        return this;
    }

    public ItemsOverviewPage firstPage() {
        FIRST_PAGE_BUTTON.click();
        return this;
    }

    public ItemsOverviewPage prevPage() {
        PREV_PAGE_BUTTON.click();
        return this;
    }

    public ItemsOverviewPage nextPage() {
        NEXT_PAGE_BUTTON.click();
        return this;
    }

    public ItemsOverviewPage lastPage() {
        LAST_PAGE_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public ItemSearchMaskPage getItemSearchMask() {
        return page(ItemSearchMaskPage.class);
    }

    public SelenideElement getReportSelection() {
        return REPORT_SELECTION;
    }

    public SelenideElement getReportFormatSelection() {
        return REPORT_TYPE_SELECTION;
    }

    public ItemSearchResultsTable getResultsTable() {
        return RESULTS_TABLE;
    }

    public SelenideElement getPaginationInfoText() {
        return PAGINATION_INFO_TEXT;
    }
}