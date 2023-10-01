/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.service;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.service.FilterDefinitionInputStreamFactory;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class FileAnalyserTest {

    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";

    private FileObject createMockFile(Integer id, String path) {
        FileObject fileObject = new FileObject();
        fileObject.setId(id);
        fileObject.setFileLocation(path);
        return fileObject;
    }

    private FileAnalyser setupAnalyser(String fileName) {
        FileAnalyser analyser = new FileAnalyser();
        analyser.setFileObject(createMockFile(1, examplaDocsRootFolder + fileName));
        analyser.run();
        return analyser;
    }

    private boolean compareTermVectors(TermVector tv1, TermVector tv2) {
        return tv1.equals(tv2) 
            && (tv1.getTermFrequency() == tv2.getTermFrequency());
    }

    @Test
    public void test001_analyseEnglishPdf() throws FileNotFoundException, Exception {
        FileAnalyser analyser = setupAnalyser("Document1.pdf");
        List<TermVector> tvs = null;
        try {
            tvs = analyser.getTermVector();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals(2, tvs.size());
        for (TermVector tv : tvs) {
            if (tv.getWordRoot().equals("java")) {
                Assert.assertEquals(3, tv.getTermFrequency());
                Assert.assertEquals(1, (int) tv.getFileId());
                continue;
            }
            if (tv.getWordRoot().equals("failure")) {
                Assert.assertEquals(37, tv.getTermFrequency());
                Assert.assertEquals(1, (int) tv.getFileId());
                continue;
            }
            throw new Exception("Unexpected termvector found:" + tv.getWordRoot());
        }

        Assert.assertEquals(2, analyser.getWordOrigins().size());
        for (StemmedWordOrigin swo : analyser.getWordOrigins()) {
            switch (swo.getStemmedWord()) {
                case "java" :
                    Assert.assertEquals("java", swo.getOriginalWord());
                    break;
                case "failure" :
                    Assert.assertEquals("failure", swo.getOriginalWord());
                    break;
                default:
                    throw new Exception("Unexpected stemmed word found:" + swo.getStemmedWord());
            }
        }

        Assert.assertEquals("undefined", analyser.getLanguage());

    }

    @Test
    public void test002_analyseGermanXls() throws FileNotFoundException {
        FileAnalyser analyser = setupAnalyser("TestTabelle.xlsx");
        //Assert.assertEquals(20, analyser.getWordOrigins().size());
        //Assert.assertEquals(20, analyser.getTermVector().size());
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test003_analyseFrenchWord() throws FileNotFoundException {
        FileAnalyser analyser = setupAnalyser("Document_FR.docx");
        // Assert.assertEquals(210, analyser.getWordOrigins().size());
        // Assert.assertEquals(210, analyser.getTermVector().size());
        Assert.assertEquals("fr", analyser.getLanguage());
    }

    @Test
    public void test004_analyseStemming() throws FileNotFoundException {
        FileAnalyser analyser = setupAnalyser("Document_wordStemming.docx");
        //  Assert.assertEquals(12, analyser.getWordOrigins().size());
        //  Assert.assertEquals(12, analyser.getTermVector().size());
        for (StemmedWordOrigin swo : analyser.getWordOrigins()) {
            if (swo.getStemmedWord().equals("saur")) {
                Assert.assertTrue(swo.getOriginalWord().equals("säuren") 
                        || swo.getOriginalWord().equals("säure"));
            }
        }
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test005_checkUniqueWordOrigins() throws FileNotFoundException, Exception {
        FileAnalyser analyser =  setupAnalyser("IPB_Jahresbericht_2004.pdf");
        Assert.assertEquals(5319, analyser.getTermVector().size());
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test006_analyseRealWorldText() throws FileNotFoundException, Exception {
        FileAnalyser analyser = setupAnalyser("ShortRealText.docx");

        Map<String, TermVector> expectedTV = Arrays.asList(
                        // TermVector(stem, fileId, frequency)
                        new TermVector("rafft", 1, 1),
                        new TermVector("menschheit", 1, 1),
                        new TermVector("gurk", 1, 1),
                        new TermVector("nikotin", 1, 1),
                        new TermVector("alkohol", 1, 1),
                        new TermVector("hopfenkaltschal", 1, 1),
                        new TermVector("schnitzel", 1, 1),
                        new TermVector("saur", 1, 1),
                        new TermVector("grundnahrungsmittel", 1, 1),
                        new TermVector("halb", 1, 1))
                .stream()
                .collect(Collectors.toMap(TermVector::getWordRoot, Function.identity()));

        List<TermVector> resultTV = analyser.getTermVector();
        Assert.assertEquals(expectedTV.size(), resultTV.size());

        for (TermVector tv : resultTV) {
            Assert.assertTrue(compareTermVectors(tv, expectedTV.get(tv.getWordRoot())));
        }

        Set<StemmedWordOrigin> expectedSWO = new HashSet<> ();
        expectedSWO.addAll(Arrays.asList(
                        new StemmedWordOrigin("rafft", "rafft"),
                        new StemmedWordOrigin("menschheit", "menschheit"),
                        new StemmedWordOrigin("gurk", "gurken"),
                        new StemmedWordOrigin("nikotin", "nikotin"),
                        new StemmedWordOrigin("alkohol", "alkohol"),
                        new StemmedWordOrigin("hopfenkaltschal", "hopfenkaltschale"),
                        new StemmedWordOrigin("schnitzel", "schnitzel"),
                        new StemmedWordOrigin("saur", "saure"),
                        new StemmedWordOrigin("grundnahrungsmittel", "grundnahrungsmittel"),
                        new StemmedWordOrigin("halb", "halbe")));
        Set<StemmedWordOrigin> resultSWO = new HashSet<> ();
        resultSWO.addAll(analyser.getWordOrigins());
        Assert.assertTrue(expectedSWO.equals(resultSWO));
    }

    @Test
    public void test007_smallNumberFilteringTest() throws Exception {
        FileAnalyser analyser = setupAnalyser("ShortNumberExample.docx");
        Assert.assertEquals(analyser.getTermVector().size(), 4);
    }
}
