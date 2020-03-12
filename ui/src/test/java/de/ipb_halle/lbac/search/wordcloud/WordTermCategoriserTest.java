/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class WordTermCategoriserTest {

    private WordTermCategoriser instance = new WordTermCategoriser(100);

    @Test
    public void categoriseTest() {

        List<WordTerm> terms = new ArrayList<>();
        terms.add(new WordTerm(1, "Word1", "word1"));
        terms.add(new WordTerm(2, "Word2", "word2"));
        terms.add(new WordTerm(2, "Word3", "word3"));
        terms.add(new WordTerm(4, "Word4", "word4"));
        terms.add(new WordTerm(6, "Word5", "word5"));
        terms.add(new WordTerm(9, "Word6", "word6"));
        terms.add(new WordTerm(11, "Word7", "word7"));
        terms.add(new WordTerm(8, "Word8", "word8"));
        terms.add(new WordTerm(3, "Word9", "word9"));
        terms.add(new WordTerm(2, "Word10", "word10"));
        terms.add(new WordTerm(2, "Word11", "word11"));
        terms.add(new WordTerm(4, "Word12", "word12"));
        terms.add(new WordTerm(11, "Word13", "word13"));
        terms.add(new WordTerm(9, "Word14", "word14"));
        terms.add(new WordTerm(8, "Word15", "word15"));
        terms.add(new WordTerm(7, "Word16", "word16"));
        terms.add(new WordTerm(9, "Word17", "word17"));
        terms.add(new WordTerm(3, "Word18", "word18"));
        terms.add(new WordTerm(2, "Word19", "word19"));

        Map<String, FreqCategory> targets = new HashMap<>();
        targets.put("Word1", FreqCategory.LOWEST);
        targets.put("Word2", FreqCategory.LOWEST);
        targets.put("Word3", FreqCategory.LOWEST);
        targets.put("Word4", FreqCategory.MEDIUM);
        targets.put("Word5", FreqCategory.MEDIUM);
        targets.put("Word6", FreqCategory.HIGH);
        targets.put("Word7", FreqCategory.HIGHEST);
        targets.put("Word8", FreqCategory.HIGH);
        targets.put("Word9", FreqCategory.LOW);
        targets.put("Word10", FreqCategory.LOWEST);
        targets.put("Word11", FreqCategory.LOW);
        targets.put("Word12", FreqCategory.MEDIUM);
        targets.put("Word13", FreqCategory.HIGHEST);
        targets.put("Word14", FreqCategory.HIGH);
        targets.put("Word15", FreqCategory.HIGH);
        targets.put("Word16", FreqCategory.MEDIUM);
        targets.put("Word17", FreqCategory.HIGHEST);
        targets.put("Word18", FreqCategory.LOW);
        targets.put("Word19", FreqCategory.LOW);

        instance.categorise(terms);

        for (WordTerm t : terms) {
            Assert.assertEquals("Wrong category of" + t.getTerm(), targets.get(t.getTerm()), t.getCategory());
        }
    }

}
