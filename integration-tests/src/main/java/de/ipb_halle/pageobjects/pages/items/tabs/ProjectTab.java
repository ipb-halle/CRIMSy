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
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for the project tab in
 * /ui/web/WEB-INF/templates/item/itemEdit.xhtml
 * 
 * @author flange
 */
public class ProjectTab implements ItemEditTab {
    private static final SelenideElement PROJECT_SELECTION = $(testId("select", "projectTab:project"));

    /*
     * Actions
     */
    public ProjectTab selectProject(String project) {
        PROJECT_SELECTION.selectOption(project);
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement projectSelection() {
        return PROJECT_SELECTION;
    }
}