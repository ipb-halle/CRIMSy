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
import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.fasta_search_service.models.search.TranslationTable;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.material.sequence.search.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.search.SequenceSearchInformation;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchServiceMock;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;

/**
 * @author flange
 */
public class SequenceSearchMaskControllerTest {
    private MessagePresenterMock messagePresenter = MessagePresenterMock.getInstance();
    private SequenceSearchMaskController controller;
    private SequenceSearchMaskValuesHolder valuesHolder;
    private SequenceSearchResultsTableController sequenceSearchResultsTableController;
    private SequenceSearchServiceMock sequenceSearchServiceMock = new SequenceSearchServiceMock();
    private SequenceAlignment alignment;

    @BeforeEach
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
        valuesHolder = controller.getValuesHolder();
        sequenceSearchResultsTableController.setSearchMaskController(controller);
    }

    @Test
    public void test_getValues() {
        valuesHolder.setSearchMode(SearchMode.DNA_DNA);
        valuesHolder.setQuery("TGA");
        valuesHolder.setTranslationTable(TranslationTable.ALTERNATIVE_FLATWORM_MITOCHONDRIAL);
        MaterialSearchMaskValues values = controller.getValues();
        SequenceSearchInformation sequenceInfos = values.sequenceInfos;
        assertThat(values.type, contains(MaterialType.SEQUENCE));
        assertEquals(SequenceType.DNA, sequenceInfos.sequenceQueryType);
        assertEquals(SequenceType.DNA, sequenceInfos.sequenceLibraryType);
        assertEquals("TGA", sequenceInfos.sequenceQuery);
        assertEquals(TranslationTable.ALTERNATIVE_FLATWORM_MITOCHONDRIAL.getId(), sequenceInfos.translationTable);

        valuesHolder.setSearchMode(SearchMode.DNA_PROTEIN);
        valuesHolder.setQuery("ATG");
        valuesHolder.setTranslationTable(TranslationTable.BLEPHARISMA_MACRONUCLEAR);
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
        valuesHolder.setQuery("TGA");
        controller.setMolecule("ABC");
        controller.actionClearSearchFilter();
        assertEquals("", valuesHolder.getQuery());
        assertEquals("", controller.getMolecule());
    }
}