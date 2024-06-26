/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.sequence.search.bean;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import de.ipb_halle.fasta_search_service.models.search.TranslationTable;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * POJO that holds form data used in the sequence search mask composite
 * component.
 * 
 * @author flange
 */
public class SequenceSearchMaskValuesHolder implements Serializable {
    private static final long serialVersionUID = 1L;
    private final MessagePresenter messagePresenter;

    @NotBlank
    private String query = "";

    private SearchMode searchMode = SearchMode.PROTEIN_PROTEIN;
    private static final SearchMode[] searchModeItems = SearchMode.values();

    private TranslationTable translationTable = TranslationTable.STANDARD;
    private static final TranslationTable[] translationTableItems = TranslationTable.values();

    private int maxResults = maxResultItems[4];
    private static final int[] maxResultItems = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 150, 200, 250, 500, 750,
            1000 };

    public SequenceSearchMaskValuesHolder(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    /*
     * Getters with logic
     */
    /**
     * @return {@code true} in case the active {@code searchMode} does not require a
     *         translation table.
     */
    public boolean isTranslationTableDisabled() {
        return !searchMode.needsTranslationTable();
    }

    /**
     * @param mode
     * @return 18nized name for the given {@link SearchMode}
     */
    public String getLocalizedSearchModeLabel(SearchMode mode) {
        return messagePresenter.presentMessage("sequenceSearch_searchMode_" + mode.toString());
    }

    /*
     * Getters/setters for fields
     */
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public SearchMode[] getSearchModeItems() {
        return searchModeItems;
    }

    public TranslationTable getTranslationTable() {
        return translationTable;
    }

    public void setTranslationTable(TranslationTable translationTable) {
        this.translationTable = translationTable;
    }

    public TranslationTable[] getTranslationTableItems() {
        return translationTableItems;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int[] getMaxResultItems() {
        return maxResultItems;
    }
}
