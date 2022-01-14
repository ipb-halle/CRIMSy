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
package de.ipb_halle.pageobjects.components;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import org.openqa.selenium.By.ByClassName;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for PrimeFaces' tree component.
 * 
 * @author flange
 */
public class PrimeFacesTree {
    private final SelenideElement element;

    public PrimeFacesTree(String testId) {
        element = $(testId(testId));
    }

    /**
     * Toggle an item in the taxonomy tree. The item has to be visible. Item
     * names are not unique inside the tree, so the nth item with the given name
     * is selected.
     * 
     * @param name
     * @param n    index of item (starting with 0)
     * @return this object
     */
    public PrimeFacesTree toggleTaxonomyItem(String name, int n) {
        SelenideElement spanWithName = element.$$(byText(name)).get(n);
        SelenideElement parent = spanWithName.parent();
        SelenideElement toggleSpan = parent
                .$(new ByClassName("ui-tree-toggler"));
        toggleSpan.click();
        return this;
    }

    /**
     * Select an item in the taxonomy tree. The item has to be visible. Item
     * names are not unique inside the tree, so the nth item with the given name
     * is selected.
     * 
     * @param name
     * @param n    index of item (starting with 0)
     * @return this object
     */
    public PrimeFacesTree selectTaxonomyItem(String name, int n) {
        element.$$(byText(name)).get(n).click();
        return this;
    }
}