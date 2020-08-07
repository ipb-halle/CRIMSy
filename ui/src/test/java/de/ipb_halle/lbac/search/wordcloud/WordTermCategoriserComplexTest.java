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

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class WordTermCategoriserComplexTest {

    private WordTermCategoriser instance;

    @Test
    public void categoriseTest() {
        int totalDocs = 250;
        instance = new WordTermCategoriserComplex(totalDocs);
        List<WordTerm> terms = new ArrayList<>();
        terms.add(new WordTerm(50, "Term1", 250,"term1"));
        terms.add(new WordTerm(45, "Term2", 220,"term2"));
        terms.add(new WordTerm(40, "Term3", 190,"term3"));
        terms.add(new WordTerm(35, "Term4", 160,"term4"));
        terms.add(new WordTerm(30, "Term5", 130,"term5"));
        terms.add(new WordTerm(25, "Term6", 100,"term6"));
        terms.add(new WordTerm(20, "Term7", 70,"term7"));
        terms.add(new WordTerm(15, "Term8", 40,"term8"));
        terms.add(new WordTerm(10, "Term9", 10,"term9"));
        terms.add(new WordTerm(5, "Term10", 1,"term10"));
        instance.categorise(terms);

        Assert.assertEquals(terms.get(0).getCategory(), FreqCategory.LOWEST);
        Assert.assertEquals(terms.get(1).getCategory(), FreqCategory.LOW);
        Assert.assertEquals(terms.get(2).getCategory(), FreqCategory.HIGH);
        Assert.assertEquals(terms.get(3).getCategory(), FreqCategory.HIGHEST);
        Assert.assertEquals(terms.get(4).getCategory(), FreqCategory.HIGHEST);
        Assert.assertEquals(terms.get(5).getCategory(), FreqCategory.HIGH);
        Assert.assertEquals(terms.get(6).getCategory(), FreqCategory.LOW);
        Assert.assertEquals(terms.get(7).getCategory(), FreqCategory.LOWEST);
        Assert.assertEquals(terms.get(8).getCategory(), FreqCategory.LOWEST);
        Assert.assertEquals(terms.get(9).getCategory(), FreqCategory.LOWEST);
    }
}
