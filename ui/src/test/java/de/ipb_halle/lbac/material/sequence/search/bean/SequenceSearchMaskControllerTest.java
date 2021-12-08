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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.fasta_search_service.models.search.TranslationTable;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.SequenceSearchServiceMock;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.SequenceSearchInformation;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;

/**
 * @author flange
 */
public class SequenceSearchMaskControllerTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();
    private SequenceSearchMaskController controller;
    private SequenceSearchResultsTableController sequenceSearchResultsTableController;
    private SequenceSearchServiceMock sequenceSearchServiceMock = new SequenceSearchServiceMock();
    SequenceAlignment alignment;

    @Before
    public void init() {
        Sequence sequence = new Sequence(42, null, 1, null, null, null);
        alignment = new SequenceAlignment(sequence, new FastaResult());

        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResult(alignment);
            return result;
        });

        sequenceSearchResultsTableController = new SequenceSearchResultsTableController(null, sequenceSearchServiceMock,
                null, messagePresenter);
        controller = new SequenceSearchMaskController(null, sequenceSearchResultsTableController, null, null, null,
                messagePresenter);
        sequenceSearchResultsTableController.setSearchMaskController(controller);
    }

    @Test
    public void test_getValues() {
        controller.setSearchMode(SearchMode.DNA_DNA);
        controller.setQuery("TGA");
        controller.setTranslationTable(TranslationTable.ALTERNATIVE_FLATWORM_MITOCHONDRIAL);
        MaterialSearchMaskValues values = controller.getValues();
        SequenceSearchInformation sequenceInfos = values.sequenceInfos;
        assertThat(values.type, contains(MaterialType.SEQUENCE));
        assertEquals(SequenceType.DNA, sequenceInfos.sequenceQueryType);
        assertEquals(SequenceType.DNA, sequenceInfos.sequenceLibraryType);
        assertEquals("TGA", sequenceInfos.sequenceQuery);
        assertEquals(TranslationTable.ALTERNATIVE_FLATWORM_MITOCHONDRIAL.getId(), sequenceInfos.translationTable);

        controller.setSearchMode(SearchMode.DNA_PROTEIN);
        controller.setQuery("ATG");
        controller.setTranslationTable(TranslationTable.BLEPHARISMA_MACRONUCLEAR);
        values = controller.getValues();
        sequenceInfos = values.sequenceInfos;
        assertThat(values.type, contains(MaterialType.SEQUENCE));
        assertEquals(SequenceType.DNA, sequenceInfos.sequenceQueryType);
        assertEquals(SequenceType.PROTEIN, sequenceInfos.sequenceLibraryType);
        assertEquals("ATG", sequenceInfos.sequenceQuery);
        assertEquals(TranslationTable.BLEPHARISMA_MACRONUCLEAR.getId(), sequenceInfos.translationTable);
    }

    @Test
    public void test_actionStartMaterialSearch() {
        assertThat(sequenceSearchResultsTableController.getResults(), empty());

        controller.actionStartMaterialSearch();
        List<FastaResultDisplayWrapper> results = sequenceSearchResultsTableController.getResults();
        assertThat(results, hasSize(1));
        assertEquals(alignment.getFoundSequence(), results.get(0).getSequence());
        assertEquals(alignment.getAlignmentInformation(), results.get(0).getFastaResult());
    }

    @Test
    public void test_actionClearSearchFilter() {
        controller.setQuery("TGA");
        controller.setMolecule("ABC");
        controller.actionClearSearchFilter();
        assertEquals("", controller.getQuery());
        assertEquals("", controller.getMolecule());
    }

    @Test
    public void test_isTranslationTableDisabled() {
        controller.setSearchMode(SearchMode.PROTEIN_PROTEIN);
        assertTrue(controller.isTranslationTableDisabled());

        controller.setSearchMode(SearchMode.PROTEIN_DNA);
        assertFalse(controller.isTranslationTableDisabled());
    }

    @Test
    public void test_getLocalizedSearchModeLabel() {
        assertEquals("sequenceSearch_searchMode_PROTEIN_PROTEIN",
                controller.getLocalizedSearchModeLabel(SearchMode.PROTEIN_PROTEIN));
        assertEquals("sequenceSearch_searchMode_DNA_PROTEIN",
                controller.getLocalizedSearchModeLabel(SearchMode.DNA_PROTEIN));
    }

    @Test
    public void test_getAndSetQuery() {
        assertEquals("", controller.getQuery());
        controller.setQuery("TGA");
        assertEquals("TGA", controller.getQuery());
    }

    @Test
    public void test_getAndSetSearchMode() {
        assertEquals(SearchMode.PROTEIN_PROTEIN, controller.getSearchMode());
        controller.setSearchMode(SearchMode.PROTEIN_DNA);
        assertEquals(SearchMode.PROTEIN_DNA, controller.getSearchMode());
    }

    @Test
    public void test_getSearchModeItems() {
        assertArrayEquals(SearchMode.values(), controller.getSearchModeItems());
    }

    @Test
    public void test_getAndSetTranslationTable() {
        assertEquals(TranslationTable.STANDARD, controller.getTranslationTable());
        controller.setTranslationTable(TranslationTable.BACTERIAL_AND_PLANT_PLASTID);
        assertEquals(TranslationTable.BACTERIAL_AND_PLANT_PLASTID, controller.getTranslationTable());
    }

    @Test
    public void test_getTranslationTableItems() {
        assertArrayEquals(TranslationTable.values(), controller.getTranslationTableItems());
    }

    @Test
    public void test_getAndSetMaxResults() {
        assertEquals(50, controller.getMaxResults());
        controller.setMaxResults(42);
        assertEquals(42, controller.getMaxResults());
    }

    @Test
    public void test_getMaxResultItems() {
        assertTrue(controller.getMaxResultItems().length >= 1);
    }
}