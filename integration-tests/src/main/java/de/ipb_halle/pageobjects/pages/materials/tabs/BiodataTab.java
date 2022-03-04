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

import de.ipb_halle.pageobjects.components.PrimeFacesTree;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/WEB-INF/templates/material/components/biodata.xhtml
 * 
 * @author flange
 */
public class BiodataTab extends AbstractPage<BiodataTab> implements MaterialEditTab {
    private static final PrimeFacesTree TAXONOMY_TREE = new PrimeFacesTree(
            "biodataTab:taxonomyTree");
    private static final SelenideElement SELECTED_TAXONOMY_INPUT = $(
            testId("input", "biodataTab:selectedTaxonomy"));

    /*
     * Actions
     */

    /*
     * Getters
     */
    public PrimeFacesTree getTaxonomyTree() {
        return TAXONOMY_TREE;
    }

    public SelenideElement getSelectedTaxonomyInput() {
        return SELECTED_TAXONOMY_INPUT;
    }
}