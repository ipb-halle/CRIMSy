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

import static org.junit.Assert.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.fasta_search_service.models.search.TranslationTable;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;

/**
 * @author flange
 */
public class SequenceSearchMaskValuesHolderTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();
    private SequenceSearchMaskValuesHolder valuesHolder;

    @BeforeEach
    public void init() {
        valuesHolder = new SequenceSearchMaskValuesHolder(messagePresenter);
    }

    @Test
    public void test_isTranslationTableDisabled() {
        valuesHolder.setSearchMode(SearchMode.PROTEIN_PROTEIN);
        assertTrue(valuesHolder.isTranslationTableDisabled());

        valuesHolder.setSearchMode(SearchMode.PROTEIN_DNA);
        assertFalse(valuesHolder.isTranslationTableDisabled());
    }

    @Test
    public void test_getLocalizedSearchModeLabel() {
        assertEquals("sequenceSearch_searchMode_PROTEIN_PROTEIN",
                valuesHolder.getLocalizedSearchModeLabel(SearchMode.PROTEIN_PROTEIN));
        assertEquals("sequenceSearch_searchMode_DNA_PROTEIN",
                valuesHolder.getLocalizedSearchModeLabel(SearchMode.DNA_PROTEIN));
    }

    @Test
    public void test_getAndSetQuery() {
        assertEquals("", valuesHolder.getQuery());
        valuesHolder.setQuery("TGA");
        assertEquals("TGA", valuesHolder.getQuery());
    }

    @Test
    public void test_getAndSetSearchMode() {
        assertEquals(SearchMode.PROTEIN_PROTEIN, valuesHolder.getSearchMode());
        valuesHolder.setSearchMode(SearchMode.PROTEIN_DNA);
        assertEquals(SearchMode.PROTEIN_DNA, valuesHolder.getSearchMode());
    }

    @Test
    public void test_getSearchModeItems() {
        assertArrayEquals(SearchMode.values(), valuesHolder.getSearchModeItems());
    }

    @Test
    public void test_getAndSetTranslationTable() {
        assertEquals(TranslationTable.STANDARD, valuesHolder.getTranslationTable());
        valuesHolder.setTranslationTable(TranslationTable.BACTERIAL_AND_PLANT_PLASTID);
        assertEquals(TranslationTable.BACTERIAL_AND_PLANT_PLASTID, valuesHolder.getTranslationTable());
    }

    @Test
    public void test_getTranslationTableItems() {
        assertArrayEquals(TranslationTable.values(), valuesHolder.getTranslationTableItems());
    }

    @Test
    public void test_getAndSetMaxResults() {
        assertEquals(50, valuesHolder.getMaxResults());
        valuesHolder.setMaxResults(42);
        assertEquals(42, valuesHolder.getMaxResults());
    }

    @Test
    public void test_getMaxResultItems() {
        assertTrue(valuesHolder.getMaxResultItems().length >= 1);
    }
}
