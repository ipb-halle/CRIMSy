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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.file.TermFrequency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class WordTermListMergerTest {

    private WordTermListMerger instance = new WordTermListMerger();

    @Test
    public void mergeTermsTest() {
        List<WordTerm> termList = new ArrayList<>();
        termList.add(new WordTerm(1, "term1", "term1"));
        termList.add(new WordTerm(6, "term2", "term2"));
        termList.add(new WordTerm(9, "term3", "term3"));

        termList = instance.mergeTerms(termList, new ArrayList<>());
        Assert.assertEquals(3, termList.size());

        List<Document> docList = new ArrayList<>();
        Document d = new Document();
        d.getTermFreqList().getTermFreq().add(new TermFrequency("term5", 4));
        d.getTermFreqList().getTermFreq().add(new TermFrequency("term4", 13));
        docList.add(d);

        termList = instance.mergeTerms(termList, docList);
        Assert.assertEquals(5, termList.size());

        docList.clear();
        d = new Document();
        d.getTermFreqList().getTermFreq().add(new TermFrequency("term1", 6));
        d.getTermFreqList().getTermFreq().add(new TermFrequency("term6", 13));
        docList.add(d);
        termList = instance.mergeTerms(termList, docList);
        Assert.assertEquals(6, termList.size());

        Assert.assertEquals(7, termList.get(0).getAboluteFrequency());
        Assert.assertEquals(6, termList.get(1).getAboluteFrequency());
        Assert.assertEquals(9, termList.get(2).getAboluteFrequency());
        Assert.assertEquals(4, termList.get(3).getAboluteFrequency());
        Assert.assertEquals(13, termList.get(4).getAboluteFrequency());
        Assert.assertEquals(13, termList.get(5).getAboluteFrequency());

    }
}
