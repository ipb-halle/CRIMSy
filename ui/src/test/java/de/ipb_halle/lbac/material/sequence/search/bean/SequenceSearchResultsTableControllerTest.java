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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceAlignment;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceSearchServiceMock;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParser;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultParserException;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
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

    @Before
    public void init() throws FastaResultParserException {
        SequenceData data1 = SequenceData.builder().sequenceString("sequence1").build();
        SequenceData data2 = SequenceData.builder().sequenceString("sequence2").build();
        SequenceData data3 = SequenceData.builder().sequenceString("sequence3").build();

        List<MaterialName> names1 = Arrays.asList(new MaterialName("firstName1", "en", 1),
                new MaterialName("secondName1", "de", 100));
        List<MaterialName> names2 = Arrays.asList(new MaterialName("firstName2", "en", 1),
                new MaterialName("secondName2", "de", 100));
        List<MaterialName> names3 = Arrays.asList(new MaterialName("firstName3", "en", 1),
                new MaterialName("secondName3", "de", 100));

        sequence1 = new Sequence(1, names1, 1, new HazardInformation(), new StorageInformation(), data1);
        sequence2 = new Sequence(2, names2, 1, new HazardInformation(), new StorageInformation(), data2);
        sequence3 = new Sequence(3, names3, 1, new HazardInformation(), new StorageInformation(), data3);

        Reader reader = readerForResourceFile("fastaresults/results1.txt");
        List<FastaResult> fastaResults = new FastaResultParser(reader).parse();

        alignment1 = new SequenceAlignment(sequence1, fastaResults.get(3));
        alignment2 = new SequenceAlignment(sequence2, fastaResults.get(1));
        alignment3a = new SequenceAlignment(sequence3, fastaResults.get(2));
        alignment3b = new SequenceAlignment(sequence3, fastaResults.get(0));

        sendFileBeanMock = new SendFileBeanMock();

        controller = new SequenceSearchResultsTableController(null, sequenceSearchServiceMock, sendFileBeanMock, null);
        sequenceSearchMaskController = new SequenceSearchMaskController(controller, null, null, null, null);
        controller.setSearchMaskController(sequenceSearchMaskController);
    }

    @Test
    public void test_actionDownloadAllResultsAsFasta_after_loadingResults() throws IOException {
        List<SequenceAlignment> alignments = Arrays.asList(alignment1, alignment2, alignment3a, alignment3b);
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

    private Reader readerForResourceFile(String filename) {
        return new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename));
    }
}