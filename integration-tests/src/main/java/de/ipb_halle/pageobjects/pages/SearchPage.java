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
package de.ipb_halle.pageobjects.pages;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.EnumSet;
import java.util.Set;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.plugins.MolPlugin;

/**
 * @author flange
 */
public class SearchPage extends AbstractPage {
    private static final SelenideElement SEARCH_TEXT = $(
            testId("input", "search:searchText"));
    private static final SelenideElement SEARCH_BUTTON = $(
            testId("search:searchButton"));
    private static final SelenideElement REFRESH_RESULT_BUTTON = $(
            testId("search:refreshResultsButton"));
    private static final SelenideElement NUMBER_OF_NEW_DOCUMENTS_BADGE = $(
            testId("search:numberOfNewDocumentsBadge"));
    private static final SelenideElement UPLOAD_DOCUMENT_BUTTON = $(
            testId("search:uploadDocumentButton"));
    private static final SelenideElement SEARCH_RESULTS_TABLE = $(
            testId("search:searchResultsTable"));
    private static final SelenideElement TOGGLE_ADVANCED_SEARCH_BUTTON = $(
            testId("search:toggleAdvancedSearchButton"));
    private static final String MOLEDITOR_TESTID = "search:molEditor";
    private static final String MOLEDITOR_WIDGETVAR = "molEditorWidgetVar";

    public enum TypeFilter {
        DOCUMENT("search:filterDocumentCheckbox"),
        STRUCTURE("search:filterStructureCheckbox"),
        MATERIAL("search:filterMaterialCheckbox"),
        BIOMATERIAL("search:filterBioMaterialCheckbox"),
        ITEM("search:filterItemCheckbox"),
        SEQUENCE("search:filterSequenceCheckbox"),
        EXPERIMENT("search:filterExperimentCheckbox"),
        COMPOSITION("search:filterCompositionCheckbox");

        private final SelenideElement checkbox;

        private TypeFilter(String testId) {
            checkbox = $(testId("input", testId));
        }
    }

    public SearchPage activateFilter(TypeFilter filter) {
        if (!filter.checkbox.isSelected()) {
            filter.checkbox.click();
        }
        return this;
    }

    public SearchPage deactivateFilter(TypeFilter filter) {
        if (filter.checkbox.isSelected()) {
            filter.checkbox.click();
        }
        return this;
    }

    public Set<TypeFilter> getActiveTypeFilters() {
        Set<TypeFilter> result = EnumSet.noneOf(TypeFilter.class);
        for (TypeFilter tf : TypeFilter.values()) {
            if (tf.checkbox.isSelected()) {
                result.add(tf);
            }
        }
        return result;
    }

    public SearchPage setActiveTypeFilters(Set<TypeFilter> filters) {
        Set<TypeFilter> alreadyActive = getActiveTypeFilters();
        EnumSet<TypeFilter> activate = EnumSet.allOf(TypeFilter.class);
        activate.removeAll(alreadyActive);
        EnumSet<TypeFilter> deactivate = EnumSet.complementOf(activate);

        // activate
        for (TypeFilter tf : activate) {
            tf.checkbox.click();
        }
        // deactivate
        for (TypeFilter tf : deactivate) {
            tf.checkbox.click();
        }

        return this;
    }

    public int getNumberOfNewDocuments() {
        return Integer.parseInt(NUMBER_OF_NEW_DOCUMENTS_BADGE.getText());
    }

    public MolPlugin getMolEditor() {
        return new MolPlugin(MOLEDITOR_TESTID, MOLEDITOR_WIDGETVAR);
    }

    public SelenideElement getSearchText() {
        return SEARCH_TEXT;
    }

    public SelenideElement getSearchButton() {
        return SEARCH_BUTTON;
    }

    public SelenideElement getRefreshResultButton() {
        return REFRESH_RESULT_BUTTON;
    }

    public SelenideElement getUploadDocumentButton() {
        return UPLOAD_DOCUMENT_BUTTON;
    }

    public SelenideElement getToggleAdvancedSearchButton() {
        return TOGGLE_ADVANCED_SEARCH_BUTTON;
    }
}