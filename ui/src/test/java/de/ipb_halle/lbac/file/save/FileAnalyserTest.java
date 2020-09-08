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
package de.ipb_halle.lbac.file.save;

import de.ipb_halle.lbac.file.StemmedWordOrigin;
import de.ipb_halle.lbac.file.TermVector;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class FileAnalyserTest {

    protected String filterDefinition = "target/test-classes/fileParserFilterDefinition.json";
    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";

    @Test
    public void test001_analyseEnglishPdf() throws FileNotFoundException, Exception {
        FileAnalyser analyser = new FileAnalyser(new File(filterDefinition));
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

        analyser.getWordOrigins();
        analyser.analyseFile(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf", 1);
        tvs = analyser.getTermVector();

        Assert.assertEquals("de", analyser.getLanguage());

    }

}
