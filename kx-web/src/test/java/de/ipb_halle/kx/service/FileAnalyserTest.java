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

import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.lbac.file.FilterDefinitionInputStreamFactory;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class FileAnalyserTest {

    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";

    @Test
    public void test001_analyseEnglishPdf() throws FileNotFoundException, Exception {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "Document1.pdf", 1);
        List<TermVector> tvs = analyser.getTermVector();
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
            if (swo.getStemmedWord().equals("java")) {
                Assert.assertEquals(1, swo.getOriginalWord().size());
                Assert.assertEquals("java", swo.getOriginalWord().iterator().next());
                continue;
            }
            if (swo.getStemmedWord().equals("failure")) {
                Assert.assertEquals(1, swo.getOriginalWord().size());
                Assert.assertEquals("failure", swo.getOriginalWord().iterator().next());
                continue;
            }
            throw new Exception("Unexpected stemmed word found:" + swo.getStemmedWord());
        }

        Assert.assertEquals("undefined", analyser.getLanguage());

    }

    @Test
    public void test002_analyseGermanXls() throws FileNotFoundException {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "TestTabelle.xlsx", 1);
        //Assert.assertEquals(20, analyser.getWordOrigins().size());
        //Assert.assertEquals(20, analyser.getTermVector().size());
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test003_analyseFrenchWord() throws FileNotFoundException {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "Document_FR.docx", 1);
        // Assert.assertEquals(210, analyser.getWordOrigins().size());
        // Assert.assertEquals(210, analyser.getTermVector().size());
        Assert.assertEquals("fr", analyser.getLanguage());
    }

    @Test
    public void test004_analyseStemming() throws FileNotFoundException {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "Document_wordStemming.docx", 1);
        //  Assert.assertEquals(12, analyser.getWordOrigins().size());
        //  Assert.assertEquals(12, analyser.getTermVector().size());
        for (StemmedWordOrigin swo : analyser.getWordOrigins()) {
            if (swo.getStemmedWord().equals("saur")) {
                Set<String> originWords = swo.getOriginalWord();
                Assert.assertEquals(2, originWords.size());
                for (String s : originWords) {
                    Assert.assertTrue(s.equals("säuren") || s.equals("säure"));
                }
            }
        }
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test005_checkUniqueWordOrigins() throws FileNotFoundException, Exception {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf", 1);
        List<TermVector> tvs = analyser.getTermVector();
        //  Assert.assertEquals(5428, tvs.size());
        Assert.assertEquals("de", analyser.getLanguage());
    }

    @Test
    public void test006_analyseRealWorldText() throws FileNotFoundException, Exception {
        FileAnalyser analyser = new FileAnalyser(FilterDefinitionInputStreamFactory.getFilterDefinition());
        analyser.analyseFile(examplaDocsRootFolder + "ShortRealText.docx", 1);
    }
}
