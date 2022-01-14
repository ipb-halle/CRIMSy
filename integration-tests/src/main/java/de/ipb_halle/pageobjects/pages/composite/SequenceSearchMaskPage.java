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
package de.ipb_halle.pageobjects.pages.composite;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/resources/crimsy/sequenceSearchMask.xhtml
 * 
 * @author flange
 */
public class SequenceSearchMaskPage extends AbstractPage {
    private static final SelenideElement QUERY_SEQUENCE_INPUT = $(
            testId("textarea", "sequenceSearchMask:querySequence"));
    private static final SelenideElement SEARCH_MODE_SELECTION = $(
            testId("select", "sequenceSearchMask:searchMode"));
    private static final SelenideElement TRANSLATION_TABLE_SELECTION = $(
            testId("select", "sequenceSearchMask:translationTable"));
    private static final SelenideElement MAX_RESULTS_SELECTION = $(
            testId("select", "sequenceSearchMask:maxResults"));

    /*
     * Actions
     */

    /*
     * Getters
     */
    public SelenideElement getQuerySequenceInput() {
        return QUERY_SEQUENCE_INPUT;
    }

    public SelenideElement getSearchModeSelection() {
        return SEARCH_MODE_SELECTION;
    }

    public SelenideElement getTranslationTableSelection() {
        return TRANSLATION_TABLE_SELECTION;
    }

    public SelenideElement getMaxResultsSelection() {
        return MAX_RESULTS_SELECTION;
    }
}