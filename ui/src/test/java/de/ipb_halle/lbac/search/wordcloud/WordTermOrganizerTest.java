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
public class WordTermOrganizerTest {

    public WordTermOrganizer instance = new WordTermOrganizer();

    @Test
    public void orderTermsTest() {
        List<WordTerm> terms = createTerms(2, FreqCategory.HIGHEST);
        terms.addAll(createTerms(3, FreqCategory.HIGH));
        terms.addAll(createTerms(1, FreqCategory.MEDIUM));
        terms.addAll(createTerms(4, FreqCategory.LOW));
        terms.addAll(createTerms(2, FreqCategory.LOWEST));

        terms = instance.orderTerms(terms, 1000);
        Assert.assertEquals(12, terms.size());
        Assert.assertEquals(FreqCategory.LOWEST, terms.get(0).getCategory());
        Assert.assertEquals(FreqCategory.LOW, terms.get(1).getCategory());
        Assert.assertEquals(FreqCategory.LOW, terms.get(2).getCategory());
        Assert.assertEquals(FreqCategory.HIGH, terms.get(3).getCategory());
        Assert.assertEquals(FreqCategory.HIGH, terms.get(4).getCategory());
        Assert.assertEquals(FreqCategory.HIGHEST, terms.get(5).getCategory());
        Assert.assertEquals(FreqCategory.HIGHEST, terms.get(6).getCategory());
        Assert.assertEquals(FreqCategory.HIGH, terms.get(7).getCategory());
        Assert.assertEquals(FreqCategory.MEDIUM, terms.get(8).getCategory());
        Assert.assertEquals(FreqCategory.LOW, terms.get(9).getCategory());
        Assert.assertEquals(FreqCategory.LOW, terms.get(10).getCategory());
        Assert.assertEquals(FreqCategory.LOWEST, terms.get(11).getCategory());

        terms = instance.orderTerms(terms, 5);
        Assert.assertEquals(5, terms.size());

    }

    private List<WordTerm> createTerms(int amount, FreqCategory cat) {
        List<WordTerm> terms = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            WordTerm t = new WordTerm(1, "TERM" + i + " -" + cat.toString(), "TERM" + i + " -" + cat.toString());
            t.setCategory(cat);
            terms.add(t);
        }
        return terms;

    }
}
