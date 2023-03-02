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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.search.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParser;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParserException;
import de.ipb_halle.lbac.material.sequence.search.display.TfastxyResultDisplayConfig;
import de.ipb_halle.lbac.material.sequence.search.service.SequenceSearchServiceMock;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.util.ResourceUtils;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;

/**
 * @author flange
 */
public class SequenceSearchResultsTableControllerTest {
    private SendFileBeanMock sendFileBeanMock;
    private SequenceSearchServiceMock sequenceSearchServiceMock = new SequenceSearchServiceMock();
    private SequenceSearchMaskController sequenceSearchMaskController;
    private SequenceSearchResultsTableController controller;
    private Sequence sequence1, sequence2, sequence3;
    private SequenceAlignment alignment1, alignment2, alignment3a, alignment3b;
    private List<SequenceAlignment> alignments;
    private List<FastaResult> fastaResults;
    private MessagePresenterMock messagePresenter = new MessagePresenterMock();

    @BeforeEach
    public void init() throws FastaResultParserException {
        messagePresenter.resetMessages();

        SequenceData data1 = SequenceData.builder().sequenceString("sequence1").build();
        SequenceData data2 = SequenceData.builder().sequenceString("sequence2").build();
        SequenceData data3 = SequenceData.builder().sequenceString("sequence3").build();

        List<MaterialName> names1 = Arrays.asList(new MaterialName("firstName1", "en", 1),
                new MaterialName("secondName1", "de", 100));
        List<MaterialName> names2 = Arrays.asList(new MaterialName("firstName2", "en", 1),
                new MaterialName("secondName2", "de", 100));
        List<MaterialName> names3 = Arrays.asList(new MaterialName("firstName3", "en", 1),
                new MaterialName("secondName3", "de", 100));

        sequence1 = new Sequence(1, names1, 1, null, null, data1);
        sequence2 = new Sequence(2, names2, 1, null, null, data2);
        sequence3 = new Sequence(3, names3, 1, null, null, data3);

        Reader reader = ResourceUtils.readerForResourceFile("fastaresults/results1.txt");
        fastaResults = new FastaResultParser(reader).parse();

        alignment1 = new SequenceAlignment(sequence1, fastaResults.get(3));
        alignment2 = new SequenceAlignment(sequence2, fastaResults.get(1));
        alignment3a = new SequenceAlignment(sequence3, fastaResults.get(2));
        alignment3b = new SequenceAlignment(sequence3, fastaResults.get(0));
        alignments = Arrays.asList(alignment1, alignment2, alignment3a, alignment3b);

        sendFileBeanMock = new SendFileBeanMock();

        controller = new SequenceSearchResultsTableController(null, sequenceSearchServiceMock, sendFileBeanMock,
                messagePresenter);
        sequenceSearchMaskController = new SequenceSearchMaskController(null, controller, null, null, null, null);
        controller.setSearchMaskController(sequenceSearchMaskController);
    }

    @Test
    public void test_reloadDataTableItems_withDifferentSearchModes() {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResults(alignments);
            return result;
        });

        sequenceSearchMaskController.getValuesHolder().setSearchMode(SearchMode.DNA_DNA);
        sequenceSearchMaskController.actionStartMaterialSearch();
        List<FastaResultDisplayWrapper> results = controller.getResults();
        for (FastaResultDisplayWrapper wrapper : results) {
            assertThat(wrapper.getConfig(), instanceOf(FastaResultDisplayConfig.class));
        }

        sequenceSearchMaskController.getValuesHolder().setSearchMode(SearchMode.PROTEIN_DNA);
        sequenceSearchMaskController.actionStartMaterialSearch();
        results = controller.getResults();
        for (FastaResultDisplayWrapper wrapper : results) {
            assertThat(wrapper.getConfig(), instanceOf(TfastxyResultDisplayConfig.class));
        }
    }

    @Test
    public void test_reloadDataTableItems_normalSearch_checkMessages() {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResults(alignments);
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();

        assertNull(messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_reloadDataTableItems_withErrorInSearchResult() {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addErrorMessage("error!");
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();

        assertThat(controller.getResults(), empty());
        assertNull(messagePresenter.getLastInfoMessage());
        assertEquals("sequenceSearch_error", messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_reloadDataTableItems_emptyResults() {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();

        assertThat(controller.getResults(), empty());
        assertEquals("sequenceSearch_noResults", messagePresenter.getLastInfoMessage());
        assertNull(messagePresenter.getLastErrorMessage());
    }

    @Test
    public void test_actionDownloadAllResultsAsFasta_after_loadingResults() throws IOException {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResults(alignments);
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();
        controller.actionDownloadAllResultsAsFasta();

        String result = new String(sendFileBeanMock.getContent());
        String expected = ">3 firstName3\nsequence3\n>2 firstName2\nsequence2\n>1 firstName1\nsequence1";
        assertEquals(expected, result);
        assertEquals("results.fasta", sendFileBeanMock.getFilename());

        // result list sorted differently
        controller.setSortBy(SortItem.SUBJECTNAME);
        sendFileBeanMock.reset();
        controller.actionDownloadAllResultsAsFasta();

        result = new String(sendFileBeanMock.getContent());
        expected = ">1 firstName1\nsequence1\n>2 firstName2\nsequence2\n>3 firstName3\nsequence3";
        assertEquals(expected, result);
        assertEquals("results.fasta", sendFileBeanMock.getFilename());

        // empty result list
        sendFileBeanMock.reset();
        sequenceSearchServiceMock.setBehaviour(request -> new SearchResultImpl(new Node()));
        sequenceSearchMaskController.actionStartMaterialSearch();
        controller.actionDownloadAllResultsAsFasta();

        assertNull(sendFileBeanMock.getContent());
        assertNull(sendFileBeanMock.getFilename());
    }

    @Test
    public void test_actionDownloadResultAsFasta_after_loadingResults() throws IOException {
        controller.actionDownloadResultAsFasta(null, 42);
        assertNull(sendFileBeanMock.getContent());
        assertNull(sendFileBeanMock.getFilename());

        FastaResultDisplayWrapper wrapper = new FastaResultDisplayWrapper(null, null);
        controller.actionDownloadResultAsFasta(wrapper, 42);
        assertNull(sendFileBeanMock.getContent());
        assertNull(sendFileBeanMock.getFilename());

        wrapper = new FastaResultDisplayWrapper(sequence1, null);
        controller.actionDownloadResultAsFasta(wrapper, 42);
        String result = new String(sendFileBeanMock.getContent());
        String expected = ">1 firstName1\nsequence1";
        assertEquals(expected, result);
        assertEquals("result_42.fasta", sendFileBeanMock.getFilename());
    }

    @Test
    public void test_actionOnChangeSortBy_after_loadingResults() {
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResults(alignments);
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();
        List<FastaResultDisplayWrapper> results = controller.getResults();

        assertionsForDefaultAlignments(results);

        // result list sorted differently
        controller.setSortBy(SortItem.SUBJECTNAME);
        controller.actionOnChangeSortBy();

        assertThat(results, hasSize(4));
        assertEquals(fastaResults.get(3), results.get(0).getFastaResult());
        assertTrue(sequence1.isEqualTo(results.get(0).getSequence()));
        assertEquals(fastaResults.get(1), results.get(1).getFastaResult());
        assertTrue(sequence2.isEqualTo(results.get(1).getSequence()));
        assertEquals(fastaResults.get(0), results.get(2).getFastaResult());
        assertTrue(sequence3.isEqualTo(results.get(2).getSequence()));
        assertEquals(fastaResults.get(2), results.get(3).getFastaResult());
        assertTrue(sequence3.isEqualTo(results.get(3).getSequence()));
    }

    @Test
    public void test_getLocalizedSortByLabel() {
        assertEquals("sequenceSearch_sortItem_EVALUE", controller.getLocalizedSortByLabel(SortItem.EVALUE));
        assertEquals("sequenceSearch_sortItem_SMITHWATERMANSCORE",
                controller.getLocalizedSortByLabel(SortItem.SMITHWATERMANSCORE));
    }

    @Test
    public void test_getAndSetSortBy() {
        assertEquals(SortItem.EVALUE, controller.getSortBy());
        controller.setSortBy(SortItem.SIMILARITY);
        assertEquals(SortItem.SIMILARITY, controller.getSortBy());
    }

    @Test
    public void test_getSortByItems() {
        assertArrayEquals(SortItem.values(), controller.getSortByItems());
    }

    @Test
    public void test_getResults() {
        assertThat(controller.getResults(), empty());

        // load some results
        sequenceSearchServiceMock.setBehaviour(request -> {
            SearchResult result = new SearchResultImpl(new Node());
            result.addResults(alignments);
            return result;
        });
        sequenceSearchMaskController.actionStartMaterialSearch();
        List<FastaResultDisplayWrapper> results = controller.getResults();

        assertionsForDefaultAlignments(results);
    }

    private void assertionsForDefaultAlignments(List<FastaResultDisplayWrapper> results) {
        assertThat(results, hasSize(4));
        assertEquals(fastaResults.get(0), results.get(0).getFastaResult());
        assertTrue(sequence3.isEqualTo(results.get(0).getSequence()));
        assertEquals(fastaResults.get(1), results.get(1).getFastaResult());
        assertTrue(sequence2.isEqualTo(results.get(1).getSequence()));
        assertEquals(fastaResults.get(2), results.get(2).getFastaResult());
        assertTrue(sequence3.isEqualTo(results.get(2).getSequence()));
        assertEquals(fastaResults.get(3), results.get(3).getFastaResult());
        assertTrue(sequence1.isEqualTo(results.get(3).getSequence()));
    }
}
