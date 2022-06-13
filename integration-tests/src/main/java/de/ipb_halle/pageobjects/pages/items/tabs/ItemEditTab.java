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
package de.ipb_halle.pageobjects.pages.items.tabs;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for the tabView in /ui/web/WEB-INF/templates/item/itemEdit.xhtml
 * 
 * @author flange
 */
public interface ItemEditTab {
    static final SelenideElement BASIC_INFORMATION_TAB = $(testId("itemEdit:basicInformationTab"));
    static final SelenideElement PROJECT_TAB = $(testId("itemEdit:projectTab"));
    static final SelenideElement LOCATION_TAB = $(testId("itemEdit:locationTab"));
    static final SelenideElement PRINT_TAB = $(testId("itemEdit:printTab"));

    /*
     * Actions
     */
    public default BasicInformationTab openBasicInformationTab() {
        BASIC_INFORMATION_TAB.click();
        return page(BasicInformationTab.class);
    }

    public default ProjectTab openProjectTab() {
        PROJECT_TAB.click();
        return page(ProjectTab.class);
    }

    public default LocationTab openLocationTab() {
        LOCATION_TAB.click();
        return page(LocationTab.class);
    }

    public default PrintTab openPrintTab() {
        PRINT_TAB.click();
        return page(PrintTab.class);
    }
}