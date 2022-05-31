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
package de.ipb_halle.lbac.material.sequence.search.display;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper.AlignmentLine;
import de.ipb_halle.lbac.util.ResourceUtils;

/**
 * @author flange
 */
public class FastaResultDisplayWrapperTest {
    private Sequence sequence = new Sequence(1, null, null, null, null, null);

    private Reader readerForResourceFile(String filename) {
        return ResourceUtils.readerForResourceFile("fastaresults/" + filename);
    }

    @Test
    public void test_getSequence() {
        assertSame(sequence, new FastaResultDisplayWrapper(sequence, new FastaResult()).getSequence());
    }

    @Test
    public void test_getFastaResult() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results1.txt");
        FastaResult result = new FastaResultParser(reader).parse().get(0);

        assertSame(result, new FastaResultDisplayWrapper(sequence, result).getFastaResult());
    }

    @Test
    public void test_getAndSetConfig() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results1.txt");
        FastaResult result = new FastaResultParser(reader).parse().get(0);
        ResultDisplayConfig config = new ResultDisplayConfig();

        assertSame(config, new FastaResultDisplayWrapper(sequence, result).config(config).getConfig());
    }

    @Test
    public void test_replaceLeadingChars() {
        assertEquals("     ABC--DEF--G", FastaResultDisplayWrapper.replaceLeadingChars("-----ABC--DEF--G", '-', ' '));
    }

    @Test
    public void test_segmentString() {
        assertThrows(IllegalArgumentException.class, () -> FastaResultDisplayWrapper.segmentString("123456789", 0));
        assertEquals(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                FastaResultDisplayWrapper.segmentString("123456789", 1));
        assertEquals(Arrays.asList("12", "34", "56", "78", "9"),
                FastaResultDisplayWrapper.segmentString("123456789", 2));
        assertEquals(Arrays.asList("123", "456", "789"), FastaResultDisplayWrapper.segmentString("123456789", 3));
        assertEquals(Arrays.asList("1234", "5678", "9"), FastaResultDisplayWrapper.segmentString("123456789", 4));
        assertEquals(Arrays.asList("12345", "6789"), FastaResultDisplayWrapper.segmentString("123456789", 5));
        assertEquals(Arrays.asList("123456789"), FastaResultDisplayWrapper.segmentString("123456789", 9));
        assertEquals(Arrays.asList("123456789"), FastaResultDisplayWrapper.segmentString("123456789", 10));
    }

    @Test
    public void test_createAlignmentLinesWithForwardAndIndexMultiplier1() {
        List<AlignmentLine> results;

        results = FastaResultDisplayWrapper.createAlignmentLines("123456789", 1, 4, 1, false);
        assertEquals(3, results.size());
        assertEquals(1, results.get(0).getStartIndex());
        assertEquals(4, results.get(0).getStopIndex());
        assertEquals("1234", results.get(0).getLine());
        assertEquals(5, results.get(1).getStartIndex());
        assertEquals(8, results.get(1).getStopIndex());
        assertEquals("5678", results.get(1).getLine());
        assertEquals(9, results.get(2).getStartIndex());
        assertEquals(9, results.get(2).getStopIndex());
        assertEquals("9", results.get(2).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("123456789", 20, 6, 1, false);
        assertEquals(2, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(25, results.get(0).getStopIndex());
        assertEquals("123456", results.get(0).getLine());
        assertEquals(26, results.get(1).getStartIndex());
        assertEquals(28, results.get(1).getStopIndex());
        assertEquals("789", results.get(1).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("    12-3--4567---89", 20, 6, 1, false);
        assertEquals(4, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(21, results.get(0).getStopIndex());
        assertEquals("    12", results.get(0).getLine());
        assertEquals(22, results.get(1).getStartIndex());
        assertEquals(24, results.get(1).getStopIndex());
        assertEquals("-3--45", results.get(1).getLine());
        assertEquals(25, results.get(2).getStartIndex());
        assertEquals(27, results.get(2).getStopIndex());
        assertEquals("67---8", results.get(2).getLine());
        assertEquals(28, results.get(3).getStartIndex());
        assertEquals(28, results.get(3).getStopIndex());
        assertEquals("9", results.get(3).getLine());
    }

    @Test
    public void test_createAlignmentLinesWithForwardAndIndexMultiplier3() {
        List<AlignmentLine> results;
        results = FastaResultDisplayWrapper.createAlignmentLines("123456789", 1, 4, 3, false);
        assertEquals(3, results.size());
        assertEquals(1, results.get(0).getStartIndex());
        assertEquals(12, results.get(0).getStopIndex());
        assertEquals("1234", results.get(0).getLine());
        assertEquals(13, results.get(1).getStartIndex());
        assertEquals(24, results.get(1).getStopIndex());
        assertEquals("5678", results.get(1).getLine());
        assertEquals(25, results.get(2).getStartIndex());
        assertEquals(27, results.get(2).getStopIndex());
        assertEquals("9", results.get(2).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("123456789", 20, 6, 3, false);
        assertEquals(2, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(37, results.get(0).getStopIndex());
        assertEquals("123456", results.get(0).getLine());
        assertEquals(38, results.get(1).getStartIndex());
        assertEquals(46, results.get(1).getStopIndex());
        assertEquals("789", results.get(1).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("    12-3--4567---89", 20, 6, 3, false);
        assertEquals(4, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(25, results.get(0).getStopIndex());
        assertEquals("    12", results.get(0).getLine());
        assertEquals(26, results.get(1).getStartIndex());
        assertEquals(34, results.get(1).getStopIndex());
        assertEquals("-3--45", results.get(1).getLine());
        assertEquals(35, results.get(2).getStartIndex());
        assertEquals(43, results.get(2).getStopIndex());
        assertEquals("67---8", results.get(2).getLine());
        assertEquals(44, results.get(3).getStartIndex());
        assertEquals(46, results.get(3).getStopIndex());
        assertEquals("9", results.get(3).getLine());
    }

    @Test
    public void test_createAlignmentLinesWithReverseAndIndexMultiplier1() {
        List<AlignmentLine> results;

        results = FastaResultDisplayWrapper.createAlignmentLines("987654321", 9, 4, 1, true);
        assertEquals(3, results.size());
        assertEquals(9, results.get(0).getStartIndex());
        assertEquals(6, results.get(0).getStopIndex());
        assertEquals("9876", results.get(0).getLine());
        assertEquals(5, results.get(1).getStartIndex());
        assertEquals(2, results.get(1).getStopIndex());
        assertEquals("5432", results.get(1).getLine());
        assertEquals(1, results.get(2).getStartIndex());
        assertEquals(1, results.get(2).getStopIndex());
        assertEquals("1", results.get(2).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("987654321", 20, 6, 1, true);
        assertEquals(2, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(15, results.get(0).getStopIndex());
        assertEquals("987654", results.get(0).getLine());
        assertEquals(14, results.get(1).getStartIndex());
        assertEquals(12, results.get(1).getStopIndex());
        assertEquals("321", results.get(1).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("    98-7--6543---21", 20, 6, 1, true);
        assertEquals(4, results.size());
        assertEquals(20, results.get(0).getStartIndex());
        assertEquals(19, results.get(0).getStopIndex());
        assertEquals("    98", results.get(0).getLine());
        assertEquals(18, results.get(1).getStartIndex());
        assertEquals(16, results.get(1).getStopIndex());
        assertEquals("-7--65", results.get(1).getLine());
        assertEquals(15, results.get(2).getStartIndex());
        assertEquals(13, results.get(2).getStopIndex());
        assertEquals("43---2", results.get(2).getLine());
        assertEquals(12, results.get(3).getStartIndex());
        assertEquals(12, results.get(3).getStopIndex());
        assertEquals("1", results.get(3).getLine());
    }

    @Test
    public void test_createAlignmentLinesWithReverseAndIndexMultiplier3() {
        List<AlignmentLine> results;

        results = FastaResultDisplayWrapper.createAlignmentLines("987654321", 40, 4, 3, true);
        assertEquals(3, results.size());
        assertEquals(40, results.get(0).getStartIndex());
        assertEquals(29, results.get(0).getStopIndex());
        assertEquals("9876", results.get(0).getLine());
        assertEquals(28, results.get(1).getStartIndex());
        assertEquals(17, results.get(1).getStopIndex());
        assertEquals("5432", results.get(1).getLine());
        assertEquals(16, results.get(2).getStartIndex());
        assertEquals(14, results.get(2).getStopIndex());
        assertEquals("1", results.get(2).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("987654321", 42, 6, 3, true);
        assertEquals(2, results.size());
        assertEquals(42, results.get(0).getStartIndex());
        assertEquals(25, results.get(0).getStopIndex());
        assertEquals("987654", results.get(0).getLine());
        assertEquals(24, results.get(1).getStartIndex());
        assertEquals(16, results.get(1).getStopIndex());
        assertEquals("321", results.get(1).getLine());

        results = FastaResultDisplayWrapper.createAlignmentLines("    98-7--6543---21", 100, 6, 3, true);
        assertEquals(4, results.size());
        assertEquals(100, results.get(0).getStartIndex());
        assertEquals(95, results.get(0).getStopIndex());
        assertEquals("    98", results.get(0).getLine());
        assertEquals(94, results.get(1).getStartIndex());
        assertEquals(86, results.get(1).getStopIndex());
        assertEquals("-7--65", results.get(1).getLine());
        assertEquals(85, results.get(2).getStartIndex());
        assertEquals(77, results.get(2).getStopIndex());
        assertEquals("43---2", results.get(2).getLine());
        assertEquals(76, results.get(3).getStartIndex());
        assertEquals(74, results.get(3).getStopIndex());
        assertEquals("1", results.get(3).getLine());
    }

    @Test
    public void test_getAlignmentsProteinQueryProteinDatabase() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results1.txt");
        List<FastaResult> results = new FastaResultParser(reader).parse();
        String expected;
        ResultDisplayConfig config = new FastaResultDisplayConfig();

        config.setLineLength(50);
        config.setPrefixSpaces(2);
        config.setSuffixSpaces(2);

        // result 0
        expected =
            "Query     1                                SAVQQKLAALEKSSGGRLGV   20\n"+
            "                                           ::::::::::::::::::::\n"+
            "Subject   1  MVTKRVQRMMFAAAACIPLLLGSAPLYAQTSAVQQKLAALEKSSGGRLGV   50\n"+
            "\n"+
            "Query    21  ALIDTADNTQVLYRGDERFPMCSTSKVMAA                       50\n"+
            "             ::::::::::::::::::::::::::::::\n"+
            "Subject  51  ALIDTADNTQVLYRGDERFPMCSTSKVMAAAAVLKQSETQKQLLNQPVEI  100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101  KPADLVNYNPIAEKHVNGTM  120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(0)).config(config).getAlignments());

        // result 1
        expected =
            "Query     1                        SAVQQKLAALEKSSGGRLGVALIDTADN   28\n"+
            "                                       ...   :.. .::.:.  .: :..\n"+
            "Subject   1  MRYIRLCIISLLATLPLAVHASPQPLEQIKQSESQLSGRVGMIEMDLASG   50\n"+
            "\n"+
            "Query    29  -TQVLYRGDERFPMCSTSKVMAA                              50\n"+
            "             -: . .:.:::::: :: ::.\n"+
            "Subject  51  RTLTAWRADERFPMMSTFKVVLCGAVLARVDAGDEQLERKIHYRQQDLVD  100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101  YSPVSEKHLADGMTVGELCA  120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(1)).config(config).getAlignments());

        // result 2
        expected =
            "Query     1                        SAVQQKLAALEKSSGGRLGVALIDTADN   28\n"+
            "                                       ...   :.. .::.:.  .: :..\n"+
            "Subject   1  MRYIRLCIISLLATLPLAVHASPQPLEQIKQSESQLSGRVGMIEMDLASG   50\n"+
            "\n"+
            "Query    29  -TQVLYRGDERFPMCSTSKVMAA                              50\n"+
            "             -: . .:.:::::: :: ::.\n"+
            "Subject  51  RTLTAWRADERFPMMSTFKVVLCGAVLARVDAGDEQLERKIHYRQQDLVD  100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101  YSPVSEKHLADGMTVGELCA  120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(2)).config(config).getAlignments());

        // result 3
        expected =
            "Query     1                  SAVQQKLAALEKSSGGRLGVALI---DTADNTQV   31\n"+
            "                                           :.:  :::.---. :.   :\n"+
            "Subject  85  MVDDRVAGPLIRSVLPAGWFIADKTGASKRGARGIVALLGPNNKAERIVV  134\n"+
            "\n"+
            "Query    32  LYRGDERFPMCSTSKVMAA   50\n"+
            "             :: :\n"+
            "Subject 135  LYIGX                139";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(3)).config(config).getAlignments());

        config.setLineLength(20);
        config.setPrefixSpaces(4);
        config.setSuffixSpaces(6);

        // result 0
        expected =
            "Query\n"+
            "\n"+
            "Subject   1    MVTKRVQRMMFAAAACIPLL       20\n"+
            "\n"+
            "Query     1              SAVQQKLAAL       10\n"+
            "                         ::::::::::\n"+
            "Subject  21    LGSAPLYAQTSAVQQKLAAL       40\n"+
            "\n"+
            "Query    11    EKSSGGRLGVALIDTADNTQ       30\n"+
            "               ::::::::::::::::::::\n"+
            "Subject  41    EKSSGGRLGVALIDTADNTQ       60\n"+
            "\n"+
            "Query    31    VLYRGDERFPMCSTSKVMAA       50\n"+
            "               ::::::::::::::::::::\n"+
            "Subject  61    VLYRGDERFPMCSTSKVMAA       80\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject  81    AAVLKQSETQKQLLNQPVEI      100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101    KPADLVNYNPIAEKHVNGTM      120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(0)).config(config).getAlignments());

        // result 1
        expected =
            "Query\n"+
            "\n"+
            "Subject   1    MRYIRLCIISLLATLPLAVH       20\n"+
            "\n"+
            "Query     1      SAVQQKLAALEKSSGGRL       18\n"+
            "                     ...   :.. .::.\n"+
            "Subject  21    ASPQPLEQIKQSESQLSGRV       40\n"+
            "\n"+
            "Query    19    GVALIDTADN-TQVLYRGDE       37\n"+
            "               :.  .: :..-: . .:.::\n"+
            "Subject  41    GMIEMDLASGRTLTAWRADE       60\n"+
            "\n"+
            "Query    38    RFPMCSTSKVMAA              50\n"+
            "               :::: :: ::.\n"+
            "Subject  61    RFPMMSTFKVVLCGAVLARV       80\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject  81    DAGDEQLERKIHYRQQDLVD      100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101    YSPVSEKHLADGMTVGELCA      120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(1)).config(config).getAlignments());

        // result 2
        expected =
            "Query\n"+
            "\n"+
            "Subject   1    MRYIRLCIISLLATLPLAVH       20\n"+
            "\n"+
            "Query     1      SAVQQKLAALEKSSGGRL       18\n"+
            "                     ...   :.. .::.\n"+
            "Subject  21    ASPQPLEQIKQSESQLSGRV       40\n"+
            "\n"+
            "Query    19    GVALIDTADN-TQVLYRGDE       37\n"+
            "               :.  .: :..-: . .:.::\n"+
            "Subject  41    GMIEMDLASGRTLTAWRADE       60\n"+
            "\n"+
            "Query    38    RFPMCSTSKVMAA              50\n"+
            "               :::: :: ::.\n"+
            "Subject  61    RFPMMSTFKVVLCGAVLARV       80\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject  81    DAGDEQLERKIHYRQQDLVD      100\n"+
            "\n"+
            "Query\n"+
            "\n"+
            "Subject 101    YSPVSEKHLADGMTVGELCA      120";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(2)).config(config).getAlignments());

        // result 3
        expected =
            "Query     1                    SAVQ        4\n"+
            "\n"+
            "Subject  85    MVDDRVAGPLIRSVLPAGWF      104\n"+
            "\n"+
            "Query     5    QKLAALEKSSGGRLGVALI-       23\n"+
            "                         :.:  :::.-\n"+
            "Subject 105    IADKTGASKRGARGIVALLG      124\n"+
            "\n"+
            "Query    24    --DTADNTQVLYRGDERFPM       41\n"+
            "               --. :.   ::: :\n"+
            "Subject 125    PNNKAERIVVLYIGX           139\n"+
            "\n"+
            "Query    42    CSTSKVMAA       50\n"+
            "\n"+
            "Subject";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(3)).config(config).getAlignments());
    }

    @Test
    public void test_getAlignmentsDNAQueryDNADatabase() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results8.txt");
        List<FastaResult> results = new FastaResultParser(reader).parse();
        String expected;
        ResultDisplayConfig config = new FastaResultDisplayConfig();

        config.setLineLength(50);
        config.setPrefixSpaces(2);
        config.setSuffixSpaces(2);

        /*
         * result 9: >>ENA|BAC26705|BAC26705.1 Mus musculus (house mouse) hypothetical protein
         * 
         * This result is tested because the query alignment is reverse-complemented.
         */
        expected =
            "Query    99             GTCGCTGTACTGCAAAGCGGCCGCGCTCAGTTCTGCCAG    61\n"+
            "                                           ::::::::  : :  ::::\n"+
            "Subject 883  GAGCATAACAACCCCGCCTACACTATCAGCGCCGCGCTGGGCTACGCCAC   932\n"+
            "\n"+
            "Query    60  CGTCATTGTGCCGTTGACGTGTTTTTCGGCAATCGGATTGTAGTTAACCA    11\n"+
            "\n"+
            "Subject 933  GCAGCTCGTCAACATTGTGTCTCACATACTTGACATCAATCTTCCCAAAA   982\n"+
            "\n"+
            "Query    10  GATCGGCAGG               1\n"+
            "\n"+
            "Subject 983  AGCTGTGCAACAGCGAGTTC  1002";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(8)).config(config).getAlignments());
    }

    @Test
    public void test_getAlignmentsDNAQueryProteinDatabase() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results9.txt");
        List<FastaResult> results = new FastaResultParser(reader).parse();
        String expected;
        ResultDisplayConfig config = new FastxyResultDisplayConfig();

        config.setLineLength(50);
        config.setPrefixSpaces(2);
        config.setSuffixSpaces(2);

        /*
         * result 7: >>sp|Q9K9L8|GLSA1_BACHD Glutaminase 1 OS=Bacillus halodurans (strain ATCC BAA-125 / DSM 18197 / FERM 7344 / JCM 9153 / C-125) OX=272558 GN=glsA1 PE=3 SV=1
         * 
         * query alignment contains a stop codon and is the reverse-complement
         */
        expected =
            "Query    83  AAALSSASVIVPLTCFSAIGL*LT   12\n"+
            "             :.::. ...:.  :  ...:  :.\n"+
            "Subject 117  AGALAVTNMIIGETTEQSLGRLLS  140";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(6)).config(config).getAlignments());
    }

    @Test
    public void test_getAlignmentsProteinQueryDNADatabase() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results12.txt");
        List<FastaResult> results = new FastaResultParser(reader).parse();
        String expected;
        ResultDisplayConfig config = new TfastxyResultDisplayConfig();

        config.setLineLength(50);
        config.setPrefixSpaces(2);
        config.setSuffixSpaces(2);

        /*
         * result 1: >>ENA|BAA28282|BAA28282.1 Escherichia coli beta-lactamase
         */
        expected =
            "Query    1  SAVQQKLAALEKSSGGRLGVALIDTADNTQVLYRGDERFPMCSTSKVMAA   50\n"+
            "            ::::::::::::::::::::::::::::::::::::::::::::::::::\n"+
            "Subject 91  SAVQQKLAALEKSSGGRLGVALIDTADNTQVLYRGDERFPMCSTSKVMAA  240"; // tfasty reports 237 as al_stop
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(0)).config(config).getAlignments());

        /*
         * result 7: >>ENA|BAA14224|BAA14224.1 Streptomyces cacaoi beta-lactamase
         * 
         * subject alignment is the reverse-complement
         */
        expected =
            "Query    9  ALEKSSGG  16\n"+
            "            : ::..::\n"+
            "Subject 36  AEEKTTGG  13"; // tfasty reports 16 as al_stop
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(6)).config(config).getAlignments());

        /*
         * result 14: >>ENA|CAA06312|CAA06312.1 Salmonella enterica subsp. enterica serovar Typhimurium CTX-M-7
         * 
         * subject alignment is reverse-complement and contains a frame shift and a stop codon
         * 
         * Can we understand the frame shift?
         * 5'-GCGATGTGCAGTACCAGTAAG-3' (part of the database sequence in the +1 frame (= no frame shift) starting with base 208 and ending with base 228)
         * 3'-CGCTACACGTCATGGTCATTC-5' (complement)
         *    zzzyyyxxxTTTGGGTTTLLL    (translation of the complement from 5' to 3' in the +1 frame)
         *         DD D                (frame shift???)
         *     SSSTTT                  (translation of the complement from 5' to 3' in the +3 frame)
         */
        expected =
            "Query     9  ALEKSSGGRLGVALI-DTADNTQVLYRGDERFPMCSTSKVMA   49\n"+
            "             :: :   ::  ..  -::..:..   : .  .  : ... .:\n"+
            "Subject 261  ALFKHRRGRHHLTGT/DTSQNAHRPRRESANYRRC*SAQRQA  138";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(13)).config(config).getAlignments());
    }

    @Test
    public void test_getAlignmentsDNAQueryProteinDatabaseWithFrameShifts() throws IOException, FastaResultParserException {
        Reader reader = readerForResourceFile("results_fastx_mgstm1.txt");
        List<FastaResult> results = new FastaResultParser(reader).parse();
        String expected;
        ResultDisplayConfig config = new FastxyResultDisplayConfig();

        config.setLineLength(60);
        config.setPrefixSpaces(2);
        config.setSuffixSpaces(2);

        /*
         * How to identify the stop indices in the query sequence (mgstm1.e05)?
         * What was done here:
         *   (a) take the last few AAs at the end of the alignment line
         *   (b) translate the whole DB sequence and reduce it until its translation matches the AAs
         *   (c) search with this segment in the DB sequence and identify the index of the last base
         *   (d) cross-check with "fastx -m B -w 60 -n mgstm1.e05 mgstm1.aa"
         * Results:
         *   Alignment line 1: FKLGLEF -> TTCAAGCTGGGCCTGGAATTT, which ends at base 208 (fastx reports 207)
         *   Alignment line 2: GNPLQXXM -> GGAAACCCGCTGCAGCNNNNCATG, end at base 376 (fastx reports 375)
         *   Alignment line 3: DQYRM -> GACCAGTACCGTATG, end at base 548 (fastx reports 546)
         *   Alignment line 4: HWSNK -> CACTGGAGTAACAAG, end at base 697 (fastx reports 693)
         * 
         * Proposal from this back-engineering:
         * - '/' shifts the index by 1
         * - '\' shifts the index by -1
         */
        expected =
            "Query    40  MPMI/MGYWKVRGLTHPIRMLLEYTDPSYDEKRYTMGD\\APDFDR-QWLNEK\\FKLGLEF  208\n"+
            "             ::::-.:::.:::::::::::::::: :::::::::::-::::::-::::::-:::::.:\n"+
            "Subject   1  MPMI-LGYWNVRGLTHPIRMLLEYTDSSYDEKRYTMGD-APDFDRSQWLNEK-FKLGLDF   57\n"+
            "\n"+
            "Query   209  P\\NLPYLIDGSHKITQ/ENAILRYLA/HKAHLEEMTEEERIRADIVENQIA\\GNPLQXXM  376\n"+
            "             :-::::::::::::::- ::::::::-.: ::.  ::::::::::::::. - . .:  :\n"+
            "Subject  58  P-NLPYLIDGSHKITQ-SNAILRYLA-RKHHLDGETEEERIRADIVENQVM-DTRMQLIM  113\n"+
            "\n"+
            "Query   377  LS\\YNLDFEKQKPEFLKTIPEKM/ELYSEFLGCKRPWFAWDK\\VTYVDFFAYDILDQYRM  548\n"+
            "             : -:: :::::::::::::::::-.:::::::-:::::: ::-::::::.::::::::::\n"+
            "Subject 114  LC-YNPDFEKQKPEFLKTIPEKM-KLYSEFLG-KRPWFAGDK-VTYVDFLAYDILDQYRM  169\n"+
            "\n"+
            "Query   549  FEP/KCLDAFPNLR\\DFLARFEGLKKISA\\YMKSSRYIGTA\\IFTKMAHWSNK  697\n"+
            "             :::-::::::::::-::::::::::::::-::::::::.: -::.::::::::\n"+
            "Subject 170  FEP-KCLDAFPNLR-DFLARFEGLKKISA-YMKSSRYIATP-IFSKMAHWSNK  218";
        assertEquals(expected, new FastaResultDisplayWrapper(sequence, results.get(0)).config(config).getAlignments());
    }
}